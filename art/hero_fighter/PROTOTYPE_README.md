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
