# SHATTERWAKE – Hero Fighter / Abnahmegalerie

**Status:** Vorschau, nicht freigegeben. **ART-002:** Glatte, vollständig körnungsfreie Metallshader; neue Renderbilder werden mit derselben Blender-Masterszene erstellt. Keine Android-Integration.

**Stilziel:** das vom Nutzer bestätigte, realistische, schwer gepanzerte
Sci-Fi-Schiff im Industriehangar. Diese erste bearbeitbare 3D-Konstruktion hat
echte Module und hochwertige Renderausgaben, entspricht dem Konzeptbild aber
noch nicht in allen Oberflächen- und Detailqualitäten. Ehrliche visuelle
Beurteilung vor einem Merge.

## 1. Rohzustand der Sternenwerft
![Fighter ohne Umbauten](previews/werft_before.jpg)

## 2. Schnellfeuer: vollständig montiertes Zwillingsgeschütz
![Fighter mit Waffenmodul](previews/werft_weapons.jpg)

## 3. Erhöhter Schaden: dunkle Kanonen (neue Variante)

![Kanonen mit erhöhtem Schaden in dunklem Anthrazit](previews/werft_damage.jpg)

Die Kanonen haben dieselbe Form und dieselben Befestigungspunkte wie bei
Schnellfeuer, aber eine dunkle, glatte Gunmetal-Oberfläche. Bei der späteren
Integration wird je nach Upgrade-Zustand entweder das helle Geschütz (ID 0)
oder das dunkle Geschütz (ID 1) ausgewählt – beide niemals gleichzeitig.

## 4. Schild: zweifacher physischer Schildgenerator
![Fighter mit Schild](previews/werft_shield.jpg)

## 5. Drohnen: montierte Start- und Ladedocks
![Fighter mit Drohnendocks](previews/werft_drones.jpg)

## 6. Sternenmagnet

![Stufe 1: kompakte gepanzerte Magnetprojektoren](previews/werft_magnet.jpg)

![Stufe 3: zusätzliche äußere Magnetspulen](previews/werft_magnet_max.jpg)

## 7. Zielkorrektur

![Stufe 1: mechanische Sensor- und Zieloptik](previews/werft_target.jpg)

![Stufe 5: ausgebaute optische Zielanlage](previews/werft_target_max.jpg)

Zielkorrektur ist im Spiel in fünf 3-%-Stufen vorgesehen (bis 15 %).

## 8. Spezialladungen

![Mechanisch integrierte Raketen- und Energiemagazine](previews/werft_special.jpg)

## 9. Reparatur und Schäden

![Beschädigte Rumpfpanzerung: drei sichtbare Trefferstufen](previews/werft_damage_state_3.jpg)

![Rumpf mit ausgetauschten, intakten Panzerplatten](previews/werft_repair.jpg)

Die vollen Rumpf-HP werden ohne Beschädigungs-Overlay gezeigt. Fehlende
Rumpf-HP wählen aus drei Schadensstufen; bei Reparaturen reduziert sich
die sichtbare Schadensstufe entsprechend. Die Darstellung ist separat
vom numerischen Schaden/HP-System.

## 10. Tatsächliche Gameplay-Ausrichtung
![Fighter GAME – Nase oben](previews/game_nose_up.png)

## Bearbeitbare Quelle / Integration

- **Blender-Master:** [hero_fighter.blend](hero_fighter.blend)
- **Parametrischer Modellaufbau:** [build_master.py](build_master.py)
- **Produktionsfertige, aber noch nicht freigegebene transparente PNGs:** [staging/game](staging/game) und [staging/werft](staging/werft)
- **Aus tatsächlicher Kameraprojektion bestimmte Koordinaten:** [staging/fighter_layout.json](staging/fighter_layout.json), wird durch den separaten Layout-Workflow exportiert
- **Herkunft/Lizenz:** [LICENSE_SOURCES.md](LICENSE_SOURCES.md) (eigene Konstruktion, keine übernommenen Fremdmodelle)

Der bereits veröffentlichte Android-Build nutzt diese Grafiken **nicht**.
Ein Wechsel erfolgt erst nach ausdrücklicher Freigabe.
