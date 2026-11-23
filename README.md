# Compose PCM SFX — Pure-Kotlin Procedural Sound Effects for Android & Jetpack Compose

[![JitPack](https://jitpack.io/v/simple-free-apps/compose-pcm-sfx.svg)](https://jitpack.io/#simple-free-apps/compose-pcm-sfx)

A lightweight, **MIT-licensed**, zero-dependency procedural 16-bit PCM sound effect synthesizer and `AudioTrack` player for Kotlin, Android, and Jetpack Compose apps and games — **with zero bundled `.mp3`, `.ogg`, or `.wav` files**.

## Why Procedural PCM Audio?
- **Zero Audio Licensing Risk:** Avoid third-party stock audio license restrictions or Content ID mismatches when publishing apps, games, or open-source templates.
- **Tiny Footprint:** Replaces megabytes of bundled audio files with ~250 lines of pure Kotlin math (`PcmSynthesizer.kt`).
- **Pure-JVM Unit Testable:** `PcmSynthesizer` has zero Android framework dependencies, so waveform generation, pitch scaling, clipping bounds, and tail fades run directly on JVM JUnit tests.
- **9 Built-In Presets + Custom Synthesis:** `TAP`, `CONFIRM`, `ERROR`, `CHIME`, `VICTORY`, `COIN`, `LASER`, `CARD_FLIP`, and `POP` (`SINE`, `TRIANGLE`, `SQUARE_SOFT`, `WHITE_NOISE`, harmonics, and ADSR envelopes).
- **Interactive Sample App:** Bilingual English & Arabic (RTL) Jetpack Compose studio app (`:app`) with live `Canvas` waveform bar visualizer, pitch multiplier (`0.5x–2.0x`), and master volume controls.

## Gradle Installation (JitPack)

Add the JitPack repository in `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Then add the dependency in your module `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.simple-free-apps:compose-pcm-sfx:v1.0.1")
}
```

## Quick Usage (`:pcm-sfx`)

```kotlin
// 1) Initialize once (thread-safe, caches preset ShortArray buffers on demand)
val sfx = AndroidSfxPlayer()

// 2) Play any built-in preset
sfx.play(SfxPreset.CONFIRM)
sfx.play(SfxPreset.VICTORY, pitchMultiplier = 1.15)

// 3) Or synthesize custom PCM buffers directly (pure JVM testable)
val customPcm = PcmSynthesizer.synthesize(
    SfxSpec(
        durationMs = 90,
        startHz = 520.0,
        endHz = 980.0,
        waveform = WaveformType.TRIANGLE,
        gain = 0.28,
    )
)
sfx.playPcm(customPcm)

// 4) Downsample for Jetpack Compose Canvas waveform preview
val bars: List<Float> = PcmSynthesizer.peakNormalizedWaveformPreview(customPcm, buckets = 44)
```

## Download Prebuilt Demo APK
Download the interactive Jetpack Compose sample app APK from **[Releases](https://github.com/simple-free-apps/compose-pcm-sfx/releases)**.

## Build & Run Unit Tests
Requires Android Studio / Gradle 9.6 / JDK 17 / Android SDK 37:

```bash
./gradlew :pcm-sfx:testDebugUnitTest :app:testDebugUnitTest :app:assembleDebug
```

---

## Try Interactive Game Demos & Production Android Source Kits

We use this exact zero-asset `AudioTrack` synthesis approach in our open-source starter and offer full commercial Android game projects on **Codester** and **SellAnyCode**:

- **[Compare All 10 Source Kits + Live Tests](https://simple-free-apps.github.io/source-kits/?utm_source=compose-pcm-sfx&utm_medium=readme&utm_campaign=matrix)** — Side-by-side levels, engine stack, 8 Google Play live builds, and 16:9 video walkthroughs.
- **[Play Interactive Browser Puzzle Demos](https://simple-free-apps.github.io/demo/?utm_source=compose-pcm-sfx&utm_medium=readme&utm_campaign=live_demo)** — Try Liquid Sort, LanternLoom, and Kivorivane mechanics directly in your browser.
- **[ChromaCircuit Starter (Free 5-Level Compose Puzzle Repo)](https://github.com/simple-free-apps/android-compose-puzzle-starter)** — Real-world game starter demonstrating procedural `AudioTrack` SFX + Compose Canvas.
- **Soravel: Puzzle Pack 5-in-1** — 5 full Jetpack Compose puzzle games (`1,928+` levels): **[Codester · $14](https://www.codester.com/items/76186/puzzle-pack-5-in-1-kotlin-android-game-bundle?ref=Gr8Man)** · **[SellAnyCode · $29](https://www.sellanycode.com/item.php?id=28630)** · **[16:9 Demo Video](https://www.youtube.com/watch?v=XFTPVgakCII)**.
- **Liquid Sort Puzzle** — Volumetric water-sort puzzle: **[Codester · $14](https://www.codester.com/items/64630/liquid-sort-puzzle-kotlin-android-game-template?ref=Gr8Man)** · **[SellAnyCode · $20](https://www.sellanycode.com/item.php?id=28631)** · **[16:9 Video](https://www.youtube.com/watch?v=X3z5HdDfmYY)** · **[Google Play](https://play.google.com/store/apps/details?id=com.gr8.games.liquidsortpuzzle)**.
- **Kivorivane (1,200 Kite-Escape Levels)** — **[Codester · $14](https://www.codester.com/items/76093/kivorivane-1000-levels-kite-escape-kotlin?ref=Gr8Man)** · **[Google Play](https://play.google.com/store/apps/details?id=com.mirveki.savureth.kivorivane)** · **[16:9 Video](https://www.youtube.com/watch?v=1FEwvlkDMkw)**.
- **Orvaleth (Godot 4 3D Clockwork Puzzle)** — **[Codester · $14](https://www.codester.com/items/76144/orvaleth-godot-3d-clockwork-puzzle-game?ref=Gr8Man)** · **[SellAnyCode · $20](https://www.sellanycode.com/item.php?id=28633)** · **[Google Play](https://play.google.com/store/apps/details?id=com.valkora.serenth.orvaleth)**.
- **Veyravel (Godot 4 3D Action Roguelite)** — **[Codester · $14](https://www.codester.com/items/76079/veyravel-3d-android-action-game-godot?ref=Gr8Man)** · **[SellAnyCode · $20](https://www.sellanycode.com/item.php?id=28634)**.
- **[Full Gr8 Apps & Games Studio Catalog (19 Live Apps on Google Play)](https://play.google.com/store/apps/dev?id=7078150569870376778)** · **[Studio Portal](https://simple-free-apps.github.io/?utm_source=compose-pcm-sfx&utm_medium=readme&utm_campaign=catalog)**.

*Disclosure: Codester links include the publisher's `?ref=Gr8Man` referral code.*

## License
Distributed under the [MIT License](LICENSE).
