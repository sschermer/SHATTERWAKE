# SHATTERWAKE – Verbindlicher Grafik-Produktionsstandard (ART-001)

**Status:** Verbindlicher Produktionsvertrag für neue Hero-Fighter-Grafik, Version 1.0
**Gültig ab:** 16.11.2026
**Referenz:** Die vom Nutzer ausdrücklich bestätigten realistischen Raumschiff- und Hangar-Entwürfe; insbesondere der große, dunkelmetallische, schwer gepanzerte Jäger im Industriehangar.
**Zweck:** Grafiken unabhängig vom Spiel entwickeln und visuell abnehmen, aber ohne Neuanfertigung direkt in die Android-App übernehmen.

> Dieses Dokument ist eine **Produktionsvorgabe**, kein Nachweis bereits fertiger Grafiken. Der aktuell im Spiel enthaltene Grab3D-Jäger wurde visuell **nicht** freigegeben; seine geringe Detailtiefe und die um 90° falsche Ausrichtung erfüllen diesen Standard nicht.

## 1. Visuelles Abnahmekriterium – wichtiger als die Technik

- Ziel: hochwertiger, glaubwürdiger, cineastisch beleuchteter **Hard-Surface-Militärjäger**, keine Cartoon-, Low-Poly- oder Neon-Arcade-Anmutung.
- Rumpf: vielschichtige Panzerung, mechanisch plausible Fugen, versenkte Technik, unterschiedlich bearbeitetes Metall, Kantenabrieb, Schrauben, dezent abgenutzte Oberflächen.
- Waffen: mechanisch **integrierte** Kanonen mit Lafetten, Gehäusen, Läufen, Mündungen und Versorgung; keine isolierten Metallstangen.
- Material: texturierte PBR-Materialien mit Base Color, Roughness, Metallic und, wo vorhanden, Normal/AO. Keine glatte weiße Plastikoptik.
- Licht: starke, glaubwürdige Flächen- und Konturbeleuchtung, erkennbare Schatten und Materialtiefe. Funktionsfarben sparsam: Schilde kaltblau, Plasma violett, Waffen dezent warm, Sensoren kontrolliert cyan.
- Umgebung: realistische Werft/Hangar-Szene **als getrenntes Hintergrund-Asset**; sie gehört **nicht** in die transparenten Schiffslayer.
- **Go/No-Go:** Der Rumpf muss bereits **ohne** Partikel, Scheinringe, HUD, Waffenfeuer oder Umgebung überzeugend wirken. Das Referenzbild ist ein Ziel für Formensprache und Qualitätsniveau, kein Beleg für bereits vorhandene Assets.

Erst wenn der Nutzer **das externe Vorschaubild ausdrücklich freigibt**, erfolgt die Ersetzung des Spielerschiffs in einer APK. Einzelne hübsche Konzeptbilder ohne nutzbares Mastermodell gelten **nicht** als fertig.

## 2. Ein Modell, zwei Kameras, ein Datenvertrag

Eine reproduzierbare Blender-Masterszene ist die einzige Quelle für Geometrie, Materialien, Modulmontage, Licht und Export. Pro Ansicht werden **Basis und alle Module mit exakt derselben Kamera, dem gleichen Bildausschnitt, identischen Objekteigenschaften und transparentem Hintergrund** gerendert.

| Ansicht | Ausrichtung | Export | Zweck |
|---|---|---|---|
| **GAME** | Orthografisch, fast von oben, Schiffsnase zeigt exakt nach oben; leichte Höhe nur soweit Waffen und Rumpf lesbar bleiben | 1024 × 1024, RGBA PNG | Spielfigur, Menü, einheitliche Kollisions-/Schussanker |
| **WERFT** | Feste cineastische Dreiviertel-Draufsicht auf **denselben** Jäger; kein seitliches Umkippen des Modells | 2048 × 2048, RGBA PNG | Großansicht und Bauteil-Zoom im Industriehangar |
| **PREVIEW** | Wie WERFT, inklusive der separat erzeugten Hangar-Szene | JPG/PNG je Upgrade-Zustand | Visuelle Freigabe ohne APK |

Werft- und Gameplay-Kamera dürfen sich unterscheiden. Entscheidend ist **Konsistenz innerhalb jeder Ansicht**, nicht ein Pixel-für-Pixel-Overlay zwischen unterschiedlichen Kameras. Der Android-Code muss später den passenden Ansichtssatz laden.

### Unverhandelbares Koordinatensystem

- Android-Welt: Bildschirm-x nach **rechts**, Bildschirm-y nach **unten**; Schussrichtung ist **negatives y**.
- **Nase oben**, Triebwerke unten; keine 90° gedrehte Rumpfgeometrie. Waffenläufe, Zieloptik, Triebwerksflammen und Mündungsanker müssen der Schiffsausrichtung folgen.
- Die Vorwärtsachse eines externen Modells **vor** dem Rendern in der Masterszene normieren. Keine nur auf den Rumpf angewendete nachträgliche PNG-Rotation.
- Pivot: Schiffsmittelpunkt; identische Bildkoordinaten/Canvasgröße **für sämtliche Layer derselben Ansicht**.
- Keine automatische Einzelbild-Zuschneidung: gleiche transparenten Ränder und registrierter Nullpunkt, auch bei kleinen Modulen.
- Maschinenlesbare Anker (Mündungen, Triebwerke, Drohnen-Docks, Hitbox, Modul-Zoompunkte) werden durch Kamera-Projektion aus der Masterszene exportiert, **nicht** geschätzt oder später manuell verschoben.

## 3. Verbindliche Dateistruktur und exakte Namen

Die im Spiel bereits vorhandene Ladekonvention hat Vorrang. Deshalb **keine neuen frei erfundenen Dateinamen für die GAME-Basisebenen**.

\`\`\`text
art/hero_fighter/
    hero_fighter.blend                 # einziges editierbares 3D-Master
    source_assets/                     # urspruengliche freie Modelle/Texturen
    reference/                         # Referenzen und visuelle Abnahmebilder
    production_manifest.json           # maschinenlesbarer Vertrag (bereits angelegt)
    previews/
        game_default.png
        werft_default.png
        werft_rapid_before_after.png
        werft_shield_before_after.png
        werft_drone_before_after.png
    LICENSE_SOURCES.md                  # pro importierter Ressource Urheber/URL/Lizenz

app/src/main/assets/rendered/
    fighter_base.png                   # GAME, 1024², vorhandener Android-Dateiname
    fighter_module_0.png               # GAME, kompatible Einstiegsstufe
    ...
    fighter_module_7.png
    fighter_module_<id>_L<level>.png    # zusätzliche mehrstufige Varianten
    werft/
        fighter_base.png               # WERFT, 2048², Loader noch zu ergänzen
        fighter_module_0.png
        ...
        fighter_module_7.png
        fighter_module_<id>_L<level>.png
        hangar_background.png          # eigener gerenderter Hangar
    fighter_layout.json                # projizierte Anker und Zoompositionen
\`\`\`

Die unter \`rendered/\` genannten Dateien sind **Exportprodukte**, nicht die bearbeitbare Quelle. **Nicht** versuchen, aus einer Abnahme-PNG die 3D-Konstruktion oder Modulpositionen zurückzubauen. Export kann später reproduzierbar aus \`hero_fighter.blend\` erzeugt werden.

**Ist-Stand des Android-Codes (am 16.11.2026):**
- \`SpriteAtlas.java\` lädt \`rendered/fighter_base.png\` sowie \`rendered/fighter_module_0.png\` bis \`_7.png\`.
- \`GameView.java\` zeichnet die Spielansicht in einer logischen Welt von **420 × 780**, das Schiff in etwa **100 × 94** Weltpixeln.
- Die aktuelle Werft vergrößert dieselbe GAME-Grafik; **separate WERFT-Dateien und Stufenvarianten sind noch nicht implementiert**.
- \`GameRules.java\` enthält **fest codierte** Werft-Fokuspunkte, die bei der ersten freigegebenen Integration durch aus Blender exportierte Punkte ersetzt werden müssen.
- \`tools/render_cc0_fighter.py\` ist der derzeitige Generator, **nicht** die künftige Qualitätsreferenz. Er muss nach Freigabe an die Masterszene angebunden bzw. ersetzt werden.

**Integrationsaufwand einmalig:** Die zusätzliche WERFT-Ansicht, Variantenauswahl und das Metadatenladen benötigen einen einmaligen Adapter im Code. Die **Grafikproduktion selbst** muss dadurch **nicht** wiederholt werden. „Copy/paste“ bedeutet: freigegebene Assets unverändert in diese Ordner übernehmen und den Adapter mit dem unveränderten Vertrag verwenden; nicht, dass die aktuelle APK bereits beide Ansichten unterstützt.

## 4. Module und Spielzustände

IDs, Werte und Bedeutung bleiben kompatibel mit \`GameRules\` / \`GameView\`.

| ID | Modul | Level-/Zustandslogik | Optisch tatsächliche Änderung |
|---:|---|---|---|
| 0 | Schnellfeuer | 0–5 | Zusätzliche Zuführungen, realistisch verschraubte Mehrfachläufe und Kühlung |
| 1 | Waffenschaden | 0–5 | Größere Hauptgeschütze, massivere Aufhängung und Energiekammer |
| 2 | Reparatur/Rumpf | HP abhängig, kein permanent gekauftes Reparaturlevel | Sichtbarer Schadens-/Reparaturzustand, keine unechte neue Waffenstufe |
| 3 | Sternenmagnet | 0–3 | Mechanischer Fang- bzw. Feldprojektor am Rumpf |
| 4 | Begleitdrohne | 0–3 | Neue Docking- und Ladeplätze mit klaren Anbaupunkten |
| 5 | Schildverstärkung | 0–2 | Physische, paarige Schildemitter mit geschützten Linsen |
| 6 | Spezialladungen | Nachladekauf, kein Level 0–5 | Nachrüstbare Energie-/Munitionskassette; sichtbare Funktion ohne Fantasie-Ränge |
| 7 | Zielkorrektur | 0–5 | Sensor-/Zielrechnereinheit mit wachsendem technischen Aufbau |

Jedes Modul hat ein eigenes 3D-Objekt bzw. eine klar definierte Objektgruppe im Master. Pro Level **echte zusätzliche/ersetzte Geometrie**, nicht nur andere Farbe oder Markierung. Die Visualisierung der Kaufvorschau darf ein transparentes Prospektivmodell zeigen; der Kaufzustand muss beim anschließenden Gameplay **identisch** aussehen. Gekaufte Stufen sind im Lauf permanent, nicht global über alle Neustarts hinweg.

Für Reparaturen müssen **HP/MaxHP und Anzahl fehlender Reparaturkäufe** korrekt im Werft-UI bleiben; grafische Beschädigungen sind optional, dürfen die bestehende HP-Logik nicht überschreiben.

## 5. Asset-Manifest und Anker

\`art/hero_fighter/production_manifest.json\` ist die **strukturierte, versionierte Quelle für den Dateivertrag**. Ein erfolgreicher Render exportiert ergänzend \`fighter_layout.json\` mit den **tatsächlich projizierten** Werten:

- Pivot der Basisgrafik (beide Ansichten)
- Mündungen links/rechts und ggf. Zusatzwaffen
- Triebwerk-Emitter links/rechts
- Drohnen-Andockpunkte
- Bounding-/Hitbox-Referenz in GAME-Ansicht
- Fokusposition je Modul **separat für GAME und WERFT**
- Rendering-Preset, Kameraidentität, Ausrichtung und Asset-Version

**Nullwerte** im aktuellen Vertragsmanifest sind bewusst **noch nicht ausgemessene** Anker. Sie werden erst nach Auswahl des neuen 3D-Modells durch einen geometrisch bestimmten Projektionsexport ersetzt. Keine erfundenen Pixelkoordinaten.

## 6. Darstellung und Leistung auf Android

- PNG-Export: **RGBA 8 Bit, sRGB**, transparenter Hintergrund, frei von eingebranntem Hangar, Schatten des Hangars oder Bildschirm-UI; Alpha-Kanten ohne dunkle Säume.
- Alle Layer müssen dieselbe Beleuchtung und Projektion verwenden. Transparente leere Pixel bleiben erhalten.
- GAME-Dateien: 1024²; WERFT-Dateien: 2048². Bei knapper Android-Hardware erlaubter zusätzlicher Downsample-Export, **ohne** manuelles Nachzeichnen.
- Das 3D-Master kann hochkomplex sein; Android erhält vorgerenderte Bilder, **keine** zwingende Echtzeit-3D-Engine.
- 2048² RGBA belegt unkomprimiert **16 MiB pro Bild**; neun dauerhaft geladene Werft-Layer wären bereits **144 MiB**. Deshalb Werft-Grafiken **lazy** laden, aktive Upgrades ggf. in einen Cache-Composit rendern und Ressourcen beim Verlassen freigeben. Zielwert für dauerhaft residenten Schiffsgrafikspeicher in der Werft **maximal 96 MiB**, für die GAME-Ansicht **maximal 48 MiB** – vor Release auf echten Android-Geräten messen.
- Keine teuren Neurenderings von PBR/3D während des Spielens; perspektivischer Wechsel nur zwischen vorab gerenderten Ansichten.

## 7. Abnahme- und Integrations-Gates

**Gate A – Quelle:** Freie Lizenz dokumentiert, kommerziell nutzbar, bearbeitbar, freiwillige Künstlernennung, Source-Modell + Originalmaterialien + Bearbeitungsdatei archiviert.

**Gate B – Design:** Freistehender Jäger sieht **ohne Effekte** wie der Referenz-Hard-Surface-Jäger aus; die Waffen wirken integriert. Keine helle Spielzeugoptik. Nutzer entscheidet anhand WERFT- und GAME-Vorschau, **noch nicht** anhand einer APK.

**Gate C – technische Grafikprüfung:** Automatisch kontrollieren: vorhandene Dateien, Pixelgrößen, PNG/RGBA, Alpha außerhalb des Schiffes, identische Projektionsparameter innerhalb jeder Ansicht, keine abgeschnittenen Meshes oder Bauteile. Visueller Test: **Nase oben in GAME**, Waffen nach oben, Triebwerke nach unten; Module auf dem Rumpf, kein rechter/seitlicher Versatz.

**Gate D – Varianten:** Mindestens drei Vorher-/Nachher-Paare mit **realen** Umbauten: Waffen, Schild, Drohnen. Kein Upgrade nur als Linie oder HUD-Markierung.

**Gate E – Einbau:** Erst nach Freigabe: vorhandene \`rendered/\`-GAME-PNGs ersetzen, WERFT-Loader und Level-Auswahl ergänzen, projizierte Anker übernehmen. Bestehende Logik, Treffer-/Schusskollision, Werft-Vorschau und Kaufbestätigung erhalten.

**Gate F – APK:** Android Build + Java-Regeltests + praktischer Sichttest in Menü, Spiel, Werft-Zoom und allen freigegebenen Modulen. Erst danach darf die neue Grafik als fertig bezeichnet werden.

## 8. Bewusste Nicht-Ziele der ersten Lieferung

- Keine vollständige Neugestaltung aller Gegner/Bosse vor der Abnahme des eigenen Schiffs.
- Keine fingierte Fotorealistik durch zusätzliche Canvas-Polygone.
- Kein automatisches „irgendein CC0-Modell nehmen“ ohne Sichtprüfung der konkreten Qualität.
- Keine Veröffentlichung ungetesteter, um 90° gedrehter Produktionsgrafiken.
- Keine Änderung der Spielmechanik, nur weil Grafikpositionen korrigiert werden müssen.

**Erste verbindliche Lieferung:** Ein freigegebenes, hochqualitatives **Hero-Fighter-Mastermodell** und die drei geprüften Upgrade-Varianten (Waffe, Schild, Drohne) mit GAME-/WERFT-Export, Ankern, Lizenznachweisen und Vorschaubildern. Danach Erweiterung auf die restlichen Module und Einbau.
