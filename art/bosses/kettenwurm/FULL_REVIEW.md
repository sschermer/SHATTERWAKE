# Kettenwurm — vollständige visuelle Abnahme (Mk-II-Basis)

Status: Vollständiger Blender-Boss als Vorschau GETESTET; NICHT visuell freigegeben; NICHT in Android integriert. Stand: 16. November 2026.

## Verbindlicher Ausgangspunkt
- Vorläufig akzeptierte gestalterische Basis: Mk-II Kopf/Einzelsegment, poliertes Titan/Stahl, dunkelrote Metallpanzerung, ohne Noise/Bump.
- In der Android-App existieren zwölf Knoten (ID 0 bis 11), fünf Typen, individuelle Trefferzonen und fest berechnete Sinuspositionen.
- Diese Gameplay-Logik, Hitboxen, Angriffe, Splitting, Drops und Belohnungen bleiben unverändert.

## Originale und echte Render-Dateien
- build_mk2.py: vorläufig akzeptierte Kopf-/Segmentquelle.
- build_full_worm.py: Original-Blender-Erzeuger für zwölf Knoten, Gelenke und fünf mechanische Varianten.
- full_output/kettenwurm_full_master.blend: editierbare 3D-Masterszene mit beiden Zuständen.
- full_output/review_contact_sheet.jpg: vier Ansichten auf einem Blatt.
- full_output/full_worm_game_intact.png und full_worm_game_damaged.png: identische orthografische GAME-Kamera.
- full_output/full_worm_hero_intact.png und full_worm_hero_damaged.png: gleiche 3D-Kamera mit verschiedenen Zuständen.
- full_output/segment_damage_detail.png: Detail zerstörter Verbindung.
- full_output/manifest.json und SHA256SUMS.txt: exakte Node-Typen, Metadaten und Prüfsummen.

## Exakte Bauteiltypen
- ID 0: Kopf / Typ 0.
- IDs 1, 2, 7, 11: Waffen / Typ 1.
- IDs 4, 8: Trennkupplung / Typ 2.
- IDs 5, 10: Reaktorkern / Typ 3.
- IDs 3, 6, 9: Energiekapazitor / Typ 4.

## Was die Vorschau belegt
- Statische Geometrie und die aus GameView.positionBossNodes übernommene Sinuspose.
- Originale mehrteilige Poliermetall-Geometrie und funktional unterscheidbare Bauteile.
- Separater beschädigter Zustand mit durchtrenntem Glied, freiliegender Technik und Trümmern.
- 1024x1024 RGBA-Renderbilder mit transparentem Hintergrund, editierbares .blend-Master.

## Was die Vorschau NICHT belegt
- Keine integrierten Einzelsegment-Sprites oder realen Android-Animationen.
- Keine veränderten Hitboxen oder Trefferregeln und noch keine Android-Regression.
- Kein Geräteleistungstest oder neues APK.
- Noch keine ausdrückliche Freigabe des vollständigen Wurms.

## Qualitätssicherung
Der Workflow kettenwurm-full.yml prüfte erfolgreich: fünf PNGs (1024x1024, RGBA, transparent), Blender-Master vorhanden, IDs 0-11, fünf exakte Typen, Bildunterschied zwischen intakt/beschädigt, Abnahme-Status, Review-Kontaktblatt und SHA256-Datei. Eigene prozedurale Geometrie und Materialien ohne Drittanbieter-Assets.

## Nächstes Gate
Nutzer bewertet Gesamtform und Zerstörungsvorschau. Erst nach ausdrücklicher Freigabe dürfen pro Modul exportierte Sprites, Laufzeitdarstellung, Regression und signierte Update-APK entstehen.