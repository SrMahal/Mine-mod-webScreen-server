package com.example;

import net.minecraft.core.BlockPos;

public final class WebScreenInput {
   public static final double BEZEL = 0.0625;
   private static volatile WebScreenInput.ClickHandler handler;
   private static volatile WebScreenInput.MenuOpener menuOpener;

   private WebScreenInput() {
   }

   public static void setHandler(WebScreenInput.ClickHandler h) {
      handler = h;
   }

   public static void click(BlockPos pos, double u, double v) {
      WebScreenInput.ClickHandler webscreeninput$clickhandler = handler;
      if (webscreeninput$clickhandler != null) {
         webscreeninput$clickhandler.onClick(pos, u, v);
      }
   }

   public static void setMenuOpener(WebScreenInput.MenuOpener o) {
      menuOpener = o;
   }

   public static void openMenu(BlockPos pos) {
      WebScreenInput.MenuOpener webscreeninput$menuopener = menuOpener;
      if (webscreeninput$menuopener != null) {
         webscreeninput$menuopener.open(pos);
      }
   }

   public interface ClickHandler {
      void onClick(BlockPos var1, double var2, double var4);
   }

   public interface MenuOpener {
      void open(BlockPos var1);
   }
}
