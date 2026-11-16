# FINAL SORTIE

Früherer Arbeitstitel: **SHATTERWAKE**. Das GitHub-Repository, die Java-Paketstruktur, die Android-App-ID `de.shatterwake.game`, der lokale Speicherschlüssel und die dauerhafte Update-Signatur bleiben aus Kompatibilitätsgründen unverändert.

**Aktuelle Version: 0.6.3-alpha – Umbenennung und neues Android-App-Icon.** Das Spiel selbst, sämtliche Gameplay-Regeln und die bisher freigegebenen 3D-Assets bleiben erhalten.


**Native Android arcade space shooter — FINAL SORTIE (0.6.3-alpha).**

Vertical 2.5D/pseudo-isometric scrolling, automatic primary blaster with a tight correction cone, two selectable touch input modes, distinct enemy flight patterns, destructible segmented space worm, modular multi-stage bosses, star pickups, temporary in-run star-shop upgrades, permanent tech archive unlocks and genuine branching sector paths.

## What's new in version 0.6

- **Boss finale and mothership dock:** A huge multistage final explosion scatters collectible bonus stars, power-ups and an actual technology chip. Existing drops persist. The safe salvage phase only ends once every collectible has been caught or left the screen. A large mothership flies in, opens a glowing hangar and the player ship automatically docks before the Werft appears.
- **Die Sternenwerft:** A large live ship view replaces the former direct-buy list. Choose any available upgrade to animate a smooth zoom onto its affected subsystem. The price and description are shown, but purchase stays disabled until the close-up finishes. **Jetzt kaufen** is a second, explicit action. **Zurueck**, including Android Back, returns to the complete ship view without spending stars.
- A paced **eight-step interactive tutorial** teaches steering, automatic fire, stars, shield, rapid fire, triple shot, plasma, its visible impact on a training target, and homing missiles. **Each of the four power-ups now gets 5 seconds of active testing** after activation, with a visible countdown and real firing/target feedback. Only then does the **WEITER** confirmation unlock the next lesson. The last screen returns to the main menu; training is never launched automatically.
- **Plasma is unambiguously visible:** a pulsing violet aura and reactor on the fighter, thick luminous violet bolts with long trails, and purple impact particles on targets. Its charged HUD tile remains highlighted while active.
- Guided missiles now fire with **one tap**, instead of a hard-to-understand select-then-release sequence, in both control layouts. They must actually destroy a practice target before the tutorial completes.
- Each special ability is first introduced with a paused hint highlighting its real HUD button. Training is safe and does not consume a normal run.
- First-discovery help, boss shop orientation, illustrated sprites, and existing gameplay balance remain available from 0.4.

### Version 0.6 – realistic enemy fleet, fair paths, special charges

- Eight **original Blender-rendered metallic enemy designs**, matching the
  smooth gunmetal hero-fighter aesthetic. Types include the light interceptor,
  raider, reconnaissance needle, heavy carrier, bomber, sentinel, detached
  machine fragment and a distinct rare gold hunter. Blender sources are in
  `art/enemies/build_enemies.py`; every baked sprite is transparent RGBA.
- Fixed, predictable aerial arcs, shallow S turns and one rare complete
  circular loop. **Enemy trajectories do not react to player steering,
  targeting or incoming shots**. Their own firing mechanics are unchanged.
- **Spezialladung costs 100 stars** and adds exactly +1 to **one explicitly
  selected** ability (shield, rapid, triple, plasma), with no three-charge cap.
  At the start of every *sector*, each empty ability is refilled to one;
  remaining quantities carry forward. A completely new run starts with one
  per ability. Separate missiles and EMP are unaffected.
- **Tutorial is available only from the main menu**. Starting a new game
  never launches it automatically, including on first launch. The duplicate
  plasma lesson is removed; the one plasma lesson shows the real effect.
  The training has eight stages and returns to the main menu on completion.
- The 0.5 eSterne display, high-detail hero fighter, repaired armor and
  cinematic Werft remain in place.

## Dauerhaft signierte Android-Updates

Die letzte nachweislich veröffentlichte signierte Fassung vor der Umbenennung war [SHATTERWAKE v0.6.2-alpha](https://github.com/sschermer/SHATTERWAKE/releases/tag/v0.6.2-alpha). Das nächste signierte Update wird als [FINAL SORTIE v0.6.3-alpha](https://github.com/sschermer/SHATTERWAKE/releases/tag/v0.6.3-alpha) veröffentlicht, **sofern** die GitHub Actions einschließlich Signatur-Fingerprintprüfung erfolgreich abschließen.

Die vorhandenen GitHub-Actions-Secrets `SHATTERWAKE_KEYSTORE_BASE64` und `SHATTERWAKE_KEYSTORE_PASSWORD` behalten absichtlich ihre Namen. Sie sind keine sichtbaren Spielnamen und werden nicht umbenannt. Android-Paketkennung, Signaturschlüssel und der persistente Speicherschlüssel bleiben identisch. Der höhere `versionCode` ermöglicht die Update-Installation über die vorhandene signierte Version.

## Installation auf Android

1. [FINAL SORTIE Release v0.6.3-alpha öffnen](https://github.com/sschermer/SHATTERWAKE/releases/tag/v0.6.3-alpha) (nach erfolgreicher Veröffentlichung).
2. `FINAL-SORTIE-v0.6.3-alpha-signed.apk` herunterladen, SHA-256 mit `SHA256SUMS.txt` vergleichen und als Update installieren.
3. **Keine Debug-APK** als Update der signierten Fassung verwenden. Falls das neue Release noch nicht veröffentlicht wurde, bleibt [v0.6.2-alpha](https://github.com/sschermer/SHATTERWAKE/releases/tag/v0.6.2-alpha) die verfügbare signierte Version.

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

### Ship and Werft art pass

- Player and hostile ships now use 384px in-memory procedural renders instead of 192px, with more faceted metal armor, recessed vents and bolts. An entirely separate rare gold star hunter design is generated.
- The Werft and gameplay share the **same permanent modular hardware**: distinct barrel assemblies, armored plates, drone docking cradles, glowing shield emitter housings, magnets, capacitor and targeting turret. A close-up preview shows the prospective hardware at its actual attachment point. Purchased upgrades remain on the playable vessel, not just in the shop.
- Accompanying drone sprites now circle the player's hull in combat instead of remaining in a fixed horizontal row.

## Earlier 3D experiment (superseded)

The initial CC0 Grab3D model was technically integrated but visually rejected.
It has since been replaced by the approved, **original SHATTERWAKE Blender
Hero Fighter** described below. The active Android build no longer downloads
the earlier model and contains no Grab3D-derived fighter sprites. Historical
voluntary credit remains in `THIRD_PARTY_NOTICES.md`.

## Sternenwerft: repair transparency

The repair offer now shows the current hull HP against maximum HP, how many hull points are missing, and how many 1-HP repair purchases are required for 100% integrity. The detail view includes a live hull bar, the current and maximum HP, the cost of one repair and the full repair cost at the current per-point price. Counts and totals update after each purchase. At 100% the repair action is disabled and clearly labeled. These are **currently missing hull points**, not a historical counter of all hits (including shielded damage).

## Verbindlicher Grafik-Neustart

Der **[Grafik-Produktionsstandard ART-001](docs/GRAPHICS_PRODUCTION_STANDARD.md)** und der [maschinenlesbare Hero-Fighter-Vertrag](art/hero_fighter/production_manifest.json) definieren das getrennte Grafikproduktions-/Freigabeverfahren für das neue hochwertige Raumschiff. **Wichtig:** Das derzeitige einfache 3D-Modell ist nicht visuell freigegeben. Neue Render-Assets werden erst nach Abnahme übernommen; die bestehenden Produktionsdateinamen und Modul-IDs bleiben erhalten.

## Hero Fighter ART-004 — integrated Android sprites

The Android app now uses the user-approved, **original project-modeled, smooth
metal Blender hero fighter**, rendered in two cameras. The GAME view is nose-up
with physical muzzle / engine anchors; the WERFT uses a cinematic 3/4 view and
a separately rendered hangar, using camera-projected module focus points in
`rendered/fighter_layout.json`. Transparent PNGs are copied **unchanged** by Git
blob SHA from `art/hero-fighter-v1/art/hero_fighter/staging/`. No second
rendering process, copy-paste edits, or external 3D asset is involved.

All eight ship systems are always offered, including drones and shields. New
modules actually change the hull: magnetic projector tiers (1–3), targeting
sensor tiers (1–5), physical magazines and restored armor. Dark high-damage
cannons replace, rather than overlap, standard silver guns. The simulation
selects the corresponding physical projectile launch points; displayed
thrusters use the geometry-projected exhaust points. Tiered hull overlays map
to missing HP and disappear incrementally on repair. The detail preview
shows the NEXT physical level before purchase. Gameplay and shop each load
their own PNG set through a bounded bitmap cache.

Reference source: [editable Blender master](https://github.com/sschermer/SHATTERWAKE/tree/art/hero-fighter-v1/art/hero_fighter).
