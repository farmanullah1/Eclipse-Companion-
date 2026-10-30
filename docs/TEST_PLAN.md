# Comprehensive Test Plan

## 1. Unit Test Matrix
- **`EqRegressionTest.kt`**:
  - Test 1: Editing any single EQ band by `+3.0 dB` updates only that targeted band and leaves every other frequency band byte-for-byte unchanged.
  - Test 2: Injected `NaN` and `Infinite` gains are strictly rejected with `IllegalArgumentException`.
  - Test 3: Excessive gain boosts or cuts are clamped strictly to `[-12.0, +12.0] dB`.
- **`BatteryStateTest.kt`**:
  - Test 1: Missing battery telemetry is reported as `ReportedValue.Unavailable` and never defaults to `0%`.
  - Test 2: Out-of-bounds readings (`<0` or `>100`) are rejected and marked unavailable.
  - Test 3: Left, Right, and Case values are verified to be completely decoupled.
- **`AudioProcessorTest.kt`**:
  - Test 1: Full-scale maximum amplitude inputs (`32767`) are softly compressed by the hyperbolic limiter so output never clips digitally.
  - Test 2: Injected out-of-bounds gain multipliers are clamped to safe internal limits.
- **`ExampleRobolectricTest.kt`**:
  - Context resources test verifies `app_name` string matches "Eclipse Companion".

## 2. Interactive QA & Screen Checklist
- **Devices Screen**: Connect/Disconnect lifecycle, BLE discovery scan indicator, compatibility badge.
- **Home Dashboard**: Battery cards, Noise Reduction / Off / Transparency pill, feature tiles.
- **Audio Presets (Sound Settings)**: 10 vertical sliders with gain indicator, Mood presets, Pakistani Artist presets, Custom presets.
- **Touch Settings**: Left/Right earbud tab switching, One Tap, Two Tap, Three Tap, Tap & Hold dropdowns, Apply & Reset.
- **Hearing Assist**: Output route detection, microphone permission prompt, emergency stop button, sensitivity/gain sliders, feedback warning.
- **Acoustic Tools & Relaxation**: Clean Dust 15s pulse, Drain Moisture 15s pulse, soothing ambient sound synthesizer with timer.
- **Diagnostics**: Real-time GATT service logs, telemetry metrics, clipboard export with redacted MAC address.
- **Offline Integrity**: App operates seamlessly in Airplane mode.
