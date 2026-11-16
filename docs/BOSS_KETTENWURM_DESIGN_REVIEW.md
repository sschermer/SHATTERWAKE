# Kettenwurm – freigegebene Bossgrafik und Android-Integration (16.11.2026)

**VERBINDLICH/FREIGEGEBEN:** Mk-II polierte Hard-Surface-Metalloptik, vollständiger zwölfgliedriger Boss, vier visuelle Zustände je fünf Typen. Der Nutzer hat alle 20 Schadensdarstellungen zur Integration freigegeben. Die erste Mk-I-Zeichnung unten ist historisch und nicht mehr das gültige Abnahmekriterium.

**IMPLEMENTIERT (Integrationsbranch):** Die 20 originalen Blender-PNGs unter `app/src/main/assets/rendered/boss/`; geometrische Blender-Quelle und editierbare Masterdatei unter `art/bosses/kettenwurm/`; HP-basiertes Sprite-Rendering nur für Boss type 0. Die bestehenden 12 Node-IDs, HP-Hitboxen, Sinusbewegung, Split-Spezialeffekte, Drops, Boss- und Werftsequenz bleiben unverändert.

**SCHADENSVERTRAG:** Zustand 0 bei HP > 2/3, Zustand 1 bis einschließlich 2/3, Zustand 2 bis einschließlich 1/3 bei HP > 0; Zustand 3 bei HP <= 0. Stufe 3 ist eine kurz sichtbare Explosion; anschließend wird die Node weiterhin wie bisher aus dem Rendering entfernt. Explosionspartikel und physische Belohnungen bleiben im GameView.

**BERECHNUNG DER ANIMATION:** Rotation aus der unveränderten Nachbarknoten-Position; visuelle Funken und dezenter Reaktorrauch bei Stufe 2. Nur die Grafik dreht, keine Trefferzone wird verschoben oder vergrößert. Ein zentrierter Ausschnitt aus den 1024-Pixel-Renderings sorgt für bessere Lesbarkeit auf dem Handy; das 256px-Cache-LOD und der 26MB-Cache bestehen fort.

**VERIFIZIERUNG:** Quelle und gepackte Debug-APK werden gegen die SHA256-Prüfsummen der vom Nutzer freigegebenen Renderings geprüft. Reine Java-Regressionen kontrollieren 1/3, 2/3, 0 HP und Ausrichtung. Der unveränderte Signatur-Fingerprint muss weiterhin mit v0.6.1-alpha übereinstimmen. Nur nach erfolgreichem CI-Build von main wird der Release `v0.6.2-alpha` veröffentlicht.

## Historischer Erstentwurf

# Kettenwurm Mk-I — Design review (unapproved)

Status: VORSCHLAG / design preview only. Not integrated into Android.
Base branch reference: main at 630cb29f19876427bc3cfdcbe1a97971ba5699ab (2026-11-16).

## Existing gameplay contract (implemented)
- GameView.Boss(type=0): 12 separately damageable Node objects (IDs 0–11).
- Node type: ID 0 head = type 0; subsequent type = 3 for i%5==0, else 2 for i%4==0, else 4 for i%3==0, else 1.
- Existing world 0 positions: x=210+sin(t*1.2-i*.58)*(68+phase*10), y=155+i*28+sin(t*1.4-i*.42)*7.
- Collision radii and projectile-hit checks stay unchanged; segment removal, side effects, drops and splitting remain unchanged.
- Canvas currently renders procedural node sprites plus overlay geometry; no Blender boss sprites are integrated yet.

## Suggested art direction
Industrial serpentine breaker machine, with wedge-shaped armored sensor/weapon head, overlapping dark titanium/steel armored rings, exposed flexible couplers, sensor glass, embedded gun barrels and warm amber reactor vents. Weathered PBR surfaces; subdued teal service lights and warm orange only for vulnerable systems. The head points upwards. Individual segments rotate subtly along the existing chain tangent, never steering to evade the player.

## Proposed production contract (post approval only)
- Blender source and deterministic build script under art/bosses/kettenwurm/ with camera and procedural materials contained in source files.
- 1024x1024 transparent RGBA sprites per head/segment type, with intact, damaged and destroyed variants; same orthographic projection, illumination and scale for every segment; keep alpha padding consistent.
- World 0 Node IDs remain stable; map art by Node.type and damage state, do not replace collision or reward logic.
- Per-segment tangent angle is derived from existing adjacent Node coordinates. Broken joints visually disconnect; actual mechanic remains the same unless separately approved.
- Save explicit image SHA256, original Blender source and tests for exact sprite dimensions/alpha, matching naming, segment coverage, anchor alignment and performance. No unlicensed third-party art.

## Visual approval gate
The illustrated concept is a **design exploration, not Blender output**. Await explicit user approval before rendering 3D master, committing generated gameplay assets or changing APK. Preview created in local conversation as kettenwurm_konzept_v1.png; not automatically included in repository.
