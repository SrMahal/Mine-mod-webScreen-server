package com.example.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import org.cef.browser.HeadlessOsrBrowser;

/** Fullscreen private browser opened with C; inputs never use ScreenClientNet. */
public final class PersonalBrowserScreen extends Screen {
    private static final int TOP = 30;
    private EditBox address;
    public PersonalBrowserScreen() { super(Component.literal("StudioCraft Browser")); }
    @Override public boolean isPauseScreen() { return false; }

    @Override protected void init() {
        address = new EditBox(font, 12, 6, width - 24, 18, Component.literal("URL"));
        address.setMaxLength(2048);
        HeadlessOsrBrowser b = PersonalBrowserManager.browser();
        address.setValue(b != null && b.getURL() != null ? b.getURL() : PersonalBrowserManager.HOME);
        addRenderableWidget(address);
        PersonalBrowserManager.ensureStarted();
        PersonalBrowserManager.focus(true);
    }
    @Override public void onClose() {
        PersonalBrowserManager.focus(false);
        super.onClose();
    }
    private int[] viewport() { return new int[]{10, TOP, Math.max(1, width-20), Math.max(1, height-TOP-10)}; }
    private double[] point(double x, double y) {
        int[] r = viewport();
        return x >= r[0] && y >= r[1] && x < r[0]+r[2] && y < r[1]+r[3]
            ? new double[]{(x-r[0])/r[2], (y-r[1])/r[3]} : null;
    }
    private int px(double u) { return (int)Math.round(u * (CefBootstrap.WIDTH - 1)); }
    private int py(double v) { return (int)Math.round(v * (CefBootstrap.HEIGHT - 1)); }
    @Override public void extractRenderState(GuiGraphicsExtractor g, int x, int y, float delta) {
        super.extractRenderState(g, x, y, delta);
        PersonalBrowserManager.ensureStarted();
        WebTexture t = PersonalBrowserManager.texture();
        if (t != null) {
            t.uploadIfDirty();
            if (t.hasFrame()) {
                int[] r = viewport();
                g.blit(RenderPipelines.GUI_TEXTURED, t.id(), r[0], r[1], 0.0f, 0.0f,
                    r[2], r[3], r[2], r[3], -1);
            }
        }
    }
    @Override public boolean mouseClicked(MouseButtonEvent e, boolean doubleClick) {
        double[] p = point(e.x(), e.y());
        HeadlessOsrBrowser b = PersonalBrowserManager.browser();
        if (p == null || b == null) return super.mouseClicked(e, doubleClick);
        address.setFocused(false);
        b.mousePress(px(p[0]), py(p[1]), e.button());
        return true;
    }
    @Override public boolean mouseReleased(MouseButtonEvent e) {
        double[] p = point(e.x(), e.y());
        HeadlessOsrBrowser b = PersonalBrowserManager.browser();
        if (p == null || b == null) return super.mouseReleased(e);
        b.mouseRelease(px(p[0]), py(p[1]), e.button());
        return true;
    }
    @Override public void mouseMoved(double x, double y) {
        double[] p = point(x, y);
        HeadlessOsrBrowser b = PersonalBrowserManager.browser();
        if (p != null && b != null) b.mouseMove(px(p[0]), py(p[1]));
    }
    @Override public boolean mouseScrolled(double x, double y, double sx, double sy) {
        double[] p = point(x, y);
        HeadlessOsrBrowser b = PersonalBrowserManager.browser();
        if (p == null || b == null) return super.mouseScrolled(x, y, sx, sy);
        b.mouseWheel(px(p[0]), py(p[1]), sy);
        return true;
    }
    @Override public boolean keyPressed(KeyEvent e) {
        if (e.key() == 256) return super.keyPressed(e);
        if (address.isFocused()) {
            if (e.key() == 257 || e.key() == 335) {
                String value = address.getValue().trim();
                if (!value.isEmpty()) PersonalBrowserManager.load(
                    value.startsWith("https://") || value.startsWith("http://") ? value
                        : value.contains(".") && !value.contains(" ") ? "https://" + value
                        : "https://duckduckgo.com/?q=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
                address.setFocused(false);
                return true;
            }
            return super.keyPressed(e);
        }
        HeadlessOsrBrowser b = PersonalBrowserManager.browser();
        if (b != null) {
            int vk = switch (e.key()) {
                case 257, 335 -> 13; case 258 -> 9; case 259 -> 8; case 261 -> 46;
                case 262 -> 39; case 263 -> 37; case 264 -> 40; case 265 -> 38;
                case 266 -> 33; case 267 -> 34; case 268 -> 36; case 269 -> 35;
                default -> -1;
            };
            if (vk != -1) b.keyPress(vk, e.scancode(), '\uffff');
        }
        return true;
    }
    @Override public boolean charTyped(CharacterEvent e) {
        if (address.isFocused()) return super.charTyped(e);
        HeadlessOsrBrowser b = PersonalBrowserManager.browser();
        if (b != null) b.keyTyped((char)e.codepoint());
        return true;
    }
}