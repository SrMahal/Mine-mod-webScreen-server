package org.cef.browser;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import org.cef.CefBrowserSettings;
import org.cef.CefClient;

public class HeadlessOsrBrowser extends CefBrowserOsr {
   private final int viewWidth;
   private final int viewHeight;
   private final HeadlessOsrBrowser.PaintSink sink;
   private static Field RAW_CODE;
   private static Field SCANCODE;

   public HeadlessOsrBrowser(CefClient client, String url, int width, int height, HeadlessOsrBrowser.PaintSink sink) {
      super(client, url, false, CefRequestContext.getGlobalContext(), new CefBrowserSettings());
      this.viewWidth = width;
      this.viewHeight = height;
      this.sink = sink;
   }

   @Override
   public Rectangle getViewRect(CefBrowser browser) {
      return new Rectangle(0, 0, this.viewWidth, this.viewHeight);
   }

   @Override
   public void onPaint(CefBrowser browser, boolean popup, Rectangle[] dirtyRects, ByteBuffer buffer, int width, int height) {
      if (!popup && this.sink != null && buffer != null && width > 0 && height > 0) {
         this.sink.onFrame(buffer, width, height);
      }
   }

   private Component src() {
      return this.getUIComponent();
   }

   private static int awtButton(int mcButton) {
      return switch (mcButton) {
         case 1 -> 3;
         case 2 -> 2;
         default -> 1;
      };
   }

   private static int awtMask(int mcButton) {
      return switch (mcButton) {
         case 1 -> 4096;
         case 2 -> 2048;
         default -> 1024;
      };
   }

   public void click(int x, int y) {
      this.mouseMove(x, y);
      this.mousePress(x, y, 0);
      this.mouseRelease(x, y, 0);
   }

   public void mouseMove(int x, int y) {
      long i = System.currentTimeMillis();
      this.sendMouseEvent(new MouseEvent(this.src(), 503, i, 0, x, y, 0, false));
   }

   public void mousePress(int x, int y, int mcButton) {
      long i = System.currentTimeMillis();
      this.sendMouseEvent(new MouseEvent(this.src(), 501, i, awtMask(mcButton), x, y, 1, false, awtButton(mcButton)));
   }

   public void mouseRelease(int x, int y, int mcButton) {
      long i = System.currentTimeMillis();
      this.sendMouseEvent(new MouseEvent(this.src(), 502, i, 0, x, y, 1, false, awtButton(mcButton)));
   }

   public void mouseWheel(int x, int y, double scrollY) {
      long i = System.currentTimeMillis();
      int j = (int)Math.round(scrollY * 3.0);
      if (j == 0) {
         j = scrollY > 0.0 ? 3 : -3;
      }

      this.sendMouseWheelEvent(new MouseWheelEvent(this.src(), 507, i, 0, x, y, 0, false, 0, 30, j));
   }

   private static KeyEvent mkKey(Component src, int id, int vk, char ch, int scancode) {
      KeyEvent keyevent = new KeyEvent(src, id, System.currentTimeMillis(), 0, vk, ch);
      if (RAW_CODE != null && vk != 0) {
         try {
            RAW_CODE.setLong(keyevent, vk);
            SCANCODE.setLong(keyevent, scancode);
         } catch (Throwable throwable) {
         }
      }

      return keyevent;
   }

   public void keyTyped(char c) {
      this.sendKeyEvent(mkKey(this.src(), 400, 0, c, 0));
   }

   public void keyPress(int awtVk, int scancode, char ch) {
      this.sendKeyEvent(mkKey(this.src(), 401, awtVk, '\uffff', scancode));
      if (ch != '\uffff') {
         this.sendKeyEvent(mkKey(this.src(), 400, 0, ch, 0));
      }

      this.sendKeyEvent(mkKey(this.src(), 402, awtVk, '\uffff', scancode));
   }

   static {
      try {
         RAW_CODE = KeyEvent.class.getDeclaredField("rawCode");
         RAW_CODE.setAccessible(true);
         SCANCODE = KeyEvent.class.getDeclaredField("scancode");
         SCANCODE.setAccessible(true);
      } catch (Throwable throwable) {
      }
   }

   public interface PaintSink {
      void onFrame(ByteBuffer var1, int var2, int var3);
   }
}
