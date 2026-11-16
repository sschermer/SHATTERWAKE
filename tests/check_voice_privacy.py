#!/usr/bin/env python3
"""Protect FINAL SORTIE microphone consent, offline-only implementation and identity."""
from pathlib import Path
import os
import subprocess
import sys
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
MANIFEST=ROOT/"app/src/main/AndroidManifest.xml"
JAVA=ROOT/"app/src/main/java/de/shatterwake/game"
ANDROID="{http://schemas.android.com/apk/res/android}"


def check_sources():
    root=ET.parse(MANIFEST).getroot()
    permissions={p.get(ANDROID+"name") for p in root.findall("uses-permission")}
    assert "android.permission.RECORD_AUDIO" in permissions
    assert "android.permission.VIBRATE" in permissions
    assert "android.permission.INTERNET" not in permissions
    queries=root.find("queries")
    assert queries is not None
    assert any(a.get(ANDROID+"name")=="android.speech.RecognitionService"
               for a in queries.iter("action"))
    c=(JAVA/"VoiceCommandController.java").read_text(encoding="utf-8")
    g=(JAVA/"GameView.java").read_text(encoding="utf-8")
    m=(JAVA/"MainActivity.java").read_text(encoding="utf-8")
    assert "createOnDeviceSpeechRecognizer" in c
    assert "isOnDeviceRecognitionAvailable" in c
    assert "createSpeechRecognizer(" not in c
    assert "EXTRA_PREFER_OFFLINE" in c
    assert "requestPermissions(" in c
    assert "LISTEN_MS=3300L" in c
    assert "acceptsSecondFinger(" in g
    assert "MotionEvent.ACTION_POINTER_DOWN" in g
    assert "voice_enabled" in g
    assert "onVoiceRecognized" in g and "useAbility(which)" in g
    assert "voiceController.cancelListening()" in m
    assert "de.shatterwake.game" in m
    assert "android:label" in MANIFEST.read_text() and "FINAL SORTIE" in MANIFEST.read_text()
    assert "shatterwake_save_v1" in g
    assert not any(p.name.startswith("*.") for p in JAVA.iterdir())
    print("PASS: opt-in on-device speech only; no INTERNET permission or mic recordings")


def check_apk(path):
    sdk=os.environ.get("ANDROID_HOME","/usr/local/lib/android/sdk")
    aapt2=Path(sdk)/"build-tools/36.0.0/aapt2"
    assert aapt2.exists(),aapt2
    badging=subprocess.check_output([str(aapt2),"dump","badging",str(path)],text=True)
    assert "package: name='de.shatterwake.game'" in badging,badging[:500]
    assert "android.permission.RECORD_AUDIO" in badging,badging[:900]
    assert "android.permission.INTERNET" not in badging,badging[:900]
    assert "FINAL SORTIE" in badging,badging[:900]
    print("PASS: packaged Android identity, user-controlled mic and no INTERNET permission")


if __name__=="__main__":
    check_sources()
    if len(sys.argv)>1:
        check_apk(Path(sys.argv[1]))
