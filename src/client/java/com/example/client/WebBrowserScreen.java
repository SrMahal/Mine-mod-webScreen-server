package com.example.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class WebBrowserScreen extends Screen {
   private static final double ASPECT = 1.6;
   private static final int TOP = 28;
   private final BlockPos pos;
   private EditBox addressBar;
   private static final int MIN_R = 4;
   private static final int MAX_R = 48;

   public WebBrowserScreen(BlockPos pos) {
      super(Component.literal("Web Screen"));
      this.pos = pos;
   }

   @Override
   public boolean isPauseScreen() {
      return false;
   }

   @Override
   protected void init() {
      int i = 18;
      int j = 5;
      int k = 5;
      int l = 10;
      int i1 = 150;
      int j1 = this.width - l - i1;
      int k1 = j1 - k - i1;
      int l1 = Math.max(120, k1 - k - l);
      this.addressBar = new EditBox(this.font, l, j, l1, i, Component.literal("URL"));
      this.addressBar.setMaxLength(2000);
      this.addressBar.setValue("https://example.com");
      this.addRenderableWidget(this.addressBar);
      this.addRenderableWidget(new WebBrowserScreen.VolumeSlider(k1, j, i1, i));
      this.addRenderableWidget(new WebBrowserScreen.RadiusSlider(j1, j, i1, i));
   }

   private int[] rect() {
      int i = this.width - 20;
      int j = this.height - 28 - 6;
      int k = i;
      int l = (int)(i / 1.6);
      if (l > j) {
         l = j;
         k = (int)(j * 1.6);
      }

      return new int[]{(this.width - k) / 2, 28 + (j - l) / 2, k, l};
   }

   private double[] toPage(double mx, double my) {
      int[] aint = this.rect();
      return !(mx < aint[0]) && !(mx > aint[0] + aint[2]) && !(my < aint[1]) && !(my > aint[1] + aint[3])
         ? new double[]{(mx - aint[0]) / aint[2], (my - aint[1]) / aint[3]}
         : null;
   }

   private boolean barFocused() {
      return this.addressBar != null && this.addressBar.isFocused();
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
      super.extractRenderState(g, mouseX, mouseY, partialTick);
      if (this.barFocused() && System.currentTimeMillis() / 500L % 2L == 0L) {
         String s = this.addressBar.getValue();
         int i = Math.min(this.addressBar.getCursorPosition(), s.length());
         int j = this.addressBar.getX() + 4 + this.font.width(s.substring(0, i));
         int k = this.addressBar.getY() + 3;
         int l = this.addressBar.getY() + this.addressBar.getHeight() - 3;
         g.fill(j, k, j + 1, l, -1);
      }

      ScreenManager.Screen screenmanager$screen = ScreenManager.getOrCreate(this.pos);
      if (screenmanager$screen != null) {
         WebTexture webtexture = screenmanager$screen.texture();
         webtexture.uploadIfDirty();
         if (webtexture.hasFrame()) {
            int[] aint = this.rect();
            g.blit(RenderPipelines.GUI_TEXTURED, webtexture.id(), aint[0], aint[1], 0.0F, 0.0F, aint[2], aint[3], aint[2], aint[3], -1);
         }
      }
   }

   @Override
   public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
      double[] adouble = this.toPage(e.x(), e.y());
      if (adouble != null) {
         if (this.addressBar != null) {
            this.addressBar.setFocused(false);
         }

         ScreenClientNet.sendPress(this.pos, adouble[0], adouble[1], e.button());
         return true;
      } else {
         return super.mouseClicked(e, doubleClick);
      }
   }

   @Override
   public boolean mouseReleased(MouseButtonEvent e) {
      double[] adouble = this.toPage(e.x(), e.y());
      if (adouble != null) {
         ScreenClientNet.sendRelease(this.pos, adouble[0], adouble[1], e.button());
         return true;
      } else {
         return super.mouseReleased(e);
      }
   }

   @Override
   public void mouseMoved(double mx, double my) {
      double[] adouble = this.toPage(mx, my);
      if (adouble != null) {
         ScreenManager.move(this.pos, adouble[0], adouble[1]);
      }
   }

   @Override
   public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
      double[] adouble = this.toPage(mx, my);
      if (adouble != null) {
         if (ctrlDown()) {
            ScreenClientNet.sendZoom(this.pos, scrollY > 0.0 ? 0.25 : -0.25);
         } else {
            ScreenClientNet.sendScroll(this.pos, adouble[0], adouble[1], scrollY);
         }

         return true;
      } else {
         return super.mouseScrolled(mx, my, scrollX, scrollY);
      }
   }

   private static boolean ctrlDown() {
      Window window = Minecraft.getInstance().getWindow();
      return InputConstants.isKeyDown(window, 341) || InputConstants.isKeyDown(window, 345);
   }

   @Override
   public boolean keyPressed(KeyEvent e) {
      if (e.key() == 256) {
         return super.keyPressed(e);
      } else if (this.barFocused()) {
         if (e.key() != 257 && e.key() != 335) {
            return super.keyPressed(e);
         } else {
            ScreenClientNet.sendLoadUrl(this.pos, normalizeUrl(this.addressBar.getValue()));
            this.addressBar.setFocused(false);
            return true;
         }
      } else {
         int i = toAwtVk(e.key());
         if (i != -1) {
            ScreenClientNet.sendKey(this.pos, i, e.scancode());
         }

         return true;
      }
   }

   @Override
   public boolean charTyped(CharacterEvent e) {
      if (this.barFocused()) {
         return super.charTyped(e);
      } else {
         ScreenClientNet.sendChar(this.pos, e.codepoint());
         return true;
      }
   }

   private static String normalizeUrl(String s) {
      s = s.trim();
      if (s.isEmpty()) {
         return "https://example.com";
      } else if (s.startsWith("http://") || s.startsWith("https://")) {
         return s;
      } else {
         return s.contains(".") && !s.contains(" ") ? "https://" + s : "https://duckduckgo.com/?q=" + URLEncoder.encode(s, StandardCharsets.UTF_8);
      }
   }

   private static int toAwtVk(int glfw) {
      return switch (glfw) {
         case 257, 335 -> 13;
         case 258 -> 9;
         case 259 -> 8;
         case 261 -> 46;
         case 262 -> 39;
         case 263 -> 37;
         case 264 -> 40;
         case 265 -> 38;
         case 266 -> 33;
         case 267 -> 34;
         case 268 -> 36;
         case 269 -> 35;
         default -> -1;
      };
   }

   private static final class RadiusSlider extends AbstractSliderButton {
      RadiusSlider(int x, int y, int w, int h) {
         super(x, y, w, h, Component.empty(), (ScreenManager.getAudioRadius() - 4.0) / 44.0);
         this.updateMessage();
      }

      private int blocks() {
         return (int)Math.round(4.0 + this.value * 44.0);
      }

      @Override
      protected void updateMessage() {
         this.setMessage(Component.literal("Радиус: " + this.blocks() + " бл."));
      }

      @Override
      protected void applyValue() {
         ScreenManager.setAudioRadius(this.blocks());
      }

      @Override
      public void onRelease(MouseButtonEvent e) {
         super.onRelease(e);
         ScreenClientNet.sendRadius(this.blocks());
      }
   }

   private static final class VolumeSlider extends AbstractSliderButton {
      VolumeSlider(int x, int y, int w, int h) {
         super(x, y, w, h, Component.empty(), ScreenManager.getMasterVolume());
         this.updateMessage();
      }

      @Override
      protected void updateMessage() {
         this.setMessage(Component.literal("Звук: " + (int)Math.round(this.value * 100.0) + "%"));
      }

      @Override
      protected void applyValue() {
         ScreenManager.setMasterVolume(this.value);
      }

      @Override
      public void onRelease(MouseButtonEvent e) {
         super.onRelease(e);
         ScreenClientNet.sendVolume(this.value);
      }
   }
}
