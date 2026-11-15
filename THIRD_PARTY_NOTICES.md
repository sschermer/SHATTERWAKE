# SHATTERWAKE — third-party and asset notices

Inventory date: 2026-11-15. Version: 0.4.0-alpha.

## Assets included in the APK

| Category | Origin | Rights / licensing position |
|---|---|---|
| Game and UI code | Newly written for SHATTERWAKE | Project `LICENSE` |
| Player ship, enemies, worm segments, asteroids, HUD, stars, effects | Original project-drawn Android Canvas geometry and generated alpha-transparent Bitmap sprites (ships and boss modules), including layered nebulae, wrecks, crystals and structures; no stock-art source | Project `LICENSE`; no imported artwork |
| Launcher icon (`ic_launcher.xml`) | Original vector paths | Project `LICENSE`; no imported image |
| Sound effects | PCM synthesised at runtime from elementary sine waves | Original generated output; no imported samples |
| Font | Android device's system sans-serif / sans-serif-condensed | System typeface, not redistributed as font file |
| Text/game names | Original textual content for project | Project `LICENSE`; independent trademark clearance required |

**Runtime dependency inventory:** zero third-party Maven/AAR/JAR application dependencies. The program uses Android framework APIs that are provided by the user's Android operating system. No advertisements, trackers, analytics SDKs, network services or remotely hosted assets are included.

## Build-time tools (not bundled as game content)

1. **Gradle Build Tool 8.11.1:** Apache License, Version 2.0. Copyright Gradle, Inc. Project: https://gradle.org/  License: https://github.com/gradle/gradle/blob/master/LICENSE  Official license info: https://docs.gradle.org/current/userguide/licenses.html
2. **Android Gradle Plugin 8.10.1:** Apache License, Version 2.0 (Android Open Source Project build-system sources); specific POM and transitive artifact notices should be rechecked for each production release. Source: https://android.googlesource.com/platform/tools/base/  License notice: https://android.googlesource.com/platform/tools/base/+/refs/heads/mirror-goog-studio-master-dev/build-system/gradle-core/src/main/java/com/android/build/gradle/BasePlugin.kt
3. **Android SDK, API 36 / Build Tools 36.0.0:** Google's Android SDK License Agreement, with individually open-source-licensed parts where applicable. The SDK is not distributed in the APK. Terms: https://developer.android.com/studio/terms
4. **Temurin OpenJDK 17:** used by CI to compile, not embedded into the APK. License: GPLv2 with Classpath Exception for OpenJDK components; details and notices: https://adoptium.net/licensing/ .
5. **GitHub Actions**: `actions/checkout@v4`, `actions/setup-java@v4`, `gradle/actions/setup-gradle@v4`, and `actions/upload-artifact@v4` run during CI but their implementation is not distributed in the app. Their respective repository licenses and security advisories should be checked when updating CI.

## Apache License 2.0 — reference

For Apache-licensed build tools, see the unmodified complete legal text at:
https://www.apache.org/licenses/LICENSE-2.0.txt

The original Android Gradle Plugin and other build-tool license and NOTICE files reside with their upstream distributions. The project's debug APK **does not package those build tools**. No third-party artwork or binary library is copied into the project under the Apache license.

## Commercial-release audit obligations

- Recalculate the runtime dependency inventory (including transitives) after **every** addition of a library or asset, including artwork, effects, music, fonts and code snippets.
- Keep copyright holder, license URL, version, attribution and distribution conditions for each added component; copy license and NOTICE text into the distribution where required.
- Confirm rights for the *specific recording*, *specific image*, *specific font*, etc. Publicly viewable or royalty-free is **not** automatically permission to sell a game that contains it.
- Open-source components can have patent, copyleft and NOTICE obligations. Obtain a distribution-appropriate professional review before release.
- Do not reuse online images displayed during early concept discussions; **none are in this project**.
- Clear the `SHATTERWAKE` name and game logo for relevant markets and classes before advertising or distribution.
