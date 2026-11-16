# SHATTERWAKE – Hero Fighter / Abnahmegalerie

**Status:** Vorschau, nicht freigegeben. Keine Android-Integration.

**Stilziel:** das vom Nutzer bestätigte, realistische, schwer gepanzerte
Sci-Fi-Schiff im Industriehangar. Diese erste bearbeitbare 3D-Konstruktion hat
echte Module und hochwertige Renderausgaben, entspricht dem Konzeptbild aber
noch nicht in allen Oberflächen- und Detailqualitäten. Ehrliche visuelle
Beurteilung vor einem Merge.

## 1. Rohzustand der Sternenwerft
![Fighter ohne Umbauten](previews/werft_before.jpg)

## 2. Schnellfeuer: vollständig montiertes Zwillingsgeschütz
![Fighter mit Waffenmodul](previews/werft_weapons.jpg)

## 3. Schild: zweifacher physischer Schildgenerator
![Fighter mit Schild](previews/werft_shield.jpg)

## 4. Drohnen: montierte Start- und Ladedocks
![Fighter mit Drohnendocks](previews/werft_drones.jpg)

## 5. Tatsächliche Gameplay-Ausrichtung
![Fighter GAME – Nase oben](previews/game_nose_up.png)

## Bearbeitbare Quelle / Integration

- **Blender-Master:** [hero_fighter.blend](hero_fighter.blend)
- **Parametrischer Modellaufbau:** [build_master.py](build_master.py)
- **Produktionsfertige, aber noch nicht freigegebene transparente PNGs:** [staging/game](staging/game) und [staging/werft](staging/werft)
- **Aus tatsächlicher Kameraprojektion bestimmte Koordinaten:** [staging/fighter_layout.json](staging/fighter_layout.json), wird durch den separaten Layout-Workflow exportiert
- **Herkunft/Lizenz:** [LICENSE_SOURCES.md](LICENSE_SOURCES.md) (eigene Konstruktion, keine übernommenen Fremdmodelle)

Der bereits veröffentlichte Android-Build nutzt diese Grafiken **nicht**.
Ein Wechsel erfolgt erst nach ausdrücklicher Freigabe.
