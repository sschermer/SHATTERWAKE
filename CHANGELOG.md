## 0.6.4-alpha — optionale Offline-Sprachbefehle (16.11.2026)

- Führe eine abschaltbare Push-to-Talk-Steuerung für die vier bestehenden Spezialfähigkeiten ein: Schild, Turbo/Schnellfeuer, Dreifach und Plasma.
- Ein zweiter Finger im Spielfeld startet eine einzige kurze lokale Mikrofon-Sitzung, während der erste Finger das Schiff weitersteuert. Rakete/EMP und Power-up-Touchbuttons bleiben vollständig bedienbar.
- Nutze ausschließlich Androids lokale Spracherkennung ab API 31, mit Deutsch (de-DE); keine Internetberechtigung, Online-Rückfallebene, Cloud-Analyse, Audiomitschnitte oder zusätzlichen Libraries.
- RECORD_AUDIO wird ausschließlich bei freiwilliger Aktivierung in den Einstellungen angefragt. Bei fehlendem lokalen Erkennungsdienst oder verweigerter Berechtigung bleiben die normalen Touch-Steuerungen nutzbar.
- Automatischer Timeout und Lifecycle-Abbruch bei Verlassen/Pausieren der App. Keine Sprachsteuerung in Menüs, der Werft oder während des Tutorials.
- Ergänze Java-Grammatik-, Falschaktivierungs-, Multitouch- und APK-Berechtigungstests. Alter Android-Paketname, Speicherstand, permanent signierender PKCS12-Schlüssel und sämtliche bisherigen Assets bleiben identisch.
- Veröffentliche eine separate signierte v0.6.4-alpha-APK; v0.6.3-alpha bleibt als vorangegangene Testversion abrufbar.

## 0.6.3-alpha — FINAL SORTIE: Namenswechsel und App-Icon (2026-11-16)

- **VERBINDLICH:** Der sichtbare Spielname lautet ab jetzt **FINAL SORTIE** (früher SHATTERWAKE).
- Neues originales, für das Android-Launcherformat zugeschnittenes und optimiertes Icon mit FINAL-SORTIE-Schriftzug und Raumjäger; Quellenherkunft in `docs/BRANDING.md` dokumentiert.
- Android-App-Label, Hauptmenü-Logo, HUD, Endbildschirm und Info-/Lizenzansicht aktualisiert; keine Änderung an Spiellogik, Raumschiff-/Bossgrafiken oder Steuerung.
- **Updatekompatibilität:** Paket `de.shatterwake.game`, Java-Pakete, `shatterwake_save_v1`, GitHub-Repository und vorhandener Signaturschlüssel bleiben unverändert.
- Version `0.6.3-alpha`, höherer `versionCode`, separates signiertes GitHub-Release `v0.6.3-alpha`; neue Branding-Regressionsprüfung für Quellcode und APK.
- Debug-Build bleibt ausschließlich eine CI-Prüfung, keine veröffentlichte Update-APK.

## 0.6.2-alpha — Kettenwurm Bossgrafik und modulare Schadensstufen (2026-11-16)

- Integriere die 20 vom Nutzer visuell freigegebenen originalen 1024-RGBA-Blender-Sprites für den ersten Boss: fünf Bauteiltypen und vier Schadenszustände (0 intakt, 1 verbeult, 2 stark beschädigt mit Funken, 3 Explosion).
- Jedes der zwölf Segmente wählt seine Grafik anhand der eigenen HP (Schwellen: 2/3, 1/3, 0), behält seine vorhandenen Trefferzonen, Typ-IDs und Gameplay-Wirkungen.
- Kettenwurm-Grafiken folgen der vorhandenen vorhersehbaren Sinusbahn; kurze Funken- und Explosionseffekte laufen ohne neue Schussausweich-KI.
- Bewahre die ursprüngliche Android-Canvas-Bosslogik der anderen Welten, Sternen-/Tech-Drops, Werft, Tutorial und beide Steuerungsarten.
- Füge die editierbare Blender-Masterdatei, reproduzierbare Blender-Erzeuger und SHA256-Nachweise hinzu; prüfe alle 20 Assets in Quellen und tatsächlich gebauter APK.
- Signierter Update-Build behält die PKCS12-Zertifikatsidentität, Paketkennung und SharedPreferences bei; neues, eigenständiges GitHub-Prerelease v0.6.2-alpha (die bisherige v0.6.1-alpha bleibt erhalten).
- Debug-APK nur als CI-Regressionstest, niemals als installierbares Update veröffentlichen.

## 0.6.0-alpha — Blender enemy fleet, fair flight and clearer upgrades

- Eight unique original metallic 3D hostile ship models, transparent renders,
  including rare gold star hunter and mechanical fragment, authored in Blender.
- Replace earlier colorful procedural enemies in Android; keep procedural
  sprite fallback if an asset is ever missing.
- Deterministic smooth arcs independent of player position and shooting:
  **no targeted avoidance**; rare readable looping enemy only.
- New special charge economy: 100 stars, ONE selected ability gains +1,
  unlimited stack size, sector-start zero-to-one replenishment.
- Optional tutorial only from the main menu, never auto-starts on first game;
  one plasma lesson rather than two; eight training steps.
- Pure-Java tests for special charge economy, tutorial and all eight flight paths.
- APK artifact renamed to v0.6.0-alpha; all original enemy RGBA files verified.

## Integrated original Blender art and physical Werft upgrades — 2026-11-16
- Replace rejected Grab3D ship on Android with the visually approved ART-004
  original Blender fighter in 1024px GAME and 2048px WERFT perspective.
- Add an independently rendered shipyard background; remove toy-like grid.
- Use actual 3D-projected muzzle, engine and per-module zoom anchors.
- Draw all eight real ship systems, magnetic coil levels, targeting levels
  and the dark *replacement* damage-gun finish.
- Render missing-HP hull damage and repair-preview state; preserve exact
  one-HP-per-purchase economy and second-step confirmation.
- Show drone and shield offers together. Cache bitmaps at reduced decoding
  size with bounded memory to avoid Android heap pressure.
- Test deterministic visual selection, tier bounds and damage mapping.

## 0.5.0-alpha — understandable training and high-visibility plasma

- Boss final component now triggers a full-screen scale multi-stage detonation with shockwaves, secondary blasts, module wreckage and tangible collectible bonus stars, ability drops and technology chip. Do not enter the Werft while any loot remains in reach; chip technology must be physically collected to unlock.
- After salvage, a mothership approaches and the player ship flies into its illuminated hangar. Die Sternenwerft shows a large modular fighter, a 2-column offer overview and an animated subsystem-specific zoom. Require a separate, post-animation explicit purchase; Back exits preview without spending.
- Replace abruptly finishing five-step onboarding with nine guided tasks and a deliberate WEITER checkpoint after every success.
- Give each of the four power-ups 15 seconds of real, counted, playable training before offering the next lesson; include a timer/progress bar, durable firing targets and harmless shield-test shots. Keep real-run durations unchanged.
- Add hands-on rapid-fire, triple-shot and plasma lessons. Plasma must destroy a visible stationary training enemy; missile lessons likewise end only after a practice kill.
- Trigger missiles and EMP on a single tap in both control modes, removing the ambiguous arm-then-release interaction.
- Give active plasma a pulsing violet ship aura/reactor, high-contrast wide projectiles and visible purple impact particles. A status label accompanies the active HUD icon.
- Retain replay/skip behavior, persisted discovery tips, existing enemy balancing and safe tutorial simulation.

# Changes

## 0.4.0-alpha — 2026-11-16
- Added a guided first-play mission (steering, targeting, star pickup, shield activation and secondary weapon use).
- Persist tutorial completion and first-explanation state; replay tutorial from the main menu.
- Pause simulation to explain each newly collected ability, requiring correct action at the original HUD control. Skip is accessible when needed.
- Added first-shop orientation, ability and secondary glyphs, and active timer bars.
- Replaced original simplified hull geometry with own transparent high-resolution cached sprites for fighter, six enemy classes and five boss module types. Added surface paneling, cockpit glazing, engine nozzles and lighting.
- Gameplay difficulty and existing boss entrance untouched.
- Updated test/build pipeline with pure-Java tutorial state-machine regression tests; no third-party assets or runtime libraries.


## 0.3.0-alpha — 2026-11-15
- Corrected excessive ease of v0.2 with moderate enemy and projectile pressure; 9-second opening grace, faster spawn and gradual projectile caps.
- Lower star rewards and much slower passive star supply; rebalanced progressive shop prices to prevent all-max upgrades by end of run.
- Boss entrance sequence: stop new spawns; wait for enemy field to clear; purge old hostile shots; blink centered boss warning with a 3-second synthesized siren; animate boss from above over 3.7 seconds.
- Boss collision and attacks disabled during fly-in; short protection grace at start of combat.
- Additional original pseudo-isometric stage art: parallax clouds, wreckage, machine frameworks, glowing crystals, storm towers and citadel structures.
- Additional deterministic regression tests for boss transition and star economy.
- No external music, textures or third-party game libraries added.


## 0.2.0-alpha — 2026-11-15
- Player-reported balance regression: first run ended in approximately 30 seconds.
- Opening 18 seconds: enemy projectiles disabled; next 37 seconds: at most three simultaneously active hostile shots.
- Progressive wave sizes and caps; fewer, slower enemies; fewer enemy bullets; longer fire intervals.
- Increased starting hull from five to six and post-hit protection from 1.5 to 2.4 seconds.
- Reduced boss volley sizes and frequencies; made opening boss less durable.
- Added multi-layered gradient-shaded craft, enemy and boss components, improved engine glows, debris and projectile effects.
- 26 pure-Java game-rule checks including early-game pacing.
- Continued procedural original artwork, no external asset packages.


## 0.1.0-alpha — 2026-11-15
- Native Java/Android Canvas arcade playable prototype.
- Procedural pseudo-isometric art and generated audio, no third-party runtime libraries.
- Five distinct worlds, three stages per run, two route choices per run.
- Multiple enemies, 12-piece segmented worm and four alternative multi-stage bosses.
- Automatic aim correction, two control schemes, missile/EMP and four on-demand abilities.
- Star economy, post-boss shop, technology archive, score and save, two paid continues.
- Legal inventory, licensing and privacy documents; GitHub Actions debug APK build.

### Combat motion and training follow-up

- Five-second active power-up trials, visible hostile craft firing in shield tutorial.
- Remove free auto-aim; five upgrade tiers at 3% each, up to 15% correction.
- Curved enemy attack paths, banked turning and rare golden looping star-rich fighter.

### Modular visual overhaul

- High-resolution procedural ship atlas with richer industrial shading, vents, bolts and a distinct gold elite silhouette.
- Real painted upgrade modules fitted directly to the hull and shared between the close-up Werft and gameplay: gun upgrades, armor repair panels, shield emitters, drone docks, targeting turret, magnet and capacitor.
- Orbiting drone movement replaces the old fixed companion line.

### Sternenwerft hull repair information

- Repair overview tile displays current HP and number of 1-HP repairs needed to reach full integrity.
- Repair detail shows hull percentage, missing HP, remaining repair steps, individual repair price and combined full-repair price.
- Each purchase updates the displays instantly; repairing a full hull remains disabled. Added Java regression assertions for missing-HP and repair-cost calculations.
