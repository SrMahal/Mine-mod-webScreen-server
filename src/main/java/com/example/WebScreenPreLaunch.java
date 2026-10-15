package com.example;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebScreenPreLaunch implements PreLaunchEntrypoint {
   private static final Logger LOGGER = LoggerFactory.getLogger("webscreen-prelaunch");

   public void onPreLaunch() {
      ModuleOpener.openAwt();
      System.setProperty("java.awt.headless", "false");

      try {
         Field field = GraphicsEnvironment.class.getDeclaredField("headless");
         field.setAccessible(true);
         field.set(null, Boolean.FALSE);
         LOGGER.info("[webscreen] AWT headless сброшен. isHeadless={}", GraphicsEnvironment.isHeadless());
      } catch (Throwable throwable) {
         LOGGER.error("[webscreen] Не удалось сбросить headless через рефлексию", throwable);
      }
   }
}
