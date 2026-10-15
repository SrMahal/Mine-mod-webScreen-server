package com.example;

import com.example.net.ScreenServerNet;
import com.example.net.ScreenSyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
   public static final String MOD_ID = "webscreen";
   public static final Logger LOGGER = LoggerFactory.getLogger("webscreen");

   public void onInitialize() {
      LOGGER.info("Hello Fabric world!");
      WebScreenBlocks.register();
      ScreenSyncPayload.register();
      ScreenServerNet.register();
      ServerLifecycleEvents.SERVER_STOPPED.register((ServerStopped)server -> ScreenServerNet.clear());
   }

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("webscreen", path);
   }
}
