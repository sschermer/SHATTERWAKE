# FINAL SORTIE – Sprachsteuerung und frei belegbare Kombinationen (v0.6.5-alpha)

## Steuerung
1. Im Hauptmenü **Einstellungen → Sprachbefehle: EIN** wählen, Mikrofonberechtigung einmalig erlauben.
2. **Einstellungen → VIER SPRACHKOMBINATIONEN** öffnen. Vier vorbelegte Wörter und ihre sechs einzeln ein-/ausschaltbaren Aktionen werden angezeigt.
3. Ein Wort antippen, neues einzelnes Wort (3–20 Buchstaben) über die Android-Tastatur eingeben und speichern. Direktbefehle und bereits für andere Plätze verwendete Wörter sind ausgeschlossen. Aktionen durch Antippen der sechs Schalter zuordnen. Jede Änderung wird lokal gespeichert.
4. Während des normalen Spiels Schiff mit einem Finger steuern, mit dem zweiten Finger außerhalb der Waffen-/Power-up-/Pause-Flächen ins Spielfeld tippen und sprechen.

## Direkte Befehle – immer verfügbar

| Wort | Aktion und Ladungsquelle |
| --- | --- |
| Schild, Schutzschild | Spezialladung Schild, 8 Sekunden |
| Turbo, Rapid, Schnellfeuer | Spezialladung Schnellfeuer, 9 Sekunden |
| Dreifach, Dreifachschuss | Spezialladung Dreifach, 9 Sekunden |
| Plasma | Spezialladung Plasma, 7 Sekunden |
| Rakete, Raketen | Eine Raketen-/Sekundärladung, sofortige Wirkung |
| Schockwelle, EMP, Schockimpuls | Eine EMP-/Sekundärladung, sofortige Wirkung |

Ein Satz wie **„Dreifach plus Turbo plus Plasma“** oder **„Raketen und Schockwelle“** löst die genannten verfügbaren Aktionen aus.

## Vier editierbare Standardbelegungen

| Auslösewort | Werkseinstellung |
| --- | --- |
| Overdrive | Schild + Turbo + Dreifach + Plasma (keine Raketen/EMP) |
| Inferno | Turbo + Dreifach + Plasma + Raketen |
| Notfall | Schild + EMP |
| Angriff | Turbo + Raketen |

Die Namen und sämtliche sechs Aktionsschalter sind frei änderbar. Ein komplett leerer Slot ist möglich, löst dann nichts aus.

## Spielvertrag
- Jede innerhalb einer Äußerung erkannte Aktion wird **höchstens einmal** ausgelöst, auch bei doppelten Wörtern oder überlappenden Kombinationen.
- **Leere Ladungen werden übersprungen**. Andere Aktionen der Kombination funktionieren trotzdem. Die Sprachsteuerung ändert weder Ladungspreise noch Nachfüllung oder maximale Werte.
- Alle Fähigkeiten behalten ihre eigenen Wirkungszeiten. Zeitversetzte Auslösung bei einer gesprochenen Kombination ist erlaubt und erwünscht. Raketen und EMP sind sofortige Aktionen, keine dauerhaften Power-ups.
- Alle Aktionen verwenden denselben Touch-/GameView-Codepfad für Verbrauch und Wirkung.
- Die vier Profile bleiben dauerhaft unter denselben bisherigen, lokalen Android-SharedPreferences gespeichert. Sie sind keine Tech-Freischaltung und verfallen nicht nach einem Lauf.

## Reaktionszeit
- Die Android-Erkennung wird nach erteilter Berechtigung **vorbereitet und wiederverwendet**.
- Vorläufige Ergebnisse werden auf **zwei aufeinanderfolgende übereinstimmende Erkennungen** geprüft. Wiederholt erkannte Aktionen dürfen früher ausgelöst werden; bei endgültigem Ergebnis werden nur neue Aktionen ergänzt.
- Die Android-Sprachsoftware kann Zwischenergebnisse und End-of-Speech-Hinweise ignorieren. Ohne bestätigte Teil-Ergebnisse erfolgt die Aktivierung wie bisher nach der endgültigen Erkennung.
- Mikrofonaufnahme max. etwa 3,3 Sekunden pro Zweitfingertipp. Keine Dauerüberwachung, kein Mikrofon im Tutorial, Shop, Pause oder Hauptmenü.

## Datenschutz / Gerätekompatibilität
Die Spracherkennung benutzt ausschließlich Androids bereits vorhandenen lokalen `SpeechRecognizer.createOnDeviceSpeechRecognizer` auf Android 12+ (API 31+). **Kein INTERNET-Recht und kein Cloud-Fallback**. Die App protokolliert keine gesprochenen Wörter und speichert weder Audiodateien noch Transkripte. Ein Gerät ohne deutschsprachige Offline-Erkennung kann wie bisher ausschließlich per Touch gesteuert werden.

## Tests und Grenzen
- Java-Regeln: `VoiceCommandChecks`, `VoiceCombosChecks`.
- CI: Android-Build, APK-Identität, Signatur, Datenschutz, Originalgrafiken, Launcher-Icon und alle bisherigen Balancing-/Tutorial-Regeln.
- **OFFEN:** Reaktionsgeschwindigkeit und Erkennungszuverlässigkeit der vier frei gewählten Wörter müssen auf dem jeweiligen Android-Smartphone erprobt werden. Falsch positive vorläufige Erkennung lässt sich trotz zweistufiger Stabilisierung nicht vollständig ausschließen.
