# SHATTERWAKE – dauerhaft signierte Android-Updates

**Status (2026-11-16):** Die dauerhaft signierte v0.6.1-alpha wurde nachweislich als GitHub-Release veröffentlicht. v0.6.2-alpha nutzt denselben Zertifikats-Fingerprint und eine höhere versionCode; eine neue Version wird erst nach erfolgreichem GitHub-Actions-Build veröffentlicht.
Der private Schlüssel ist **nicht im Repository** hinterlegt.

## 1. Einmalige Einrichtung

Der Besitzer dieses Projekts erhält außerhalb des Repositories ein
Sicherungspaket mit dem **PKCS12**-Schlüssel
`SHATTERWAKE-update-signing-key.p12` und der Datei
`SHATTERWAKE-GitHub-Secrets.txt`.

1. Das komplette Paket **offline an zwei geschützten Orten sichern**.
   Keystore und Kennwort niemals in Git, Issues, Chat oder E-Mails kopieren.
2. Repository https://github.com/sschermer/SHATTERWAKE öffnen.
3. **Settings → Secrets and variables → Actions → New repository secret**.
4. Secret **`SHATTERWAKE_KEYSTORE_BASE64`**: die lange Base64-Wertzeile
   direkt unter diesem Namen aus der Textdatei (ohne die Überschrift).
5. Secret **`SHATTERWAKE_KEYSTORE_PASSWORD`**: die Passwort-Wertzeile
   direkt unter diesem Namen aus derselben Datei.
6. **Actions → Build native Android APK → Run workflow**, Branch `main`.
7. Nach **erfolgreichem** Durchlauf signierte APK aus dem
   [Release v0.6.2-alpha](https://github.com/sschermer/SHATTERWAKE/releases/tag/v0.6.2-alpha)
   herunterladen (`SHATTERWAKE-v0.6.2-alpha-signed.apk`).

Secrets können mit dem verfügbaren GitHub-Connector nicht geschrieben werden.
Daher sind Schritte 3–6 nur durch die berechtigte Repository-Verwaltung möglich.

## 2. Gesicherte App-Identität

- Android-Paketname: `de.shatterwake.game` (unverändert)
- Alias: `shatterwake-updates`
- Keystore: PKCS12 mit RSA-4096 und SHA-256
- Erwarteter **SHA-256 Fingerprint des Signaturzertifikats**:
  `12fc96a90b8ca79d7e01f8e1a420f97e872ec71dc32ebe2b10ba59919776fc06`
- Der GitHub-Workflow prüft **vor jeder Veröffentlichung**, dass der
  tatsächlich verwendete APK-Signaturfingerprint exakt übereinstimmt.
- Die Versionsnummer (Android-`versionCode`) steigt bei jedem
  erneuten Actions-Lauf automatisch: `10000 + github.run_number`.
  Die lesbare Versionsbezeichnung ergänzt die jeweilige Run-Nummer.
- GitHub-Releases `v0.6.1-alpha` und `v0.6.2-alpha` sind getrennte, dauerhafte Artefakte.
  Version 0.6.2 wird nicht über den alten Release-Tag hochgeladen; der alte APK-Link bleibt erhalten.
- Die frühere Debug-Pipeline stellt keine neuen Debug-APKs mehr als
  vermeintliche Update-Dateien bereit.

## 3. Erster Wechsel und spätere Updates

**Wichtig:** Die bisherige `v0.6.0-alpha-debug.apk` trägt
einen automatisch erzeugten Debug-Schlüssel. Android kann eine mit dem neuen
dauerhaften Schlüssel signierte APK **nicht darüber installieren**.
Beim ersten Wechsel ist daher eine Deinstallation der Debug-App
erforderlich. Dabei gehen lokal gespeicherte Tech-Freischaltungen und
Einstellungen möglicherweise verloren. Erst deinstallieren, wenn
die signierte APK tatsächlich verfügbar ist und ein Verlust akzeptiert wird.

Ab der ersten Installation von `v0.6.1-alpha-signed.apk` installiert Android
neue APKs **über die bestehende App**, falls Paketname, Signatur identisch
und `versionCode` höher sind. App-Daten bleiben dabei erhalten.
Es handelt sich um manuellen Sideload, nicht automatische Update-Erkennung.

## 4. Sicherheits- und Fehlerszenarien

- **Ein Secret fehlt:** Der Workflow bricht ab; er veröffentlicht keine
  falsch signierte APK.
- **Beide Secrets fehlen:** Tests laufen normal, die Veröffentlichung
  ist bewusst deaktiviert.
- **Falscher Schlüssel:** Fingerprintprüfung verhindert Veröffentlichung.
- **Schlüssel/Passwort verloren:** Eine kompatible Update-Signatur kann
  nicht neu erzeugt werden. Offline-Backups unbedingt erhalten.
- **Schlüssel kompromittiert:** Nicht weiter veröffentlichen; die
  bisherige APK-Sideload-Identität und bestehende Installationen sind betroffen.
- **Release für Google Play:** Das hier ist private APK-Testsignierung;
  für Play App Signing ist ein separates Release-/Upload-Key-Konzept und
  eine separate rechtliche Prüfung erforderlich.

Quelle: https://developer.android.com/studio/publish/app-signing
