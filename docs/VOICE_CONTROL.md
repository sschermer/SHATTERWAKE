# FINAL SORTIE – optionale Sprachaktivierung (Android 12+, v0.6.4-alpha)

## Bedienung
1. Im Hauptmenü **Einstellungen → Sprachbefehle: EIN** wählen. Erst dann fragt Android bei Bedarf nach Mikrofonzugriff.
2. Normalen Durchlauf starten und das Schiff mit einem Finger bewegen.
3. Mit dem **zweiten Finger** kurz ins freie Spielfeld tippen – außerhalb von Power-ups, Rakete, EMP und Pause.
4. Bei Anzeige **MIC: JETZT SPRECHEN** genau einen Befehl sagen:
   - **Schild** → Schutzschild
   - **Turbo**, **Rapid** oder **Schnellfeuer** → Schnellfeuer
   - **Dreifach** oder **Dreifachschuss** → Dreifachschuss
   - **Plasma** → Plasma
5. Derselbe Codepfad wie beim Touch-Button wird ausgeführt; genau eine verfügbare Ladung wird verbraucht. Ohne Ladung keine Aktivierung.

## Bedien- und Sicherheitsvertrag
- Beide bestehenden Steuerungsmodi bleiben erhalten; der erste Steuerfinger bewegt das Schiff unverändert.
- Kein Start durch Einzelfinger, über Buttons, während Tutorial, Shop, Menü oder Pause.
- Kein Dauerlauschen: Aufnahme maximal 3,3 Sekunden, höchstens 1,6 Sekunden zusätzliche Ergebnis-Nachlaufzeit. Für einen neuen Befehl ist ein neuer Zweitfingertipp nötig.
- Mehrdeutige, unbekannte und lange Äußerungen sind ungültig; niemals zwei Ladungen für ein Kommando.
- Mikrofon bei App-Pause, Hintergrund oder Lebenszyklusende schließen.
- Nutzer können Sprachsteuerung abschalten und Android-Mikrofonrecht widerrufen.
- Android 12 / API 31+ und vorhandener **lokaler** deutscher Spracherkenner erforderlich. Ausschließlich SpeechRecognizer.createOnDeviceSpeechRecognizer, kein Standard-/Cloud-Fallback. Keine INTERNET-Berechtigung, Aufzeichnung, zusätzlichen SDKs oder Audiotranskripte im Spiel.
- Fehlt der lokale Dienst oder ein Sprachmodell, bleibt die Touch-Steuerung unverändert.

## Architektur / Qualität
- VoiceCommandRules.java: deutsche Stichwortgrammatik und getestete sichere Zweitfinger-Berührungszonen.
- VoiceCommandController.java: explizite Android-Permission, nur lokale Kurzzeiterkennung, Timeout, Lifecycle-Cleanup.
- GameView.java: opt-in Einstellung und gespeicherter Schalter in bisherigen SharedPreferences; lokaler Mikrofonstatus; dieselbe useAbility-Routine wie bei Touch.
- MainActivity.java: Erkennungs-Lifecycle und Berechtigungsrückmeldung.
- tests/VoiceCommandChecks.java + tests/check_voice_privacy.py: Tests für Sprache, Ablehnung falscher Befehle, Touchgrenzen und APK-Permissions.

## Offen: echter Mikrofontest
Die CI kann Code-, Permissions-, Asset- und Buildbedingungen testen, nicht aber Genauigkeit oder Verfügbarkeit des deutschen Sprachmodells auf dem Smartphone. Ein Gerätetest mit Spielaudio ist nötig.