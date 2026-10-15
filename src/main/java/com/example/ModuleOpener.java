package com.example;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Field;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ModuleOpener {
   private static final Logger LOGGER = LoggerFactory.getLogger("webscreen-modules");

   private ModuleOpener() {
   }

   static void openAwt() {
      try {
         Field field = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
         field.setAccessible(true);
         Object object = field.get(null);
         Field field1 = Lookup.class.getDeclaredField("IMPL_LOOKUP");
         Object object1 = invoke(object, "staticFieldBase", Field.class, field1);
         long i = (Long)invoke(object, "staticFieldOffset", Field.class, field1);
         Lookup lookup = (Lookup)invokeGetObject(object, object1, i);
         MethodHandle methodhandle = lookup.findVirtual(Module.class, "implAddOpensToAllUnnamed", MethodType.methodType(void.class, String.class));
         Module module = Class.forName("java.awt.GraphicsEnvironment").getModule();
         methodhandle.invoke((Module)module, (String)"java.awt");
         methodhandle.invoke((Module)module, (String)"java.awt.event");
         LOGGER.info("[webscreen] java.desktop открыт в рантайме — JVM-аргументы не нужны.");
      } catch (Throwable throwable) {
         LOGGER.warn(
            "[webscreen] Не удалось открыть java.desktop автоматически. Если экран не появится, добавьте JVM-аргументы из JVM-аргументы.txt.", throwable
         );
      }
   }

   private static Object invoke(Object unsafe, String method, Class<?> argType, Object arg) throws Exception {
      return unsafe.getClass().getMethod(method, argType).invoke(unsafe, arg);
   }

   private static Object invokeGetObject(Object unsafe, Object base, long offset) throws Exception {
      return unsafe.getClass().getMethod("getObject", Object.class, long.class).invoke(unsafe, base, offset);
   }
}
