# WebScreen

A real web browser on a block, right inside Minecraft. WebScreen renders a full
Chromium browser (via JCEF) onto a screen block — click links, type, scroll, and
browse actual websites in-game.

## Features

- **Real browser** — powered by Chromium (JCEF), not a fake mockup.
- **Full interaction** — click links, type, scroll, zoom (Ctrl+scroll), and an
  address bar with search.
- **Remote control** — right-click a screen with the Remote item to open the
  browser menu.
- **Multi-block screens** — place screens in a grid to build one seamless wall.
- **Per-screen audio** — volume and falloff-radius sliders; sound fades with
  distance.
- **Multiplayer** — browse together. Navigation, input, and audio settings sync
  between players.

## Requirements

- Minecraft **26.2**, **Fabric** loader, **Fabric API**, **Java 25**.
- On first launch the mod downloads the Chromium engine (~200 MB) automatically.

## Usage

1. `/give @s webscreen:web_screen` and `/give @s webscreen:web_remote` (or craft them).
2. Place the screen block facing you.
3. Right-click it with the **Remote** to open the browser, type a URL, press Enter.

## Building

Requires JDK 25.

```bash
./gradlew build
```

The plain (thin) jar is produced in `build/libs/`. Distribution builds bundle the
JCEF Java bindings; the native Chromium engine is downloaded at runtime, not
shipped in the jar.

## License

[MIT](LICENSE)

## Notes

Each player runs their own browser instance; pages sync by URL and input, not by
pixels, so live/dynamic content may differ slightly between players.


## StudioCraft experimental branch (personal Chromium)

Press **C** while in the world to open a local Chromium browser, and **Esc** to return to the game. The browser stays active while Minecraft remains running. The starting page is `https://minecraft.3eb.site/`. This private browser never sends player navigation through ScreenClientNet. The multiplayer block displays retain their existing architecture.

Development branch only: `feat/personal-chromium-c`. Build requires **Java 25**, **Fabric Loom**, and network access to resolve dependencies. The extracted .class files in this repository are kept for reference, and are NOT the source tree used for Gradle. A clean local build and gameplay validation are still required before releasing the mod.

The wrapper launcher scripts are imported from upstream; `gradle/wrapper/gradle-wrapper.jar` was not imported because the connector cannot read that binary artifact. To build, install Gradle compatible with the configured Loom plugin, then run `gradle build` using Java 25, or restore an official Gradle wrapper JAR from a trusted distribution.
