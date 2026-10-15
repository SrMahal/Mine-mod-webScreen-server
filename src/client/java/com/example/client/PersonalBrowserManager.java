package com.example.client;

import java.util.concurrent.atomic.AtomicBoolean;
import org.cef.browser.HeadlessOsrBrowser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Private browser in this Minecraft client; never participates in multiplayer screen sync. */
public final class PersonalBrowserManager {
    private static final Logger LOG = LoggerFactory.getLogger("webscreen-personal");
    public static final String HOME = "https://minecraft.3eb.site/";
    private static final AtomicBoolean CREATING = new AtomicBoolean();
    private static volatile HeadlessOsrBrowser browser;
    private static volatile WebTexture texture;
    private PersonalBrowserManager() {}

    public static void ensureStarted() {
        if (browser != null || CefBootstrap.client() == null || !CREATING.compareAndSet(false, true)) return;
        Thread thread = new Thread(() -> {
            try {
                WebTexture t = new WebTexture("personal_browser");
                HeadlessOsrBrowser b = new HeadlessOsrBrowser(CefBootstrap.client(), HOME,
                    CefBootstrap.WIDTH, CefBootstrap.HEIGHT, t::pushFrame);
                b.createImmediately();
                b.setWindowVisibility(true);
                b.setWindowlessFrameRate(30);
                texture = t;
                browser = b;
            } catch (Throwable e) {
                LOG.error("Could not create private WebScreen browser", e);
            } finally {
                CREATING.set(false);
            }
        }, "webscreen-personal-browser");
        thread.setDaemon(true);
        thread.start();
    }

    public static WebTexture texture() { return texture; }
    public static HeadlessOsrBrowser browser() { return browser; }

    public static void focus(boolean focused) {
        HeadlessOsrBrowser b = browser;
        if (b != null) b.setFocus(focused);
    }
    public static void load(String url) {
        HeadlessOsrBrowser b = browser;
        if (b != null && url != null && !url.isBlank()) b.loadURL(url);
    }
    public static void shutdown() {
        HeadlessOsrBrowser b = browser;
        browser = null;
        texture = null;
        if (b != null) try { b.close(true); } catch (Throwable e) { LOG.warn("Closing personal browser", e); }
    }
}