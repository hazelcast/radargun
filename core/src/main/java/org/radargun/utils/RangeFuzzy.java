package org.radargun.utils;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.concurrent.ThreadLocalRandom;

import org.radargun.config.Converter;

/**
 * TODO: add documentaiton
 *
 */
public final class RangeFuzzy implements Serializable {

   private int min;
   private int max;

   private RangeFuzzy(int min, int max) {
      this.min = min;
      this.max = max;
   }

   public static RangeFuzzy uniform(int min, int max) {
      return new RangeFuzzy(min, max);
   }

   public int next(ThreadLocalRandom random) {
      return random.nextInt(min, max + 1);
   }

   @Override
   public String toString() {
      return min + ";" + max;
   }

   public static class RangeFuzzyConverter implements Converter<RangeFuzzy> {
      @Override
      public RangeFuzzy convert(String string, Type type) {
         string = string.trim();
         if (!string.contains(";")) {
            throw new IllegalArgumentException("Range must be in a format min;max");
         }

         String[] parts = string.split(";", 0);
         return new RangeFuzzy(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
      }

      @Override
      public String convertToString(RangeFuzzy value) {
         if (value == null)
            return "null";
         else
            return value.toString();
      }

      @Override
      public String allowedPattern(Type type) {
         return "[0-9.]+;[0-9.]+";
      }

   }
}
