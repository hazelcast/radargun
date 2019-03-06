package org.radargun.service.redis.lettuce;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.lettuce.core.LettuceFutures;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import org.radargun.stages.cache.test.CacheInvocations.Get;
import org.radargun.stages.cache.test.CacheInvocations.Put;
import org.radargun.stages.cache.test.CacheInvocations.Remove;
import org.radargun.stages.test.Invocation;
import org.radargun.traits.BasicOperations;
import org.radargun.traits.PipelinedOperations;

public class LettuceBasicOperations implements BasicOperations, PipelinedOperations {

   private LettuceService service;

   public LettuceBasicOperations(LettuceService service) {
      this.service = service;
   }

   @SuppressWarnings("unchecked")
   @Override
   public <K, V> PipelinedOperations.Cache<K, V> getCache(String cacheName) {
      if (service.isUseAsync()) {
         return new AsyncCache<>(service.<K, V>getAsyncCommands(), service.getPipelineTimeout());
      } else {
         return new SyncCache<>(service.<K, V>getSyncCommands());
      }
   }

   private class SyncCache<K, V> implements PipelinedOperations.Cache<K, V> {
      private RedisAdvancedClusterCommands<K, V> commands;

      public SyncCache(RedisAdvancedClusterCommands<K, V> commands) {
         this.commands = commands;
      }

      @Override
      public V get(K key) {
         return commands.get(key);
      }

      @Override
      public boolean containsKey(K key) {
         return commands.exists(key) > 0;
      }

      @Override
      public void put(K key, V value) {
         commands.set(key, value);
      }

      @Override
      public V getAndPut(K key, V value) {
         return commands.getset(key, value);
      }


      @Override
      public boolean remove(K key) {
         return commands.del(key) > 0;
      }

      @Override
      public V getAndRemove(K key) {
         V value = commands.get(key);
         commands.del(key);
         return value;
      }

      @Override
      public void clear() {
         throw new UnsupportedOperationException("Clear not supported.");
      }

      @Override
      public Collection executePipeline(List<Invocation> operations) {
         throw new UnsupportedOperationException("Pipelining is not supported on async Lettuce commands.");
      }
   }

   private class AsyncCache<K, V> implements PipelinedOperations.Cache<K, V> {
      private RedisAdvancedClusterAsyncCommands<K, V> commands;
      private final int timeout;

      public AsyncCache(RedisAdvancedClusterAsyncCommands<K, V> commands, int timeout) {
         this.commands = commands;
         this.timeout = timeout;
         commands.setAutoFlushCommands(false);
      }

      @Override
      public V get(K key) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public boolean containsKey(K key) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void put(K key, V value) {
         commands.set(key, value);
      }

      @Override
      public V getAndPut(K key, V value) {
         throw new UnsupportedOperationException("Not supported yet.");
      }


      @Override
      public boolean remove(K key) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public V getAndRemove(K key) {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void clear() {
         throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Collection executePipeline(List<Invocation> operations) {
         if (operations == null) {
            return null;
         }

         List<RedisFuture<?>> futures = new ArrayList<>(operations.size());
         for (Invocation operation : operations) {
            if (operation instanceof Get) {
               Get<K, V> get = (Get) operation;
               futures.add(commands.get(get.getKey()));
            } else if (operation instanceof Put) {
               Put<K, V> put = (Put) operation;
               futures.add(commands.set(put.getKey(), put.getValue()));
            } else if (operation instanceof Remove) {
               Remove<K, V> remove = (Remove) operation;
               futures.add(commands.del(remove.getKey()));
            } else {
               throw new UnsupportedOperationException("This operation is currently not supported in pipeline: " + operation.getClass());
            }
         }

         commands.flushCommands();

         boolean success = LettuceFutures.awaitAll(timeout, TimeUnit.SECONDS, futures.toArray(new RedisFuture[futures.size()]));
         if (!success) {
            throw new IllegalStateException("Error occurred during pipeline invocation.");
         }

         Collection results = new ArrayList(futures.size());
         try {
            for (Future<?> future : futures) {
               results.add(future.get());
            }
         } catch (Exception ex) {
            throw new IllegalStateException("Error occurred during pipeline invocation.");
         }

         return results;
      }
   }

}
