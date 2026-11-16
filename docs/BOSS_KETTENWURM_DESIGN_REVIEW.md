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
