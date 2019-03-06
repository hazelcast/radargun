package org.radargun.service;

import java.util.Collection;
import java.util.List;

import com.hazelcast.core.IMap;
import com.hazelcast.core.Pipelining;
import org.radargun.stages.cache.test.CacheInvocations.Get;
import org.radargun.stages.cache.test.CacheInvocations.Put;
import org.radargun.stages.cache.test.CacheInvocations.Remove;
import org.radargun.stages.test.Invocation;
import org.radargun.traits.PipelinedOperations;

/**
 * Functionally same as {@link HazelcastOperations} but the interfaces have changed a bit
 * and in order to support transactions we have to adapt.
 *
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
public class Hazelcast312Operations extends Hazelcast36Operations implements PipelinedOperations {

   public Hazelcast312Operations() {
   }

   public Hazelcast312Operations(Hazelcast36Service service) {
      super(service);
   }

   @Override
   public <K, V> HazelcastPipelinedCache<K, V> getCache(String cacheName) {
      return new Cache<K, V>(service.<K, V>getMap(cacheName));
   }

   protected interface HazelcastPipelinedCache<K, V> extends HazelcastCache<K, V>, PipelinedOperations.Cache<K, V> { }

   protected static class Cache<K, V> extends Hazelcast36Operations.Cache<K, V> implements HazelcastPipelinedCache<K, V> {

      public Cache(IMap<K, V> map) {
         super(map);
      }

      @Override
      public Collection<V> executePipeline(List<Invocation> operations) {
         if (operations == null) {
            return null;
         }

         Pipelining pipelined = new Pipelining(operations.size());
         IMap<K, V> imap = (IMap) map;

         try {
            for (Invocation operation : operations) {
               if (operation instanceof Get) {
                  Get<K, V> get = (Get) operation;
                  pipelined.add(imap.getAsync(get.getKey()));
               } else if (operation instanceof Put) {
                  Put<K, V> put = (Put) operation;
                  pipelined.add(imap.setAsync(put.getKey(), put.getValue()));
               } else if (operation instanceof Remove) {
                  Remove<K, V> remove = (Remove) operation;
                  pipelined.add(imap.removeAsync(remove.getKey()));
               } else {
                  throw new UnsupportedOperationException("This operation is currently not supported in pipeline: " + operation.getClass());
               }
            }
         } catch (InterruptedException ex) {
            throw new IllegalStateException("Error occurred while placing the operation into pipeline.", ex);
         }

         try {
            return pipelined.results();
         } catch (Exception ex) {
            throw  new IllegalStateException("Error occurred while executing the pipeline.", ex);
         }
      }
   }
}
