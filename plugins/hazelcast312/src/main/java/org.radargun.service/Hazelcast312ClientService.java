package org.radargun.service;

import org.radargun.Service;
import org.radargun.traits.ProvidesTrait;

/**
 * Hazelcast client service
 *
 * @author Roman Macor &lt;rmacor@redhat.com&gt;
 */

@Service(doc = "Hazelcast client")
public class Hazelcast312ClientService extends Hazelcast37ClientService {

   @ProvidesTrait
   @Override
   public Hazelcast312ClientOperations createOperations() {
      return new Hazelcast312ClientOperations(this);
   }

   @ProvidesTrait
   @Override
   public Hazelcast312ClientService getSelf() {
      return this;
   }

}
