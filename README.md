# SHATTERWAKE

**Native Android arcade space shooter — playable prototype 0.5.0-alpha.**

Vertical 2.5D/pseudo-isometric scrolling, automatic primary blaster with a tight correction cone, two selectable touch input modes, distinct enemy flight patterns, destructible segmented space worm, modular multi-stage bosses, star pickups, temporary in-run star-shop upgrades, permanent tech archive unlocks and genuine branching sector paths.

## What's new in version 0.5

- **Boss finale and mothership dock:** A huge multistage final explosion scatters collectible bonus stars, power-ups and an actual technology chip. Existing drops persist. The safe salvage phase only ends once every collectible has been caught or left the screen. A large mothership flies in, opens a glowing hangar and the player ship automatically docks before the Werft appears.
- **Die Sternenwerft:** A large live ship view replaces the former direct-buy list. Choose any available upgrade to animate a smooth zoom onto its affected subsystem. The price and description are shown, but purchase stays disabled until the close-up finishes. **Jetzt kaufen** is a second, explicit action. **Zurueck**, including Android Back, returns to the complete ship view without spending stars.
- A paced **nine-step interactive tutorial** teaches steering, automatic fire, stars, shield, rapid fire, triple shot, plasma, its visible impact on a training target, and homing missiles. **Each of the four power-ups now gets a full 15 seconds of active testing** after activation, with a visible countdown and real firing/target feedback. Only then does the **WEITER** confirmation unlock the next lesson. The last screen requires explicit confirmation to start sector 1.
- **Plasma is unambiguously visible:** a pulsing violet aura and reactor on the fighter, thick luminous violet bolts with long trails, and purple impact particles on targets. Its charged HUD tile remains highlighted while active.
- Guided missiles now fire with **one tap**, instead of a hard-to-understand select-then-release sequence, in both control layouts. They must actually destroy a practice target before the tutorial completes.
- Each special ability is first introduced with a paused hint highlighting its real HUD button. Training is safe and does not consume a normal run.
- First-discovery help, boss shop orientation, illustrated sprites, and existing gameplay balance remain available from 0.4.

## Install on an Android phone

1. Open the latest [GitHub release](https://github.com/sschermer/SHATTERWAKE/releases/tag/v0.5.0-alpha) while signed into the repository-owning account (the repository is private).
2. Download **SHATTERWAKE-v0.5.0-alpha-debug.apk**.
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

Use your finger to move the ship. Primary blaster fires continuously and corrects direction inside a narrow forward cone. Under EINSTELLUNGEN you can choose one-hand or two-finger steering. Tap missile or EMP once to trigger it in either mode, including while steering with another finger. The four bottom icons activate shield, rapid-fire, triple-shot and plasma when charged. Collect gold stars and boss rewards, shop after each boss, and choose actual alternative worlds for the next stage. Bought powerups expire at the end of the run, while discovered boss technologies are saved to your device. You may continue twice by paying half the remaining stars.

## Legal, source and privacy

All ships, enemies, HUD, backgrounds and sound effects are newly drawn/programmatically generated specifically for this game. No scraped graphics, external audio samples or paid asset packs are incorporated. See:

- [`LICENSE`](LICENSE) — original project rights.
- [`THIRD_PARTY_NOTICES.md`](THIRD_PARTY_NOTICES.md) — assets, runtime dependencies, build-tools inventory and notice references.
- [`docs/LICENSING_AND_RELEASE.md`](docs/LICENSING_AND_RELEASE.md) — pre-commercialization verification requirements, exclusions, signing and storefront checklist.
- [`docs/PRIVACY.md`](docs/PRIVACY.md) — no network, no advertising, local settings/progress only.
- [`docs/GAME_DESIGN.md`](docs/GAME_DESIGN.md) — gameplay spec and prototype limitations.

**Commercial release is not automatically legally cleared by writing a license file.** Trademark/name clearance, a real publisher identity, signing and store/legal review remain necessary.

### Flight and targeting update

- Four training power-ups now run for five active seconds; shield bullets visibly come from a hostile craft.
- Shooting is strictly straight ahead at the start. Optional five-stage Werft target correction improves it by 3% per upgrade, to 15%.
- Enemy movement uses banked smooth attack paths. A rare golden Sternenjaeger makes a full looping circuit and awards a large star haul.
