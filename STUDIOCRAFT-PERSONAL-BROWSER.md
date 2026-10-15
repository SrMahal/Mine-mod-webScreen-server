# StudioCraft Personal Browser — implementation branch

This feature reuses **the same JCEF engine**, but not the same browser instance or BlockPos-based multiplayer sync. Press C in-game to open a local GUI and Esc to dismiss. The browser stays alive for the Minecraft process; shutdown happens before CefBootstrap shuts down. The landing page is https://minecraft.3eb.site/.

The original repository is an unpacked binary (no build system). The source files under src/ are based on https://github.com/wiliel-maker/webscreen (MIT); the unpacked binary files are retained unchanged. **Do not deploy until a Gradle build and client runtime tests have passed.**

Test two local clients with different login accounts; confirm private navigation never calls ScreenClientNet; confirm block displays still work, no browser is created in the dedicated server, and C can be rebound to avoid collision.
