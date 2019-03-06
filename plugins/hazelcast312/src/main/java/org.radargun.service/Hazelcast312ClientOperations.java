package org.radargun.service;

import org.radargun.service.Hazelcast312Operations.HazelcastPipelinedCache;
import org.radargun.traits.PipelinedOperations;

/**
 * Hazelcast client-server operations are the same as "library mode" operations
 *
 * @author Jiri Holusa (jiri@hazelcast.com)
 */

public class Hazelcast312ClientOperations extends Hazelcast37ClientOperations implements PipelinedOperations {

   public Hazelcast312ClientOperations(Hazelcast312ClientService service) {
      super(service);
   }

   @Override
   public <K, V> HazelcastPipelinedCache<K, V> getCache(String cacheName) {
      return new Hazelcast312Operations.Cache<K, V>(service.<K, V>getMap(cacheName));
   }
}
