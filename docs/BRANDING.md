# FINAL SORTIE — brand and Android launcher icon (v0.6.3-alpha)

## Decision and compatibility
The visible game and launcher label are **FINAL SORTIE**, replacing the SHATTERWAKE display name. This is a visual/branding-only change, **not** a new Android application.
Keep the following technical identifiers and update-compatibility guarantees unchanged:
- Android application ID / namespace: `de.shatterwake.game`
- Java class package: `de.shatterwake.game`
- Existing preferences: `shatterwake_save_v1`
- Repository URL and Android signing secrets, private PKCS12 alias, certificate fingerprint.
- All gameplay, player-ship sprites, enemy/boss art and save handling.

## Launcher asset and provenance
- Shipping file: `app/src/main/res/drawable-nodpi/ic_launcher.webp`
- Format: square 384×384 lossy WebP; Android framework decodes it at runtime.
- Creative origin: OpenAI image generation created specifically for this game's newly chosen branding on 2026-11-16. A square launcher area from the generated **FINAL SORTIE** brand draft was cropped, resized with Pillow LANCZOS, and compressed at WebP quality 75. The approved dark hard-surface starfighter/planet/cyan-thruster direction is preserved. No third-party stock images, game screenshots or brand logos were imported.
- Original larger icon preview: `FINAL_SORTIE_launcher_512.png` produced in the development conversation; the committed WebP is the shipped final icon.
- Shipping image SHA-256: `11671c33382e9e6bc8417117d595fb1df7b441d20533ec42423c1cf2d8990170`.
- This is **branding artwork**; it is not a replacement for the reproducible Blender master and gameplay ship textures. All approved original game assets remain unchanged.
- Generated visual output can have similarities to unrelated media. This workflow does not establish exclusive trademark rights. Complete game title/logo trademark clearance is a separate pre-commercialization task.

## Verification
`python3 tests/check_branding.py` checks visible titles, compatibility-safe identifiers, 384px WebP image structure and file hash. In CI, the same script also checks that the launcher graphic is actually packaged inside the debug APK. GitHub Actions creates a signed, compatible APK only with the original secrets and verified signing fingerprint.
