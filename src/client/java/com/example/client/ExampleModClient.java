package com.example.client;

import com.example.WebScreenBlocks;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;
import com.example.WebScreenInput;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStopping;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.EndTick;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.PackType;

public class ExampleModClient implements ClientModInitializer {
   private static int tickCounter;
   private static KeyMapping personalBrowserKey;

   public void onInitializeClient() {
      CefBootstrap.initAsync();
      personalBrowserKey = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.webscreen.personal", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.categories.webscreen"));
      // Keep the ~350 MB first-run engine download on the startup loading screen
      // instead of letting it race with world loading.
      ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new CefReloadListener());
      BlockEntityRendererRegistry.register(WebScreenBlocks.WEB_SCREEN_BE, WebScreenRenderer::new);
      ScreenClientNet.register();
      WebScreenInput.setHandler(ScreenClientNet::sendClick);
      WebScreenInput.setMenuOpener(pos -> Minecraft.getInstance().setScreenAndShow(new WebBrowserScreen(pos)));
      ClientLifecycleEvents.CLIENT_STOPPING.register((ClientStopping)client -> { PersonalBrowserManager.shutdown(); CefBootstrap.shutdown(); });
      ClientTickEvents.END_CLIENT_TICK.register((EndTick)client -> {
         while (personalBrowserKey.consumeClick()) {
            if (client.screen == null) client.setScreenAndShow(new PersonalBrowserScreen());
         }
         if (tickCounter % 5 == 0) {
            ScreenManager.updateAudio();
         }

         if (++tickCounter >= 40) {
            tickCounter = 0;
            ScreenManager.tick();
         }
      });
   }
}
