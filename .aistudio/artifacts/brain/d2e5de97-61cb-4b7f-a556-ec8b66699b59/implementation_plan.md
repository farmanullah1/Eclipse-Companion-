# Eclipse Companion — Pro Audio Utility Suite & System Integrations

A comprehensive blueprint for upgrading Eclipse Companion with professional acoustic utility tools, system-level Android integrations (Home Screen Widget & Quick Settings Tile), proactive hearing protection, and visual themes—all operating with 100% device-local privacy.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> All new utility features, acoustic measurements, widgets, and Quick Settings tiles strictly preserve the app's zero-network guarantee (`android.permission.INTERNET` remains completely excluded). All audio generation, BLE telemetry, and exposure metrics run strictly on-device.

- **Earbud Utility Suite Confirmed**:
  - **Find My Earbuds**: Real-time BLE RSSI signal strength radar coupled with high-pitched pulsed acoustic locator beeps (individually triggerable for Left, Right, or Both earbuds).
  - **Ear Tip Seal Fit Test**: Automated acoustic seal diagnostic that plays calibration sweeps and measures low-frequency containment to ensure optimal ANC performance.
  - **Hearing Health & Noise Dose Monitor**: Calculates cumulative daily sound exposure against World Health Organization (WHO) safety thresholds (85 dBA 8-hour daily dose) with real-time exposure gauges.
  - **Spatial Audio Soundstage Studio**: 3D acoustic room simulation allowing listeners to adjust perceived room size, stereo width, and reverberant warmth.
- **System Integrations & UI Upgrades Confirmed**:
  - **Android Home Screen Widget**: Quick glance widget displaying real-time Left, Right, and Case battery levels alongside immediate one-tap ANC mode switching.
  - **Quick Settings Tile (`TileService`)**: System notification shade tile enabling instant ANC / Transparency cycling without opening the app.
  - **Smart Battery Health & Low-Power Alerts**: Configurable alerts when earbuds reach 20% and 10% battery, plus an optional battery preservation mode.
  - **Visual Theme Customization**: Three tailored aesthetic palettes: **Obsidian Eclipse** (default dark luxury), **Cyberpunk Neon** (high-contrast cyan/magenta), and **Studio Gold** (analog brass/champagne).

---

## 1. Overview & Core Concept

- **What It Does**: Transforms Eclipse Companion from a hardware controller into an all-in-one audiophile command center. Users gain emergency acoustic locator tools to find misplaced earbuds, acoustic verification of their silicone ear tip fit, proactive daily hearing protection meters, seamless home screen and notification shade controls, and tailored visual personalization.
- **Target Audience**: Daily earbud users, commuters, fitness enthusiasts, and audio professionals who want seamless system shortcuts and smart acoustic safety.
- **Key Value**: Maximum convenience with zero cloud dependencies: find earbuds instantly, control modes from your home screen or status bar, and protect your hearing without tracking.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Find My Earbuds (Radar & Chirp)**:
   - User opens **Find My Earbuds** from the Home or Sound Tools screen.
   - Screen displays an animated circular radar pulsing with live proximity cues ("Nearby", "Getting Closer", "Very Close" based on BLE RSSI).
   - Independent action buttons: **Play Left Chirp** and **Play Right Chirp** play a piercing, high-frequency locator tone with gradual volume ramp-up for safety.

2. **Ear Tip Seal Fit Test**:
   - User initiates **Fit Test** after inserting earbuds.
   - A 5-second acoustic frequency sweep plays in both ears.
   - Evaluates low-end bass resonance retention and provides immediate seal verdict: **Good Seal** (green) or **Adjust Fit / Change Tip Size** (amber) for each ear.

3. **Hearing Health & Noise Dose Meter**:
   - A dedicated **Hearing Health** card on the Sound Settings screen.
   - Shows a circular progress gauge tracking `% of Safe Daily Dose` (based on WHO 85 dB benchmark).
   - Alerts user if listening volume and duration approach 100% daily dose.

4. **Home Screen Widget & Quick Settings Tile**:
   - Users can place a 4x2 or 2x2 widget on their Android home screen showing live battery levels for Left, Right, and Case, with direct buttons for **Noise Reduction**, **Off**, and **Transparency**.
   - Pulling down the Android notification shade reveals the **Ronin ANC** quick settings tile, cycling listening modes with one tap.

5. **Theme Customization**:
   - User selects **Themes** in Settings:
     - **Obsidian Eclipse**: Deep dark `#121314` with `#5D5FEF` violet accent.
     - **Cyberpunk Neon**: Jet black `#0B0C10` with `#00F0FF` cyan and `#FF003C` magenta highlights.
     - **Studio Gold**: Warm charcoal `#1A1918` with `#E5B869` brass acoustic accents.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Locator Chirp Safety Ramping**:
  - *Chosen Approach*: The acoustic locator chirp starts at -18 dBFS and ramps up over 3 seconds, preceded by a prominent "Take earbuds out of your ears before sounding alarm" safety warning.
  - *Why*: Prevents acoustic shock or hearing damage if the user triggers the chirp while an earbud is still in their ear.
- **Decision 2: AppWidget Implementation via Clean Jetpack Architecture**:
  - *Chosen Approach*: Use standard Android `AppWidgetProvider` with `RemoteViews` for instantaneous rendering and wide compatibility across Android 8 through 16 without heavy external widget dependencies.
  - *Why*: Fast cold-start, low memory footprint, and zero battery drain while the screen is off.
- **Decision 3: Zero-Network Noise Exposure Calculation**:
  - *Chosen Approach*: Exposure dose calculations run locally using the Android `AudioManager` volume index and active playback duration.
  - *Why*: No background microphones or audio streaming data are sent anywhere; computations remain 100% private.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        System Entry Points                             │
│  ┌───────────────────────┐  ┌─────────────────┐  ┌──────────────────┐  │
│  │ MainActivity (Compose)│  │ AppWidgetProvider│  │ QuickSettingsTile│  │
│  │  - FindMyEarbudsView  │  │  - Battery Bars │  │  - ANC Cycling   │  │
│  │  - FitTestView        │  │  - Mode Toggles │  └──────────────────┘  │
│  │  - HearingDoseCard    │  └─────────────────┘                        │
│  │  - ThemeSelector      │                                             │
│  └───────────────────────┘                                             │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │ Events & StateFlows
┌────────────────────────────────────▼───────────────────────────────────┐
│                            MainViewModel                               │
│  - findMyEarbudsRadarRssi            - activeTheme: StateFlow<AppTheme>│
│  - triggerLocatorChirp(side)         - dailyNoiseDosePercent           │
│  - runSealFitTest()                  - lowBatteryAlertThreshold        │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────▼───────────────────────────────────┐
│              Hardware Repositories & Local Audio Engines                │
│  - AndroidBluetoothRepository (RSSI Telemetry & Device Commands)       │
│  - SoundToolsEngine (Safe Locator Chirps & Fit Sweeps)                 │
│  - LocalSettingsRepository (Theme, Dose History & Preferences)         │
└────────────────────────────────────────────────────────────────────────┘
```

### Component & File Mapping

1. **`FindMyEarbudsScreen.kt`**:
   - Interactive radar canvas with distance rings.
   - Independent L / R chirp triggers with volume ramp and safety warnings.
2. **`EarTipFitTestScreen.kt`**:
   - Multi-stage diagnostic sweep with seal feedback animation.
3. **`HearingHealthCard.kt`**:
   - Daily WHO noise dose gauge, volume exposure warnings, and safe volume limiter toggle.
4. **`EclipseAppWidgetProvider.kt` & `EclipseTileService.kt`**:
   - Home screen widget layout and Quick Settings notification shade tile.
5. **`Theme.kt` & Theme Engine**:
   - Dynamic theme state supporting Obsidian, Cyberpunk, and Studio Gold palettes.
6. **`MainViewModel.kt`**:
   - Coordinates locator audio, fit test diagnostics, dose calculations, and widget sync.
