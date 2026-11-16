#!/usr/bin/env python3
"""Assert exact approved Kettenwurm images are shipped unchanged in source or APK.

A self-contained stdlib checker; no third-party dependencies on CI.
"""
from pathlib import Path
import hashlib
import struct
import sys
import zipfile

ROOT = Path(__file__).resolve().parents[1]
BOSS = ROOT / "app/src/main/assets/rendered/boss"
CHECKSUMS = ROOT / "art/bosses/kettenwurm/damage_output/SHA256SUMS.txt"
APP_PREFIX = "assets/rendered/boss/"


def approved_hashes():
    values = {}
    for row in CHECKSUMS.read_text(encoding="utf-8").splitlines():
        h, name = row.split(maxsplit=1)
        if name.startswith("boss_type_") and name.endswith(".png"):
            values[name] = h
    assert len(values) == 20, "Expected all 20 approved Blender image hashes"
    return values


def validate_png(blob, name, digest):
    assert hashlib.sha256(blob).hexdigest() == digest, name + ": changed from approved render"
    assert blob[:8] == b"\x89PNG\r\n\x1a\n", name + ": invalid PNG"
    assert struct.unpack(">II", blob[16:24]) == (1024, 1024), name + ": not 1024px"
    assert blob[25] == 6, name + ": not RGBA PNG"


def main():
    hashes = approved_hashes()
    if len(sys.argv) > 1:
        apk = Path(sys.argv[1])
        with zipfile.ZipFile(apk) as archive:
            for name, digest in sorted(hashes.items()):
                validate_png(archive.read(APP_PREFIX + name), name, digest)
        print("PASS: all 20 approved unmodified Blender sprites inside actual APK")
    else:
        for name, digest in sorted(hashes.items()):
            validate_png((BOSS / name).read_bytes(), name, digest)
        print("PASS: 20 approved unmodified Blender 1024 RGBA assets staged")


if __name__ == "__main__":
    main()
