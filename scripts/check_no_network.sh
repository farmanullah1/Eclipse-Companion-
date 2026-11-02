#!/usr/bin/env bash
# Fails if the merged debug manifest requests the INTERNET permission.
set -euo pipefail
gradle :app:processDebugManifest -q
M=$(find app/build -path '*merged_manifests*' -name AndroidManifest.xml | head -n1)
if [ -z "$M" ]; then echo "merged manifest not found"; exit 2; fi
if grep -q "android.permission.INTERNET" "$M"; then echo "FAIL: INTERNET permission present in $M"; exit 1; fi
echo "OK: no INTERNET permission in $M"
