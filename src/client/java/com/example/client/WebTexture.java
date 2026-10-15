package com.example.client;

import com.mojang.blaze3d.platform.NativeImage;
import java.nio.ByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WebTexture {
   private static final Logger LOGGER = LoggerFactory.getLogger("webscreen-tex");
   private final Identifier id;
   private final Object lock = new Object();
   private boolean loggedFirstFrame;
   private boolean loggedFirstUpload;
   private byte[] frame;
   private int frameW;
   private int frameH;
   private boolean dirty;
   private NativeImage image;
   private DynamicTexture texture;
   private int texW;
   private int texH;
   private boolean registered;

   public WebTexture(String path) {
      this.id = Identifier.fromNamespaceAndPath("webscreen", path);
   }

   public Identifier id() {
      return this.id;
   }

   public int width() {
      return this.texW;
   }

   public int height() {
      return this.texH;
   }

   public boolean hasFrame() {
      return this.registered && this.texW > 0;
   }

   public void pushFrame(ByteBuffer bgra, int w, int h) {
      synchronized (this.lock) {
         int i = w * h * 4;
         if (this.frame == null || this.frame.length != i) {
            this.frame = new byte[i];
         }

         bgra.rewind();
         bgra.get(this.frame, 0, i);
         this.frameW = w;
         this.frameH = h;
         this.dirty = true;
         if (!this.loggedFirstFrame) {
            this.loggedFirstFrame = true;
            LOGGER.info("[webscreen] >>> ПЕРВЫЙ кадр onPaint в игре: {}x{}", w, h);
         }
      }
   }

   public void uploadIfDirty() {
      byte[] abyte;
      int i;
      int j;
      synchronized (this.lock) {
         if (!this.dirty || this.frame == null) {
            return;
         }

         i = this.frameW;
         j = this.frameH;
         abyte = (byte[])this.frame.clone();
         this.dirty = false;
      }

      try {
         if (this.image == null || this.texW != i || this.texH != j) {
            if (this.texture != null) {
               this.texture.close();
            }

            this.image = new NativeImage(i, j, false);
            this.texture = new DynamicTexture(() -> "webscreen", this.image);
            this.texW = i;
            this.texH = j;
            Minecraft.getInstance().getTextureManager().register(this.id, this.texture);
            this.registered = true;
            LOGGER.info("[webscreen] >>> Текстура создана и зарегистрирована: {} ({}x{})", new Object[]{this.id, i, j});
         }

         ByteBuffer bytebuffer = this.image.getPixelBytes();
         int k = i * j;

         for (int l = 0; l < k; l++) {
            int i1 = l << 2;
            bytebuffer.put(i1, abyte[i1 + 2]);
            bytebuffer.put(i1 + 1, abyte[i1 + 1]);
            bytebuffer.put(i1 + 2, abyte[i1]);
            bytebuffer.put(i1 + 3, abyte[i1 + 3]);
         }

         this.texture.upload();
         if (!this.loggedFirstUpload) {
            this.loggedFirstUpload = true;
            LOGGER.info("[webscreen] >>> ПЕРВАЯ заливка текстуры выполнена ({}x{})", i, j);
         }
      } catch (Throwable throwable) {
         LOGGER.error("[webscreen] >>> Ошибка заливки текстуры", throwable);
      }
   }
}
