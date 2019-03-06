package org.radargun.service.redis.jedis;

import org.radargun.stages.cache.generators.KeyGenerator;

public class JedisByteArrayKeyGenerator implements KeyGenerator {

   @Override
   public Object generateKey(long keyIndex) {
      return Long.toHexString(keyIndex).getBytes();
   }

}
