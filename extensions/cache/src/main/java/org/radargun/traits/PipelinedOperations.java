package org.radargun.traits;

import java.util.Collection;
import java.util.List;

import org.radargun.Operation;
import org.radargun.stages.test.Invocation;


/**
 * Partially taken from JSR-107 Cache
 *
 * @author Jiri Holusa (jiri@hazelcast.com)
 */
@Trait(doc = "Operations using pipelining.")
public interface PipelinedOperations {
   String TRAIT = PipelinedOperations.class.getSimpleName();
   Operation EXECUTE_PIPELINE = Operation.register(TRAIT + ".ExecutePipeline");

   /**
    * The cache may provide native implementation of pipeline get or it may be simulated
    * with multiple asynchronous operations. Native version should be preferred, unless
    * preferAsync is set to true.
    */
   <K, V> Cache<K, V> getCache(String cacheName);

   interface Cache<K, V> extends BasicOperations.Cache<K, V> {

      Collection executePipeline(List<Invocation> operations);

   }
}
