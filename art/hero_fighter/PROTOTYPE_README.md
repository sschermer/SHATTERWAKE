# SHATTERWAKE — original hero fighter art branch

Run: `blender -b -t 4 --python art/hero_fighter/build_master.py -- art/hero_fighter`

This is the first genuinely separate **editable Blender master**, not an Android
sprite and not an illustration pretending to be a 3D model.

Review:
- `previews/werft_before.png`
- `previews/werft_weapons.png`
- `previews/werft_shield.png`
- `previews/werft_drones.png`
- `previews/game_nose_up.png`

`staging/game`: transparent 1024px base + independent modules 0,4,5.
`staging/werft`: transparent 2048px base + independent modules 0,4,5.
The actual Blender source is `hero_fighter.blend`.

All imagery on this isolated branch is a **visual prototype, not yet approved**.
Android code and the latest APK on `main` are unchanged by this workflow.

## ART-002 – Glatte Metalloberflächen (16.11.2026)

Der Nutzer wünscht ausdrücklich **keine Körnung**. Das Master-Skript erzeugt deshalb jetzt **keine Noise-Texturen, keine prozedurale Base-Color-/Roughness-Marmorierung und kein Noise-Bump-Mapping**. Auch die mikrofeinen Scratch-Geometrien wurden entfernt. Die Werkstoffwirkung entsteht über glatte Metallschichten, Metall/Rauheit pro Bauteil, Schatten, Kanten und echte Fugen/Schrauben. Cycles rendert mit 64 Samples und Denoising, um auch verbleibendes Samplingrauschen zu reduzieren. Für den Handybildschirm haben saubere, ruhige Flächen Vorrang.

## ART-003: Erhöhter Waffenschaden

Der Waffenschaden (ID 1) bekommt genau die gleiche Kanonengeometrie wie Schnellfeuer (ID 0), nur in glattem dunklem Gunmetal. Beide Perspektiven exportieren `fighter_module_1.png` im identischen Koordinatensystem. Die Varianten ersetzen einander optisch und dürfen nicht übereinandergelegt werden. **Darstellungspriorität:** `damageLevel>0 → ID 1`, sonst `fireLevel>0 → ID 0`. Ihre Spielwerte bleiben unabhängig. Vorschau: `previews/werft_damage.jpg`.
