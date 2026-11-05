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
- **`V4ScreenshotAlignmentTest.kt`**:
  - Test 1: `eqFrequenciesMatchV4Specification` verifies exact 10 bands `[32, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000]`.
  - Test 2: `eqPresetsIncludeMoodAndArtistsGroups` verifies Mood (6), Artists (7), and Custom groups.
  - Test 3: `touchGestureLabelsMatchV4OfficialNames` verifies One Tap, Two Tap, Three Tap, Tap & Hold.
  - Test 4: `touchActionsIncludeAncSwitchingAndNoAction` verifies standard and ignored duplicate actions.
  - Test 5: `listeningModesIncludeOffState` verifies Noise Reduction, Off, Transparency order and labels.
  - Test 6: `maintenanceTonesStayShortAndCapped` verifies duration and amplitude capping (60s @ 0.25f, 30s @ 0.50f).
- **`EqualizerSuiteEnhancementsTest.kt`**:
  - Test 1: `ten_bands_conform_strictly_to_iso_standards` confirms 10 ISO center frequencies.
  - Test 2: `preset_profile_structure_and_safe_clamping` validates custom preset structure, category, and gains.
  - Test 3: `hearing_test_compensation_calculation_boosts_hearing_loss_frequencies` validates threshold delta compensation math and safe clamp limits.
- **`ProUtilitySuiteTest.kt`**:
  - Test 1: `findMyEarbuds_and_fitTest_tools_have_capped_durations_and_safe_amplitudes` verifies acoustic chirp safety caps (15s @ 0.65f) and logarithmic seal test (5s @ 0.35f).
  - Test 2: `theme_options_have_valid_identifiers_and_labels` verifies Obsidian Eclipse, Cyberpunk Neon, and Studio Gold theme definitions.
  - Test 3: `widget_broadcast_intent_actions_are_defined_and_consistent` verifies widget broadcast actions for listening modes.
  - Test 4: `who_noise_dose_calculation_formula_is_accurate` verifies 85 dBA 100% daily dose calculation and doubling rate.

## 2. Interactive QA & Physical Checks
- Clean Dust and Drain Moisture start only with the tick and an earbuds route, stop on Stop / call / leaving screen / home button, and never play through the phone speaker.
- Vertical EQ drag changes only its own band (also verified with TalkBack custom accessibility actions).
- Custom EQ slot save and reload after app restart.
- Pill navigation bar displays properly and remains accessible at font scale 2.0.
- Status-bar icons remain readable in both dark and light themes.
- Listening mode order strictly follows `Noise Reduction · Off · Transparency`.
- Offline integrity: App operates seamlessly in Airplane mode with zero network access.

