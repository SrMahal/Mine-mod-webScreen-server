package com.example.client;

import com.example.net.ScreenSyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.minecraft.core.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ScreenClientNet {
   private static final Logger LOGGER = LoggerFactory.getLogger("webscreen-net-client");

   private ScreenClientNet() {
   }

   public static void register() {
      ClientPlayNetworking.registerGlobalReceiver(ScreenSyncPayload.TYPE, (payload, context) -> context.client().execute(() -> apply(payload)));
      LOGGER.info("[webscreen-net] Клиентский приёмник синхронизации зарегистрирован.");
   }

   private static void apply(ScreenSyncPayload p) {
      BlockPos blockpos = p.pos();
      boolean flag = ScreenManager.get(blockpos) != null;
      LOGGER.info("[webscreen-net] ПРИНЯЛ action={} {} (экран есть={})", new Object[]{p.action(), blockpos, flag});
      switch (p.action()) {
         case 0:
            ScreenManager.click(blockpos, p.a(), p.b());
            break;
         case 1:
            ScreenManager.press(blockpos, p.a(), p.b(), p.i1());
            break;
         case 2:
            ScreenManager.release(blockpos, p.a(), p.b(), p.i1());
            break;
         case 3:
            ScreenManager.scroll(blockpos, p.a(), p.b(), p.c());
            break;
         case 4:
            ScreenManager.charTyped(blockpos, p.i1());
            break;
         case 5:
            ScreenManager.keyPress(blockpos, p.i1(), p.i2(), '\uffff');
            break;
         case 6:
            ScreenManager.zoom(blockpos, p.c());
            break;
         case 7:
            ScreenManager.loadUrl(blockpos, p.text());
            break;
         case 9:
            ScreenManager.setMasterVolume(p.a());
            break;
         case 10:
            ScreenManager.setAudioRadius(p.a());
      }
   }

   private static void send(ScreenSyncPayload p) {
      try {
         if (!ClientPlayNetworking.canSend(ScreenSyncPayload.TYPE)) {
            LOGGER.warn("[webscreen-net] НЕ могу отправить action={} {}: сервер не принимает пакет (на сервере/у хоста нет мода 1.0.4?).", p.action(), p.pos());
            return;
         }

         ClientPlayNetworking.send(p);
         LOGGER.info("[webscreen-net] Отправил action={} {}", p.action(), p.pos());
      } catch (Throwable throwable) {
         LOGGER.error("[webscreen-net] Ошибка отправки action={} {}", new Object[]{p.action(), p.pos(), throwable});
      }
   }

   public static void sendClick(BlockPos pos, double u, double v) {
      send(ScreenSyncPayload.click(pos, u, v));
   }

   public static void sendPress(BlockPos pos, double u, double v, int button) {
      send(ScreenSyncPayload.press(pos, u, v, button));
   }

   public static void sendRelease(BlockPos pos, double u, double v, int button) {
      send(ScreenSyncPayload.release(pos, u, v, button));
   }

   public static void sendScroll(BlockPos pos, double u, double v, double scrollY) {
      send(ScreenSyncPayload.scroll(pos, u, v, scrollY));
   }

   public static void sendChar(BlockPos pos, int codepoint) {
      send(ScreenSyncPayload.character(pos, codepoint));
   }

   public static void sendKey(BlockPos pos, int awtVk, int scancode) {
      send(ScreenSyncPayload.key(pos, awtVk, scancode));
   }

   public static void sendZoom(BlockPos pos, double delta) {
      send(ScreenSyncPayload.zoom(pos, delta));
   }

   public static void sendLoadUrl(BlockPos pos, String url) {
      send(ScreenSyncPayload.loadUrl(pos, url));
   }

   public static void requestUrl(BlockPos pos) {
      send(ScreenSyncPayload.request(pos));
   }

   public static void sendVolume(double v) {
      send(ScreenSyncPayload.volume(v));
   }

   public static void sendRadius(double blocks) {
      send(ScreenSyncPayload.radius(blocks));
   }
}
