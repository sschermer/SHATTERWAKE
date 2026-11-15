# SHATTERWAKE

**Native Android arcade space shooter — playable prototype 0.4.0-alpha.**

Vertical 2.5D/pseudo-isometric scrolling, automatic primary blaster with a tight correction cone, two selectable touch input modes, distinct enemy flight patterns, destructible segmented space worm, modular multi-stage bosses, star pickups, temporary in-run star-shop upgrades, permanent tech archive unlocks and genuine branching sector paths.

## What's new in version 0.4

- A five-part **interactive beginner mission** teaches movement, automatic targeting, star collection, using the shield and triggering guided missiles in both input modes.
- On the first acquisition of each ability pickup, the simulation freezes, explains its effect and requires activation using the **real HUD control**. The introductory step has an escape option for accessibility.
- The first boss shop explains stars and upgrade durations; training progress is saved locally and replayable through the main menu.
- HUD now shows illustrated ability/secondary icons, charge counts, and effect countdown bars.
- Fighter, enemy and boss-module artwork now uses original 192 px transparent cached sprites, with faceted hull panels, cockpit, mechanical markings, glow and distinct silhouettes. This is an *asset-pipeline upgrade*, not final commercial art.
- The difficulty settings, star rewards, shop prices and boss entry from 0.3 are unchanged.

## Install on an Android phone

1. Open the latest [GitHub release](https://github.com/sschermer/SHATTERWAKE/releases/tag/v0.4.0-alpha) while signed into the repository-owning account (the repository is private).
2. Download **SHATTERWAKE-v0.4.0-alpha-debug.apk**.
3. Confirm that the SHA-256 of the APK matches `SHA256SUMS.txt` from the release.
4. Open the APK and permit installation from the chosen file manager/browser when Android asks.
5. This is a **debug-signed test APK**. A later build signed with a different debug key may require uninstalling the earlier version first (which deletes the local unlock history).

If the release is unavailable, inspect the [Android APK GitHub Actions workflow](https://github.com/sschermer/SHATTERWAKE/actions/workflows/android.yml) and download the APK artifact from a successful run.

## Local build

- Java 17, Gradle 8.11.1, Android SDK API 36, Android Build Tools 36.0.0.
- From the repository root: `gradle --no-daemon :app:assembleDebug`.
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.
- The project intentionally depends on no external game engine or runtime library.

## Changes in 0.3 alpha

The second alpha overshot toward an easy game: even entire worlds could be cleared without trouble and every shop upgrade could be maxed. Version 0.3 raises a gradually paced enemy challenge while cutting star rewards by roughly 45% from module and enemy drops, slowing ambient star spawns, and repricing upgrades. Bosses now arrive in four distinct states: stop spawning new enemies, wait until existing enemies disappear, **three seconds of blinking WARNING with an original synthesized siren**, then an animated 3.7-second fly-in from above. No collision damage or attacks occur during the entrance, followed by a short grace interval.

Visual work adds original per-world debris and structures, volumetric-style gradients, distant planets and parallax backgrounds. These remain procedural placeholder art rather than final high-fidelity 2.5D models.

## Play

Use your finger to move the ship. Primary blaster fires continuously and corrects direction inside a narrow forward cone. Change secondary control mode under EINSTELLUNGEN; select missile/EMP at lower right and trigger by lifting your moving finger (single-finger), or tap buttons directly while steering with another finger (two-finger). The four bottom icons activate shield, rapid-fire, triple-shot and plasma when charged. Collect gold stars and boss rewards, shop after each boss, and choose actual alternative worlds for the next stage. Bought powerups expire at the end of the run, while discovered boss technologies are saved to your device. You may continue twice by paying half the remaining stars.

## Legal, source and privacy

All ships, enemies, HUD, backgrounds and sound effects are newly drawn/programmatically generated specifically for this game. No scraped graphics, external audio samples or paid asset packs are incorporated. See:

- [`LICENSE`](LICENSE) — original project rights.
- [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) — assets, runtime dependencies, build-tools inventory and notice references.
- [`docs/LICENSING_AND_RELEASE.md`](docs/LICENSING_AND_RELEASE.md) — pre-commercialization verification requirements, exclusions, signing and storefront checklist.
- [`docs/PRIVACY.md`](docs/PRIVACY.md) — no network, no advertising, local settings/progress only.
- [`docs/GAME_DESIGN.md`](docs/GAME_DESIGN.md) — gameplay spec and prototype limitations.

**Commercial release is not automatically legally cleared by writing a license file.** Trademark/name clearance, a real publisher identity, signing and store/legal review remain necessary.
