package com.example.client;

import com.example.ExampleMod;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

/**
 * Holds the vanilla loading screen until the Chromium engine is ready.
 *
 * <p>On a first run jcefmaven downloads ~350 MB. Without this the download raced with world
 * loading and the game looked frozen with no feedback. Blocking the resource reload instead
 * keeps that work on the startup progress bar, where waiting is expected.
 */
public final class CefReloadListener implements IdentifiableResourceReloadListener {
   private static final long TIMEOUT_MINUTES = 20L;

   @Override
   public Identifier getFabricId() {
      return ExampleMod.id("cef_bootstrap");
   }

   @Override
   public CompletableFuture<Void> reload(
      PreparableReloadListener.SharedState state,
      Executor prepareExecutor,
      PreparableReloadListener.PreparationBarrier barrier,
      Executor applyExecutor
   ) {
      // copy() so the timeout never completes the shared future the rest of the mod waits on.
      return CefBootstrap.ready()
         .copy()
         .completeOnTimeout(null, TIMEOUT_MINUTES, TimeUnit.MINUTES)
         .thenCompose(barrier::wait)
         .thenRunAsync(() -> {
         }, applyExecutor);
   }
}
