# Release Checklist — Eclipse Companion (R-7065)

Prior to public release or internal deployment, verify every step:

## 1. Identity & Compliance
- [x] Application ID set to `com.aistudio.eclipsecompanion.ecrlwt` (no trademark infringement).
- [x] App title matches resource `app_name`: "Eclipse Companion".
- [x] Explicitly identified as an independent companion app with no claim of official endorsement.
- [x] Launcher icon configured as adaptive vector layers with high-density mipmaps.

## 2. Zero-Network & Privacy Audit
- [x] `android.permission.INTERNET` is completely absent from `AndroidManifest.xml`.
- [x] No third-party tracking, crash reporting, ad SDKs, or cloud analytics included.
- [x] Microphone access requested strictly at runtime when user initiates Hearing Assist.
- [x] Captured microphone audio is volatile in RAM ring buffer; no disk logging or uploads.

## 3. Protocol & Hardware Truth
- [x] Battery indicators show `ReportedValue.Unavailable` when disconnected; zero fabricated percentages.
- [x] ANC / Transparency / Normal modes reflect confirmed hardware state with in-flight pending indicators.
- [x] 10-Band EQ verified with independent slider isolation (editing one band does not touch others).
- [x] OTA firmware flashing is safely locked offline to prevent unverified hardware bricking.

## 4. Audio Engine Safety
- [x] Hearing Assist defaults to conservative safe output gain (15%).
- [x] Soft peak limiter enabled by default with hyperbolic clamping.
- [x] Prominent Emergency Stop button displayed during active audio routing.
- [x] Audio focus cleanly released on stop, incoming calls, or headphone disconnection.

## 5. Build & Test Verification
- [x] `compile_applet` passes without compiler warnings or broken resources.
- [x] Unit test suite (`gradle :app:testDebugUnitTest`) passes 100% of tests.
- [x] Release signing configuration points to valid keystore without hardcoded credentials.
