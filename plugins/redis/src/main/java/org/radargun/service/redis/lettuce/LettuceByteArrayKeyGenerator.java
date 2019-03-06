package org.radargun.service.redis.lettuce;

import org.radargun.stages.cache.generators.KeyGenerator;

public class LettuceByteArrayKeyGenerator implements KeyGenerator {

   @Override
   public Object generateKey(long keyIndex) {
      return Long.toHexString(keyIndex).getBytes();
   }

}
