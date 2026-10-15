package com.example.net;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ScreenServerNet {
   private static final Logger LOGGER = LoggerFactory.getLogger("webscreen-net-server");
   private static final Map<BlockPos, String> SCREEN_URLS = new ConcurrentHashMap<>();
   // Global audio settings, server-authoritative so every player shares one volume/radius.
   private static volatile double volume = 1.0;
   private static volatile double radius = 12.0;

   private ScreenServerNet() {
   }

   public static void register() {
      ServerPlayNetworking.registerGlobalReceiver(ScreenSyncPayload.TYPE, (payload, context) -> {
         ServerPlayer serverplayer = context.player();
         context.server().execute(() -> handle(serverplayer, payload));
      });
      LOGGER.info("[webscreen-net] Серверный приёмник синхронизации зарегистрирован.");
   }

   private static void handle(ServerPlayer sender, ScreenSyncPayload p) {
      BlockPos blockpos = p.pos().immutable();
      if (p.action() == 8) {
         String s = SCREEN_URLS.get(blockpos);
         LOGGER.info("[webscreen-net] REQUEST {} от {} -> url={}", new Object[]{blockpos, sender.getName().getString(), s});
         if (s != null && !s.isEmpty()) {
            ServerPlayNetworking.send(sender, ScreenSyncPayload.loadUrl(blockpos, s));
         }

         // Bring the joining player's audio settings in line with everyone else.
         ServerPlayNetworking.send(sender, ScreenSyncPayload.volume(volume));
         ServerPlayNetworking.send(sender, ScreenSyncPayload.radius(radius));
      } else {
         if (p.action() == 7) {
            SCREEN_URLS.put(blockpos, p.text());
         } else if (p.action() == 9) {
            volume = p.a();
         } else if (p.action() == 10) {
            radius = p.a();
         }

         Collection<ServerPlayer> collection = sender.level().getServer().getPlayerList().getPlayers();
         LOGGER.info(
            "[webscreen-net] action={} {} от {} -> рассылаю {} игрокам", new Object[]{p.action(), blockpos, sender.getName().getString(), collection.size()}
         );

         for (ServerPlayer serverplayer : collection) {
            ServerPlayNetworking.send(serverplayer, p);
         }
      }
   }

   public static void clear() {
      SCREEN_URLS.clear();
      volume = 1.0;
      radius = 12.0;
   }
}
