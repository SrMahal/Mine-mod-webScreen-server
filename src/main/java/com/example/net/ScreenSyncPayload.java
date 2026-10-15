package com.example.net;

import com.example.ExampleMod;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

public record ScreenSyncPayload(BlockPos pos, int action, double a, double b, double c, int i1, int i2, String text) implements CustomPacketPayload {
   public static final int CLICK = 0;
   public static final int PRESS = 1;
   public static final int RELEASE = 2;
   public static final int SCROLL = 3;
   public static final int CHAR = 4;
   public static final int KEY = 5;
   public static final int ZOOM = 6;
   public static final int LOAD_URL = 7;
   public static final int REQUEST = 8;
   public static final int SET_VOLUME = 9;
   public static final int SET_RADIUS = 10;
   public static final Type<ScreenSyncPayload> TYPE = new Type<>(ExampleMod.id("screen_sync"));
   public static final StreamCodec<FriendlyByteBuf, ScreenSyncPayload> CODEC = StreamCodec.of(
      (buf, m) -> {
         buf.writeBlockPos(m.pos);
         buf.writeVarInt(m.action);
         buf.writeDouble(m.a);
         buf.writeDouble(m.b);
         buf.writeDouble(m.c);
         buf.writeVarInt(m.i1);
         buf.writeVarInt(m.i2);
         buf.writeUtf(m.text == null ? "" : m.text);
      },
      buf -> new ScreenSyncPayload(
         buf.readBlockPos(), buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readVarInt(), buf.readVarInt(), buf.readUtf()
      )
   );

   @Override
   public Type<ScreenSyncPayload> type() {
      return TYPE;
   }

   public static void register() {
      PayloadTypeRegistry.serverboundPlay().register(TYPE, CODEC);
      PayloadTypeRegistry.clientboundPlay().register(TYPE, CODEC);
   }

   public static ScreenSyncPayload click(BlockPos pos, double u, double v) {
      return new ScreenSyncPayload(pos, 0, u, v, 0.0, 0, 0, "");
   }

   public static ScreenSyncPayload press(BlockPos pos, double u, double v, int button) {
      return new ScreenSyncPayload(pos, 1, u, v, 0.0, button, 0, "");
   }

   public static ScreenSyncPayload release(BlockPos pos, double u, double v, int button) {
      return new ScreenSyncPayload(pos, 2, u, v, 0.0, button, 0, "");
   }

   public static ScreenSyncPayload scroll(BlockPos pos, double u, double v, double scrollY) {
      return new ScreenSyncPayload(pos, 3, u, v, scrollY, 0, 0, "");
   }

   public static ScreenSyncPayload character(BlockPos pos, int codepoint) {
      return new ScreenSyncPayload(pos, 4, 0.0, 0.0, 0.0, codepoint, 0, "");
   }

   public static ScreenSyncPayload key(BlockPos pos, int awtVk, int scancode) {
      return new ScreenSyncPayload(pos, 5, 0.0, 0.0, 0.0, awtVk, scancode, "");
   }

   public static ScreenSyncPayload zoom(BlockPos pos, double delta) {
      return new ScreenSyncPayload(pos, 6, 0.0, 0.0, delta, 0, 0, "");
   }

   public static ScreenSyncPayload loadUrl(BlockPos pos, String url) {
      return new ScreenSyncPayload(pos, 7, 0.0, 0.0, 0.0, 0, 0, url == null ? "" : url);
   }

   public static ScreenSyncPayload request(BlockPos pos) {
      return new ScreenSyncPayload(pos, 8, 0.0, 0.0, 0.0, 0, 0, "");
   }

   public static ScreenSyncPayload volume(double v) {
      return new ScreenSyncPayload(new BlockPos(0, 0, 0), 9, v, 0.0, 0.0, 0, 0, "");
   }

   public static ScreenSyncPayload radius(double blocks) {
      return new ScreenSyncPayload(new BlockPos(0, 0, 0), 10, blocks, 0.0, 0.0, 0, 0, "");
   }
}
