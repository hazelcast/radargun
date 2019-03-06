package org.radargun.stages.cache.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.radargun.Operation;
import org.radargun.config.Namespace;
import org.radargun.config.Property;
import org.radargun.config.Stage;
import org.radargun.stages.cache.test.CacheInvocations.ExecutePipeline;
import org.radargun.stages.test.Invocation;
import org.radargun.stages.test.OperationLogic;
import org.radargun.stages.test.OperationSelector;
import org.radargun.stages.test.RatioOperationSelector;
import org.radargun.stages.test.Stressor;
import org.radargun.stages.test.TestStage;
import org.radargun.traits.BasicOperations;
import org.radargun.traits.InjectTrait;
import org.radargun.traits.PipelinedOperations;

/**
 * @author Radim Vansa &lt;rvansa@redhat.com&gt;
 */
@Namespace(name = TestStage.NAMESPACE, deprecatedName = TestStage.DEPRECATED_NAMESPACE)
@Stage(doc = "Test using PipelinedOperations")
public class PipelinedOperationsTestStage extends CacheOperationsTestStage {
   @Property(doc = "Ratio of GET requests. Default is 4.")
   protected int getRatio = 4;

   @Property(doc = "Ratio of PUT requests. Default is 1.")
   protected int putRatio = 1;

   @Property(doc = "Ratio of REMOVE requests. Default is 0.")
   protected int removeRatio = 0;

   @Property(doc = "Size of the pipeline. Default is 5.")
   protected int pipelineSize = 5;

   @InjectTrait
   protected PipelinedOperations pipelinedOperations;

   @Override
   protected OperationSelector createOperationSelector() {
      RatioOperationSelector operationSelector = new RatioOperationSelector.Builder()
         .add(BasicOperations.GET, getRatio)
         .add(BasicOperations.PUT, putRatio)
         .add(BasicOperations.REMOVE, removeRatio)
         .build();

      return operationSelector;
   }

   @Override
   public OperationLogic getLogic() {
      return new Logic();
   }

   protected class Logic extends OperationLogic {
      protected PipelinedOperations.Cache cache;
      protected KeySelector keySelector;

      private List<Invocation> pipeline;

      @Override
      public void init(Stressor stressor) {
         super.init(stressor);
         String cacheName = cacheSelector.getCacheName(stressor.getGlobalThreadIndex());
         this.cache = pipelinedOperations.getCache(cacheName);
         keySelector = getKeySelector(stressor);
         pipeline = new ArrayList<>(pipelineSize);
      }

      @Override
      public void run(Operation operation) throws RequestException {
         Object key = keyGenerator.generateKey(keySelector.next());
         Random random = stressor.getRandom();

         Invocation invocation;
         if (operation == BasicOperations.GET) {
            invocation = new CacheInvocations.Get(cache, key);
         } else if (operation == BasicOperations.PUT) {
            int size;
            if (entrySize != null && entrySizeRange == null) {
               size = entrySize.next(random);
            } else if (entrySize == null && entrySizeRange != null) {
               size = entrySizeRange.next(ThreadLocalRandom.current());
            } else {
               throw new IllegalArgumentException("No entrySize configured. Please configure either entrySize or entrySizeRange property.");
            }

            invocation = new CacheInvocations.Put(cache, key, valueGenerator.generateValue(key, size, random));
         } else if (operation == BasicOperations.REMOVE) {
            invocation = new CacheInvocations.Remove(cache, key);
         } else {
            throw new IllegalArgumentException(operation.name);
         }

         pipeline.add(invocation);
         if (pipeline.size() == pipelineSize) {
            ExecutePipeline pipelineToExecute = new ExecutePipeline(cache, pipeline);
            stressor.makeRequest(pipelineToExecute);
            pipeline.clear();
         } else {
            // since Radargun records the requests ONLY when the invocation is made through stressor,
            // that means when using pipelining, you will get the actually recorded throughput as <real throughput>/<pipeline depth>
            // It order to hack this around, we execute empty pipeline invocation when not actually having the pipeline ready.
            // This will distort the latency numbers and needs to be fixed, but works for throughput as PoC
            ExecutePipeline dummyPipeline = new ExecutePipeline(cache, null);
            stressor.makeRequest(dummyPipeline);
         }
      }
   }
}
