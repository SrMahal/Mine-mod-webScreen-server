package com.example.client;

import com.example.WebScreenBlock;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.HeadlessOsrBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ScreenManager {
   private static volatile double masterVolume = 1.0;
   private static volatile double audioRadius = 12.0;
   private static final Logger LOGGER = LoggerFactory.getLogger("webscreen-screens");
   private static final Map<BlockPos, ScreenManager.Screen> SCREENS = new ConcurrentHashMap<>();
   private static final Map<BlockPos, String> LAST_URL = new ConcurrentHashMap<>();
   private static final Set<BlockPos> PENDING = ConcurrentHashMap.newKeySet();
   private static final AtomicInteger COUNTER = new AtomicInteger();
   // Screens further than this from the player don't get a browser until approached.
   private static final double CREATE_DISTANCE = 32.0;
   private static final ExecutorService CREATOR = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r, "webscreen-browser-creator");
      thread.setDaemon(true);
      return thread;
   });

   public static double getMasterVolume() {
      return masterVolume;
   }

   public static void setMasterVolume(double v) {
      masterVolume = Math.max(0.0, Math.min(1.0, v));
   }

   public static double getAudioRadius() {
      return audioRadius;
   }

   public static void setAudioRadius(double blocks) {
      audioRadius = Math.max(2.0, blocks);
   }

   private ScreenManager() {
   }

   public static ScreenManager.Screen getOrCreate(BlockPos pos) {
      ScreenManager.Screen screenmanager$screen = SCREENS.get(pos);
      if (screenmanager$screen != null) {
         return screenmanager$screen;
      } else if (!PENDING.isEmpty()) {
         return null;
      } else if (!nearPlayer(pos)) {
         // Spinning up a whole Chromium instance for a screen the player is nowhere near is
         // what makes entering a world with many screens stutter. It gets created on approach.
         return null;
      } else {
         BlockPos blockpos = pos.immutable();
         if (!PENDING.add(blockpos)) {
            return null;
         } else {
            CefClient cefclient = CefBootstrap.client();
            if (cefclient == null) {
               PENDING.remove(blockpos);
               return null;
            } else {
               CREATOR.submit(
                  () -> {
                     try {
                        WebTexture webtexture = new WebTexture("web_" + COUNTER.getAndIncrement());
                        HeadlessOsrBrowser headlessosrbrowser = new HeadlessOsrBrowser(
                           cefclient,
                           CefBootstrap.DEFAULT_URL,
                           CefBootstrap.WIDTH,
                           CefBootstrap.HEIGHT,
                           (buf, w, h) -> webtexture.pushFrame(buf, w, h)
                        );
                        headlessosrbrowser.createImmediately();
                        headlessosrbrowser.setWindowVisibility(true);
                        headlessosrbrowser.setFocus(true);
                        headlessosrbrowser.setWindowlessFrameRate(30);
                        SCREENS.put(blockpos, new ScreenManager.Screen(headlessosrbrowser, webtexture));
                        Minecraft.getInstance().execute(() -> ScreenClientNet.requestUrl(blockpos));
                     } catch (Throwable throwable) {
                        LoggerFactory.getLogger("webscreen-screens").error("[webscreen] Не удалось создать браузер для {}", blockpos, throwable);
                     } finally {
                        try {
                           Thread.sleep(150L);
                        } catch (InterruptedException interruptedexception) {
                        }

                        PENDING.remove(blockpos);
                     }
                  }
               );
               return null;
            }
         }
      }
   }

   public static void click(BlockPos pos, double u, double v) {
      ScreenManager.Screen screenmanager$screen = SCREENS.get(pos);
      if (screenmanager$screen != null) {
         screenmanager$screen.browser.click(px(u), py(v));
      }
   }

   public static void press(BlockPos pos, double u, double v, int button) {
      ScreenManager.Screen screenmanager$screen = SCREENS.get(pos);
      if (screenmanager$screen != null) {
         screenmanager$screen.browser.mousePress(px(u), py(v), button);
      }
   }

   public static void release(BlockPos pos, double u, double v, int button) {
      ScreenManager.Screen screenmanager$screen = SCREENS.get(pos);
      if (screenmanager$screen != null) {
         screenmanager$screen.browser.mouseRelease(px(u), py(v), button);
      }
   }

   public static void move(BlockPos pos, double u, double v) {
      ScreenManager.Screen screenmanager$screen = SCREENS.get(pos);
      if (screenmanager$screen != null) {
         screenmanager$screen.browser.mouseMove(px(u), py(v));
      }
   }

   public static void scroll(BlockPos pos, double u, double v, double scrollY) {
      ScreenManager.Screen screenmanager$screen = SCREENS.get(pos);
      if (screenmanager$screen != null) {
         screenmanager$screen.browser.mouseWheel(px(u), py(v), scrollY);
      }
   }

   public static void charTyped(BlockPos pos, int codepoint) {
      ScreenManager.Screen screenmanager$screen = SCREENS.get(pos);
      if (screenmanager$screen != null) {
         screenmanager$screen.browser.keyTyped((char)codepoint);
      }
   }

   public static void keyPress(BlockPos pos, int awtVk, int scancode, char ch) {
      ScreenManager.Screen screenmanager$screen = SCREENS.get(pos);
      if (screenmanager$screen != null) {
         screenmanager$screen.browser.keyPress(awtVk, scancode, ch);
      }
   }

   public static void zoom(BlockPos pos, double delta) {
      ScreenManager.Screen screenmanager$screen = SCREENS.get(pos);
      if (screenmanager$screen != null) {
         double d0 = screenmanager$screen.browser.getZoomLevel() + delta;
         d0 = Math.max(-3.0, Math.min(4.0, d0));
         screenmanager$screen.browser.setZoomLevel(d0);
      }
   }

   public static void loadUrl(BlockPos pos, String url) {
      ScreenManager.Screen screenmanager$screen = SCREENS.get(pos);
      if (screenmanager$screen != null && url != null && !url.isEmpty()) {
         // If this URL is already our known state for the screen (i.e. the server just echoed
         // back a navigation THIS client reported via onAddressChange), do nothing. Calling
         // loadURL again here re-navigates the browser during its initial load and crashes
         // native CEF. Only genuinely new remote URLs get loaded.
         if (!url.equals(LAST_URL.get(pos))) {
            LAST_URL.put(pos, url);
            screenmanager$screen.browser.loadURL(url);
         }
      }
   }

   // Called from the CEF thread when a browser navigates (link click, redirect, JS, typed URL).
   // Pushes the new URL to the server so every player converges on the same page.
   public static void onAddressChanged(CefBrowser browser, String url) {
      if (url != null && !url.isEmpty() && !"about:blank".equals(url)) {
         BlockPos blockpos = findPos(browser);
         if (blockpos != null && !url.equals(LAST_URL.get(blockpos))) {
            LAST_URL.put(blockpos, url);
            Minecraft.getInstance().execute(() -> ScreenClientNet.sendLoadUrl(blockpos, url));
         }
      }
   }

   private static boolean nearPlayer(BlockPos pos) {
      Minecraft minecraft = Minecraft.getInstance();
      return minecraft.player != null && minecraft.player.blockPosition().closerThan(pos, CREATE_DISTANCE);
   }

   private static BlockPos findPos(CefBrowser browser) {
      for (Entry<BlockPos, ScreenManager.Screen> entry : SCREENS.entrySet()) {
         if (entry.getValue().browser == browser) {
            return entry.getKey();
         }
      }

      return null;
   }

   public static ScreenManager.Screen get(BlockPos pos) {
      return SCREENS.get(pos);
   }

   private static int px(double u) {
      return (int)Math.round(clamp01(u) * (CefBootstrap.WIDTH - 1));
   }

   private static int py(double v) {
      return (int)Math.round(clamp01(v) * (CefBootstrap.HEIGHT - 1));
   }

   public static void updateAudio() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.level != null && minecraft.player != null && !SCREENS.isEmpty()) {
         Vec3 vec3 = minecraft.player.position();
         double d0 = audioRadius;
         double d1 = masterVolume;

         for (Entry<BlockPos, ScreenManager.Screen> entry : SCREENS.entrySet()) {
            BlockPos blockpos = entry.getKey();
            double d2 = vec3.x - (blockpos.getX() + 0.5);
            double d3 = vec3.y - (blockpos.getY() + 0.5);
            double d4 = vec3.z - (blockpos.getZ() + 0.5);
            double d5 = Math.sqrt(d2 * d2 + d3 * d3 + d4 * d4);
            double d6 = clamp01(d5 / d0);
            double d7 = (Math.cos(d6 * Math.PI) + 1.0) / 2.0;
            double d8 = d1 * d7;
            String s = "try{document.querySelectorAll('video,audio').forEach(function(m){m.volume=" + String.format(Locale.US, "%.2f", d8) + ";});}catch(e){}";
            entry.getValue().browser.executeJavaScript(s, "", 0);
         }
      }
   }

   public static void tick() {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.level == null) {
         disposeAll();
      } else {
         for (BlockPos blockpos : SCREENS.keySet()) {
            if (!minecraft.level.isLoaded(blockpos) || !(minecraft.level.getBlockState(blockpos).getBlock() instanceof WebScreenBlock)) {
               ScreenManager.Screen screenmanager$screen = SCREENS.remove(blockpos);
               LAST_URL.remove(blockpos);
               if (screenmanager$screen != null) {
                  try {
                     screenmanager$screen.browser.close(true);
                  } catch (Throwable throwable) {
                  }
               }
            }
         }
      }
   }

   public static void disposeAll() {
      LAST_URL.clear();

      for (BlockPos blockpos : SCREENS.keySet()) {
         ScreenManager.Screen screenmanager$screen = SCREENS.remove(blockpos);
         if (screenmanager$screen != null) {
            try {
               screenmanager$screen.browser.close(true);
            } catch (Throwable throwable) {
            }
         }
      }
   }

   private static double clamp01(double a) {
      return a < 0.0 ? 0.0 : (a > 1.0 ? 1.0 : a);
   }

   public static final class Screen {
      final HeadlessOsrBrowser browser;
      final WebTexture texture;
      double lastVolume = -1.0;

      Screen(HeadlessOsrBrowser browser, WebTexture texture) {
         this.browser = browser;
         this.texture = texture;
      }

      public WebTexture texture() {
         return this.texture;
      }
   }
}
