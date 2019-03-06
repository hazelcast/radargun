package org.radargun.service.redis.lettuce;

import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.async.RedisAdvancedClusterAsyncCommands;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;
import io.lettuce.core.codec.ByteArrayCodec;
import org.radargun.Service;
import org.radargun.config.Property;
import org.radargun.logging.Log;
import org.radargun.logging.LogFactory;
import org.radargun.traits.Lifecycle;
import org.radargun.traits.ProvidesTrait;

@Service(doc = "Redis clustered service")
public class LettuceService implements Lifecycle {

   protected final Log log = LogFactory.getLog(getClass());

   @Property(name = "server", doc = "Address of one of the servers of Redis cluster")
   public String server;

   @Property(name = "use-async", doc = "Whether use synchronous (= false) or asychronous Lettuce commands. Default is sync (= false).")
   public boolean useAsync;

   @Property(name = "pipeline-timeout", doc = "Number of seconds to wait for an async call to complete. Default is 20.")
   public int pipelineTimeout = 20;

   private RedisClusterClient redisClient;
   private StatefulRedisClusterConnection connection;
   private boolean isRunning;

   @ProvidesTrait
   public LettuceBasicOperations getBasicOperations() {
      return new LettuceBasicOperations(this);
   }

   @ProvidesTrait
   public Lifecycle getLifecycle() {
      return this;
   }

   @Override
   public void start() {
      redisClient = RedisClusterClient.create("redis://" + server);
      isRunning = true;
   }

   @Override
   public void stop() {
      isRunning = false;
      if (connection != null) {
         connection.close();
      }
   }

   @Override
   public boolean isRunning() {
      return isRunning;
   }

   public <K, V> RedisAdvancedClusterCommands<K, V> getSyncCommands() {
      connect();
      return connection.sync();
   }

   public <K, V> RedisAdvancedClusterAsyncCommands<K, V> getAsyncCommands() {
      connect();
      return connection.async();
   }

   private synchronized void connect() {
      if (connection == null) {
         connection = redisClient.<byte[], byte[]>connect(new ByteArrayCodec());
      }
   }

   public boolean isUseAsync() {
      return useAsync;
   }

   public int getPipelineTimeout() {
      return pipelineTimeout;
   }
}
