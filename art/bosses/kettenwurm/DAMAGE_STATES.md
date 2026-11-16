# SHATTERWAKE – Kettenwurm: vier Schadenszustände für fünf Bauteiltypen

**Status:** VERBINDLICH sind Zustände und deren Bedeutung. Die Blender-Grafik ist eine visuelle Entwurfsproduktion; die Android-Integration erfolgt erst nach ausdrücklicher Gesamtfreigabe.

## Zustände pro Segment (nach EIGENEN Segment-HP, nicht Gesamt-Boss-HP)

| Zustand | Berechnung | Grafik | Effekt nach Integration |
|:---:|---|---|---|
| 0 | HP > 2/3 der maximalen Segment-HP | Intakte, polierte Metalloberfläche | Keine Partikel |
| 1 | 1/3 < HP <= 2/3 | Echte verformte Panzergeometrie, kleinere Delle | Keine Daueremission |
| 2 | 0 < HP <= 1/3 | Stärker eingedrückte Panzerung, freiliegende Technik | Kurzlebige, sparsame Funken / optional Rauch |
| 3 | HP <= 0 | Sichtbare Explosion, abgerissene Bleche und Kupplungen | **Einmalige** Animation; danach Segment entfernen und Gelenk unterbrechen |

Die Grenzen sind exakt formuliert, damit etwa HP=maxHP*2/3 noch Zustand 1 und HP=maxHP/3 noch Zustand 2 ergibt. Schadensstufen steigen beim Abbau der HP monoton an.

## Fünf Bauteiltypen

| Typ | Bezeichnung | Stufe 1 – leicht verbeult | Stufe 2 – stark beschädigt | Stufe 3 – zerstört |
|:---:|---|---|---|---|
| 0 | Greif-/Sensor-Kopf | Gedellte Stirnpanzerung, verbogener Metallstoßrand | Aufgebrochener Stirnplattenbereich, freiliegende Greifmechanik, Funken am Kieferantrieb | Schädelpanzer in Splittern, Feuerkern und abgerissene Greifer |
| 1 | Waffenmodul | Kleine Delle der Waffenlafette | Verzogene Lafette, gestauchte Läufe, Funken am Waffenanschluss | Waffenpanzer, Läufe und Versorgungsleitungen zerplatzen |
| 2 | Trennkupplung | Leicht eingedrückte Kupplungsplatte | Gelöste Klammern, offene Hydraulik, Funken | Berstende Kupplung, danach Lücke in der Kette |
| 3 | Reaktormodul | Gestauchter Reaktorkäfig | Offener heißer Kern, verbogener Metallbügel, Funken | Intensive Kernexplosion und Metallsplitter |
| 4 | Energie-/Kondensatormodul | Delle am Schutzgehäuse | Freiliegende Spulen, Funken mit dezentem Kaltblau | Explodierende Energiespeicher, Teile lösen sich |

**Keine grobe Noise-/Bump-Körnung:** Die Verformung wird in Blender geometrisch erzeugt. Polierter Titan-, Stahl- und dunkelroter Panzerungsstil bleibt erhalten.

## Geplanter Datenvertrag

- Zwölf vorhandene Boss-Nodes und IDs 0–11 unverändert.
- Typverteilung nach aktuellem GameView: `[0,1,1,4,2,3,4,1,2,4,3,1]`.
- Fünf Typen × vier Zustände = **20 transparent gerenderte GAME-Einzelbilder** in 1024×1024 RGBA. Alle stammen aus derselben Kamera, mit exakt gleicher Bildregistrierung.
- Zusätzliche Detailansichten zum Prüfen von sichtbaren Dellen und Funken.
- Dateimuster: `damage_output/boss_type_<typ>_damage_<zustand>.png`.
- Editierbare Blender-Masterszene: `damage_output/kettenwurm_damage_master.blend`.
- Ursprung: `build_damage_states.py`, bestehende polierte Quelle `build_mk2.py`; Maschinendaten `damage_contract.py`.
- `damage_output/damage_review_matrix.jpg` enthält sämtliche 20 Varianten zum visuellen Vergleich.

## Integrations- und Testgrenze

Die GIF-/Animationsdauer der Explosion und die Häufigkeit der Funken werden erst beim Android-Integrationsschritt verbindlich abgestimmt. Zustand 3 ist **kein dauerhaft sichtbarer Block**: Nach Abspielen des Explosions-Effekts muss das Segment verschwinden, die vorhandene Kollisionslogik bleibt maßgeblich.

Bei der späteren Android-Integration sollen die Grafiken je `Node.type` und Verhältnis `hp/maxHp` gewählt werden. Der vorhandene Kill-Trigger, Split-Mechanismus, Drops, Trefferzonen, Projektile und die Boss-Phasen dürfen dabei nicht verändert werden.

**Abnahme:** Erst nach visueller Genehmigung werden die PNG-Dateien in `app/src/main/assets/` integriert, der Java-Renderer angepasst, Regression und Leistung gemessen sowie eine kompatibel signierte Update-APK erstellt. Bis dahin keine Gameplay-/APK-Änderung.

**Lizenzen:** Original-Blender-Geometrie, selbst geschriebene Scripts und prozedurale Materialien; keine fremden Modelle, Texturen oder Medien.