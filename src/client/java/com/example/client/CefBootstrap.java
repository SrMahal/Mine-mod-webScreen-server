package com.example.client;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import me.friwi.jcefmaven.CefAppBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefDisplayHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CefBootstrap {
   private static final Logger LOGGER = LoggerFactory.getLogger("webscreen-cef");
   private static final File INSTALL_DIR = FabricLoader.getInstance().getGameDir().resolve("webscreen-jcef").toFile();
   // Browser resolution per screen. Each pixel costs memory bandwidth on every repaint and
   // ~2.5x fewer of them makes a screen's first paint (the world-entry spike) much cheaper.
   // Keep the 1.6 aspect ratio that WebBrowserScreen draws with.
   public static final int WIDTH = 800;
   public static final int HEIGHT = 500;
   public static final String DEFAULT_URL = "https://example.com";
   private static CefApp cefApp;
   private static volatile CefClient cefClient;
   // Completes once the engine is downloaded and CEF is up (or failed). The resource-reload
   // listener waits on this so the vanilla loading screen covers the ~350 MB first-run download.
   private static final CompletableFuture<Void> READY = new CompletableFuture<>();

   private CefBootstrap() {
   }

   public static CompletableFuture<Void> ready() {
      return READY;
   }

   public static void initAsync() {
      Thread thread = new Thread(CefBootstrap::init, "webscreen-cef-init");
      thread.setDaemon(true);
      thread.start();
   }

   private static void init() {
      try {
         LOGGER.info("[webscreen] Инициализирую JCEF внутри Minecraft...");
         CefAppBuilder cefappbuilder = new CefAppBuilder();
         cefappbuilder.setInstallDir(INSTALL_DIR);
         cefappbuilder.setProgressHandler((state, percent) -> {
            if (percent >= 0.0F) {
               LOGGER.info("[webscreen] Движок: {} {}%", state, Math.round(percent));
            } else {
               LOGGER.info("[webscreen] Движок: {}", state);
            }
         });
         CefSettings cefsettings = cefappbuilder.getCefSettings();
         cefsettings.windowless_rendering_enabled = true;
         // Unique cache path per game instance. Without it, two Minecraft clients on ONE
         // machine share CEF's singleton and the second instance renders nothing (no onPaint).
         File file = FabricLoader.getInstance().getGameDir().resolve("webscreen-cache").toFile();
         file.mkdirs();
         cefsettings.root_cache_path = file.getAbsolutePath();
         cefsettings.cache_path = file.getAbsolutePath();
         cefappbuilder.addJcefArgs("--disable-gpu", "--no-sandbox", "--off-screen-rendering-enabled");
         cefApp = cefappbuilder.build();
         cefClient = cefApp.createClient();
         cefClient.addDisplayHandler(new CefDisplayHandlerAdapter() {
            @Override
            public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {
               if (frame == null || frame.isMain()) {
                  ScreenManager.onAddressChanged(browser, url);
               }
            }
         });
         LOGGER.info("[webscreen] >>> JCEF ЗАПУЩЕН. Версия: {}", cefApp.getVersion());
      } catch (Throwable throwable) {
         LOGGER.error("[webscreen] >>> JCEF init FAILED", throwable);
      } finally {
         // Always release the loading screen, even if the engine failed to start.
         READY.complete(null);
      }
   }

   public static CefClient client() {
      return cefClient;
   }

   public static void shutdown() {
      try {
         PersonalBrowserManager.shutdown();
         ScreenManager.disposeAll();
         CefClient cefclient = cefClient;
         if (cefclient != null) {
            cefclient.dispose();
            cefClient = null;
         }

         CefApp cefapp = cefApp;
         if (cefapp != null) {
            cefapp.dispose();
            cefApp = null;
         }

         LOGGER.info("[webscreen] CEF остановлен при выходе.");
      } catch (Throwable throwable) {
         LOGGER.error("[webscreen] Ошибка при остановке CEF", throwable);
      }
   }
}
