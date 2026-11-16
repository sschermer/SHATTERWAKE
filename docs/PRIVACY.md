# FINAL SORTIE — Datenschutz / Offline-Sprachbefehle (v0.6.5-alpha)

## Bestehende App-Daten
FINAL SORTIE arbeitet ohne INTERNET-Berechtigung, Werbung, Analyse,
Tracking, Online-Konten, Cloud-Spielstände, Einkäufe und fremde Medien.
Die bestehende lokale Einstellungsspeicherung \`shatterwake_save_v1\`
bleibt erhalten: Bestwert, dauerhaft geborgene Technologien,
Steuerungsmodus, Ton, Vibration, Tutorialstand und weitere Spielfortschrittsdaten.
Android-Sicherungen der App sind deaktiviert.

## Optionale Mikrofonverwendung
Sprachbefehle sind **standardmäßig deaktiviert**. Nur wer sie unter
**Einstellungen → Sprachbefehle: EIN** ausdrücklich aktiviert, wird
gegebenenfalls um die Android-Mikrofonberechtigung \`RECORD_AUDIO\` gebeten.
Das Mikrofon öffnet sich nicht beim normalen App- oder Spielstart.

Bei aktiviertem Sprachmodus startet ein **zweiter Fingertipp** während
des Steuerns im Spielfeld, außerhalb der Touch-Buttons, eine einzelne,
zeitlich begrenzte Spracherkennung. Es werden nur vier Kommandos für
bestehende Spezialfähigkeiten akzeptiert: Schild, Turbo/Schnellfeuer,
Dreifach und Plasma. Die Sitzung endet nach einem Ergebnis bzw.
automatisch nach kurzer Zeit. Im Menü, Tutorial, der Werft und während
einer Pause wird kein Sprachbefehl gestartet.

FINAL SORTIE verwendet ausschließlich die Android-Schnittstelle
\`SpeechRecognizer.createOnDeviceSpeechRecognizer\` (Android 12/API 31+
mit installiertem lokalem Erkennungsdienst), Sprache \`de-DE\`.
Es gibt **keinen Online-/Cloud-Fallback**. Fehlt der lokale Dienst oder
das deutsche Offline-Sprachmodell, bleibt die Touch-Bedienung nutzbar.
Ein Herunterladen von Sprachmodellen wird von der App nicht ausgelöst.

Die App erstellt oder speichert keine Audiodateien, Gesprächsmitschnitte,
Transkripte oder Analysedaten und überträgt selbst keine Spracherkennungsdaten
über das Netzwerk. Die Verarbeitung erfolgt im lokal bereitgestellten
Android-Systemdienst. Die App besitzt weiterhin keine
\`android.permission.INTERNET\`-Berechtigung. Für Bereitstellung
und Updates von Sprachpaketen können zusätzliche Hersteller-/Android-
Systembedingungen gelten; diese sind nicht Teil des Spiels.

Die Option kann jederzeit wieder deaktiviert werden. Darüber hinaus
lässt sich die Mikrofonberechtigung in den Android-App-Einstellungen
widerrufen. Beim Verlassen der App oder im Hintergrund wird eine
laufende Spracherkennung beendet.

## Vor kommerzieller Veröffentlichung
Die verantwortliche veröffentlichende Person muss eine vollständige
Datenschutzerklärung inklusive ihrer tatsächlichen Kontaktangaben,
regionaler Pflichten sowie einer Prüfung des jeweiligen Android-
Sprachdienstes bereitstellen. Das Manifest und die gepackte APK
werden unter \`tests/check_voice_privacy.py\` getestet.


## Vordefinierte und nutzerdefinierte Sprachkombinationen (v0.6.5)
Vier optionale, vom Nutzer änderbare lokale Auslösewörter und je ein Bitmuster
für sechs Aktionen werden unter `voice_word_0..3` und
`voice_mask_0..3` in den bestehenden SharedPreferences gespeichert.
Die Wörter dienen ausschließlich zur Abgleichung mit lokalen Android-
Erkennungsergebnissen. Sie werden nicht an FINAL-SORTIE-Server übertragen
(keine Internetberechtigung). Die App speichert keine Mikrofonaufnahmen,
Transkripte oder Sprachverläufe. Zum Löschen/Zurücksetzen sind die Wörter
im Menü editierbar; ein vollständiges Zurücksetzen erfolgt mit dem Löschen
der lokalen App-Daten.
