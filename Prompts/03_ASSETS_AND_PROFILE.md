# File 03 — Icons, favicon, illustration, extra resources, protocol profile, scripts, build wrapper, docs index (v3.0)

Create each file at the exact path. All assets are original artwork (an "eclipse" ring mark), not the vendor's logo.

## 1. Launcher icon (adaptive, vector — no PNGs needed because minSdk is 26)

### `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_foreground" />
</adaptive-icon>
```

### `app/src/main/res/drawable/ic_launcher_background.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#344C63" android:pathData="M0,0h108v108h-108z" />
</vector>
```

### `app/src/main/res/drawable/ic_launcher_foreground.xml`
Eclipse ring: an outer circle with an off-centre inner cut-out (even-odd fill), kept inside the 66 dp safe zone.
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path
        android:fillColor="#FFFFFF"
        android:fillType="evenOdd"
        android:pathData="M32,54 a22,22 0 1,1 44,0 a22,22 0 1,1 -44,0 z M39,52 a18,18 0 1,1 36,0 a18,18 0 1,1 -36,0 z" />
</vector>
```

## 1b. Extra resources (v3.0)

### `app/src/main/res/drawable/ic_stat_assist.xml`
Status-bar icon for the Hearing Assist notification. Status-bar icons must be white on transparent.
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp" android:height="24dp" android:viewportWidth="24" android:viewportHeight="24">
    <path
        android:fillColor="#FFFFFF"
        android:fillType="evenOdd"
        android:pathData="M4,12 a8,8 0 1,1 16,0 a8,8 0 1,1 -16,0 z M7,11 a5.5,5.5 0 1,1 11,0 a5.5,5.5 0 1,1 -11,0 z" />
</vector>
```

### `app/src/main/res/drawable/ic_shortcut.xml`
Icon for the three launcher shortcuts.
```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="48dp" android:height="48dp" android:viewportWidth="48" android:viewportHeight="48">
    <path android:fillColor="#344C63" android:pathData="M0,24 a24,24 0 1,0 48,0 a24,24 0 1,0 -48,0 z" />
    <path
        android:fillColor="#FFFFFF"
        android:fillType="evenOdd"
        android:pathData="M12,24 a12,12 0 1,1 24,0 a12,12 0 1,1 -24,0 z M16,23 a9.5,9.5 0 1,1 19,0 a9.5,9.5 0 1,1 -19,0 z" />
</vector>
```

### `app/src/main/res/xml/shortcuts.xml`
Long-press the launcher icon: Hearing Assist, Equalizer, Relaxation. `MainActivity` reads the `open` extra (`assist`, `equalizer`, `relax`).
```xml
<?xml version="1.0" encoding="utf-8"?>
<shortcuts xmlns:android="http://schemas.android.com/apk/res/android">
    <shortcut
        android:shortcutId="assist" android:enabled="true" android:icon="@drawable/ic_shortcut"
        android:shortcutShortLabel="@string/shortcut_assist_short" android:shortcutLongLabel="@string/shortcut_assist_long">
        <intent android:action="android.intent.action.VIEW"
            android:targetPackage="com.eclipsecompanion.app" android:targetClass="com.eclipsecompanion.app.MainActivity">
            <extra android:name="open" android:value="assist" />
        </intent>
    </shortcut>
    <shortcut
        android:shortcutId="equalizer" android:enabled="true" android:icon="@drawable/ic_shortcut"
        android:shortcutShortLabel="@string/shortcut_eq_short" android:shortcutLongLabel="@string/shortcut_eq_long">
        <intent android:action="android.intent.action.VIEW"
            android:targetPackage="com.eclipsecompanion.app" android:targetClass="com.eclipsecompanion.app.MainActivity">
            <extra android:name="open" android:value="equalizer" />
        </intent>
    </shortcut>
    <shortcut
        android:shortcutId="relax" android:enabled="true" android:icon="@drawable/ic_shortcut"
        android:shortcutShortLabel="@string/shortcut_relax_short" android:shortcutLongLabel="@string/shortcut_relax_long">
        <intent android:action="android.intent.action.VIEW"
            android:targetPackage="com.eclipsecompanion.app" android:targetClass="com.eclipsecompanion.app.MainActivity">
            <extra android:name="open" android:value="relax" />
        </intent>
    </shortcut>
</shortcuts>
```

### `app/src/main/res/xml/data_extraction_rules.xml`
Belt and braces next to `allowBackup="false"`: nothing is copied to cloud backup or to a new phone.
```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="root" path="." />
        <exclude domain="file" path="." />
        <exclude domain="database" path="." />
        <exclude domain="sharedpref" path="." />
        <exclude domain="external" path="." />
    </cloud-backup>
    <device-transfer>
        <exclude domain="root" path="." />
        <exclude domain="file" path="." />
        <exclude domain="database" path="." />
        <exclude domain="sharedpref" path="." />
        <exclude domain="external" path="." />
    </device-transfer>
</data-extraction-rules>
```

## 2. Favicon and illustration (web-style assets, also used in docs/README)

### `assets/favicon.svg`
```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" width="64" height="64" role="img" aria-label="Eclipse Companion">
  <rect width="64" height="64" rx="14" fill="#344C63"/>
  <path fill="#F6F6F4" fill-rule="evenodd"
        d="M14,32 a18,18 0 1,1 36,0 a18,18 0 1,1 -36,0 z M20,30 a14.5,14.5 0 1,1 29,0 a14.5,14.5 0 1,1 -29,0 z"/>
</svg>
```

### `assets/eclipse-illustration.svg`
```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 440 200" width="440" height="200" role="img" aria-label="White earbuds and charging case">
  <rect x="141" y="60" width="158" height="116" rx="44" fill="#EEEEEC" stroke="#D7D7D2" stroke-width="3"/>
  <line x1="141" y1="108" x2="299" y2="108" stroke="#D7D7D2" stroke-width="3"/>
  <circle cx="220" cy="136" r="5" fill="#344C63"/>
  <g stroke="#D7D7D2" stroke-width="3" fill="#FFFFFF">
    <rect x="58" y="80" width="22" height="84" rx="11"/>
    <circle cx="70" cy="72" r="30"/>
    <rect x="360" y="80" width="22" height="84" rx="11"/>
    <circle cx="370" cy="72" r="30"/>
  </g>
  <circle cx="70" cy="72" r="13" fill="#EEEEEC"/>
  <circle cx="370" cy="72" r="13" fill="#EEEEEC"/>
</svg>
```

## 3. Protocol profile (ships with NOTHING verified)

Every byte value is intentionally empty: they must come from the user's own capture (master prompt section 4). Do **not** fill any of them in.

### `app/src/main/assets/protocol/eclipse_r7065.json`
```json
{
  "schema": 1,
  "name": "Ronin Eclipse R-7065 (empty template)",
  "verified": false,
  "notes": "No commands are known or verified. Capture them from your own earbuds and import a completed profile.",
  "identify": {
    "namePrefixes": ["Eclipse", "Ronin"],
    "serviceUuids": []
  },
  "transport": {
    "type": "gatt",
    "service": "",
    "write": "",
    "notify": "",
    "writeNoResponse": false,
    "sppUuid": "00001101-0000-1000-8000-00805F9B34FB"
  },
  "framing": { "header": "", "lengthByte": false, "checksum": "none", "checksumFrom": 0 },
  "modes": {},
  "commands": {
    "readBattery": { "request": "", "ack": null, "verified": false },
    "readMode": { "request": "", "ack": null, "verified": false },
    "setMode": { "request": "", "ack": null, "verified": false },
    "readFirmware": { "request": "", "ack": null, "verified": false },
    "setEq": { "request": "", "ack": null, "verified": false },
    "setTouch": { "request": "", "ack": null, "verified": false },
    "setGaming": { "request": "", "ack": null, "verified": false },
    "setName": { "request": "", "ack": null, "verified": false },
    "setTransparencyLevel": { "request": "", "ack": null, "verified": false },
    "setAncLevel": { "request": "", "ack": null, "verified": false },
    "setWearDetection": { "request": "", "ack": null, "verified": false },
    "setVoicePrompts": { "request": "", "ack": null, "verified": false },
    "factoryReset": { "request": "", "ack": null, "verified": false }
  }
}
```

### `docs/PROFILE_FORMAT.md`
```markdown
# Protocol profile format (schema 1)

- `transport.type`: `gatt` (BLE) or `spp` (Bluetooth Classic serial). `gatt` needs `service`, `write`, and usually `notify` UUIDs.
- `framing`: frame = `header` + optional length byte (= body size) + body + optional checksum. `checksum` is `none`, `sum8`, `xor8` or `crc16modbus` (low byte first), computed from frame byte `checksumFrom` to the end of the body.
- `commands.*.request` is a template of hex bytes and tokens. `{name}` inserts one byte (0-255). `{name*}` inserts ASCII text.
  Tokens used by the app: setMode `{mode}`; setEq `{b0}`..`{b9}` (value = round(dB / stepDb) + offset); setTouch `{side}` `{gesture}` `{action}`;
  setGaming `{on}`; setTransparencyLevel `{level}`; setAncLevel `{level}`; setWearDetection `{on}`; setVoicePrompts `{on}`; setName `{name*}`.
- `commands.*.ack` is an optional hex prefix of the earbuds' reply body. With an ack the app waits up to 3 s and shows "Confirmed" only on a match.
- `commands.*.verified` must be `true` for the app to ever send it. Set it only after reproducing the command twice and seeing the earbuds react.
- `parse.battery`: `{ "prefix": "hex", "left": index, "right": index, "case": index, "invalid": [255] }`. `parse.mode`: `{ "prefix": "hex", "index": n, "map": { "ANC": v, "TRANSPARENCY": v, "NORMAL": v } }`. `parse.firmware`: `{ "prefix": "hex", "index": n }`.
- `eq`: `{ "bands": 10, "offset": 0, "stepDb": 1.0 }`. `touch`: `{ "sides": {"LEFT": v, "RIGHT": v}, "gestures": {"TAP": v, "DOUBLE": v, "TRIPLE": v, "LONG": v}, "actions": {"Play/Pause": v, ...} }`. `transparencyLevel`: `{ "min": 0, "max": 10 }`. `ancLevel`: `{ "min": 0, "max": 3 }` (use the real range you captured; both ranges must satisfy 0 ≤ min ≤ max ≤ 255). The app shows a level slider only when the matching command is verified **and** the range is defined.

Everything above is a structure description only. No value is known for the Eclipse R-7065 until captured.
```

## 4. Scripts

### `scripts/check_no_network.sh`
```bash
#!/usr/bin/env bash
# Fails if the merged debug manifest requests the INTERNET permission.
set -euo pipefail
./gradlew :app:processDebugMainManifest -q
M=$(find app/build -path '*merged_manifests*' -name AndroidManifest.xml | head -n1)
if [ -z "$M" ]; then echo "merged manifest not found"; exit 2; fi
if grep -q "android.permission.INTERNET" "$M"; then echo "FAIL: INTERNET permission present in $M"; exit 1; fi
echo "OK: no INTERNET permission in $M"
```

### `scripts/audit_sources.sh`
Fails if any network, analytics, ad or dynamic-code reference sneaks into the sources or Gradle files.
```bash
#!/usr/bin/env bash
# Run from the project root: bash scripts/audit_sources.sh
set -euo pipefail
PATTERN='android\.permission\.INTERNET|okhttp|retrofit|volley|firebase|crashlytics|admob|appsflyer|mixpanel|sentry|google-services|java\.net\.URL|HttpURLConnection|HttpsURLConnection|DexClassLoader|PathClassLoader|WebView'
if grep -RIEn "$PATTERN" app/src/main app/build.gradle.kts build.gradle.kts settings.gradle.kts \
     --include='*.kt' --include='*.kts' --include='*.xml' --include='*.json'; then
  echo "FAIL: network, analytics, ads or dynamic-code reference found (see above)"; exit 1
fi
echo "OK: no network, analytics, ads or dynamic-code references found"
```

## 5. Gradle wrapper and repository hygiene

The Gradle wrapper **scripts and jar are binary/generated**, so the generator cannot type them. Create the properties file below, then tell the user to run `gradle wrapper --gradle-version 8.9` once (or let Android Studio generate `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` when it syncs). Gradle 8.9 is the minimum for AGP 8.7.x.

### `gradle/wrapper/gradle-wrapper.properties`
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```
(This URL is used only on the developer's computer at build time. The app itself has no internet permission.)

### `.gitignore`
```
*.iml
.gradle/
/local.properties
/.idea/
.DS_Store
/build/
/app/build/
/captures/
.externalNativeBuild/
.cxx/
*.keystore
*.jks
keystore.properties
```
Never commit a signing keystore or `keystore.properties`.

## 6. Docs (generate these files)

Create every file in `docs/`. Three come from other places; **all the rest are written out in full in file 05 (`05_DOCS_COMPLETE_TEXT.md`): copy them verbatim** and leave result tables empty.

| `docs/` file | Source |
|---|---|
| `CAPTURE_PROTOCOL.md` | master prompt section 4, verbatim |
| `PRIVACY.md` | master prompt section 6, verbatim |
| `PROFILE_FORMAT.md` | the block in section 3 above, verbatim |
| `README.md` | file 05 |
| `DEVICE_CAPABILITY_MATRIX.md` | file 05 |
| `OFFICIAL_FEATURE_PARITY.md` | file 05 |
| `SOURCE_AND_ASSUMPTION_REGISTER.md` | file 05 |
| `SECURITY_REVIEW.md` | file 05 |
| `TEST_PLAN.md` | file 05 (unit list, physical checklist, call-audio regression plan, QA matrix) |
| `TROUBLESHOOTING.md` | file 05 (same Q and A as the in-app Help screen) |
| `KNOWN_LIMITATIONS.md` | file 05 |
| `AUDIO_ROUTING_AND_HEARING_ASSIST.md` | file 05 |
| `AUDIO_SETTINGS_SCOPE.md` | file 05 |
| `DEPENDENCY_AND_LICENSE_INVENTORY.md` | file 05 |
| `RELEASE_CHECKLIST.md` | file 05 |
| `PHYSICAL_DEVICE_TEST_RESULTS.md` | file 05 (empty table) |
| `CHANGES_FROM_PROMPT.md` | file 05 (starts as "None yet."; Gemini lists every deviation here) |
