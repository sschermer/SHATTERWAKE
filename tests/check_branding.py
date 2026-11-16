#!/usr/bin/env python3
"""FINAL SORTIE branding regression: run at source checkout or with APK path."""
from pathlib import Path
import hashlib
import struct
import sys
import zipfile

root = Path(__file__).resolve().parents[1]
view = (root / "app/src/main/java/de/shatterwake/game/GameView.java").read_text(encoding="utf-8")
manifest = (root / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8")
gradle = (root / "app/build.gradle").read_text(encoding="utf-8")
workflow = (root / ".github/workflows/android.yml").read_text(encoding="utf-8")

assert 'android:label="FINAL SORTIE"' in manifest
assert 'android:icon="@drawable/ic_launcher"' in manifest
assert 'android:roundIcon="@drawable/ic_launcher"' in manifest
assert view.count('"FINAL SORTIE"') >= 3
assert 'FINAL SORTIE 0.6.4-alpha' in view
assert '"SHATTERWAKE"' not in view, "Old display name survives in runtime"
assert 'getSharedPreferences("shatterwake_save_v1"' in view, "Existing progress must survive rename"
assert "applicationId 'de.shatterwake.game'" in gradle
assert "namespace 'de.shatterwake.game'" in gradle
assert "?: '9'" in gradle and "?: '0.6.4-alpha'" in gradle
assert "SHATTERWAKE_VERSION_NAME=0.6.4-alpha.$GITHUB_RUN_NUMBER" in workflow
assert 'tag=v0.6.4-alpha' in workflow
assert 'expected="12fc96a90b8ca79d7e01f8e1a420f97e872ec71dc32ebe2b10ba59919776fc06"' in workflow

icon = root / "app/src/main/res/drawable-nodpi/ic_launcher.webp"
data = icon.read_bytes()
assert data[:4] == b"RIFF" and data[8:16] == b"WEBPVP8 ", "Icon should be lossy WebP"
assert data[23:26] == b"\x9d\x01\x2a", "Invalid WebP VP8 frame"
w, h = struct.unpack_from("<HH", data, 26)
assert (w & 0x3fff, h & 0x3fff) == (384, 384), "Icon dimensions must be 384x384"
expected_hash = "11671c33382e9e6bc8417117d595fb1df7b441d20533ec42423c1cf2d8990170"
assert hashlib.sha256(data).hexdigest() == expected_hash, "Unexpected launcher bitmap"
assert not (root / "app/src/main/res/drawable/ic_launcher.xml").exists(), "Legacy icon still shadowing the new launcher"

if len(sys.argv) > 1:
    with zipfile.ZipFile(sys.argv[1]) as archive:
        icons = [path for path in archive.namelist() if path.endswith("/ic_launcher.webp")]
        assert len(icons) == 1, ("APK must package exactly one new launcher icon", icons)
        assert hashlib.sha256(archive.read(icons[0])).hexdigest() == expected_hash
    print("PASS: FINAL SORTIE launcher icon packaged in APK")
else:
    print("PASS: FINAL SORTIE branding, icon SHA-256 and save/update identifiers")
