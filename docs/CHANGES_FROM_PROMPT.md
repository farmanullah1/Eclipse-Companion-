# Changes from the provided prompt files

This document logs all deviations, compile adaptations, and platform adjustments made to the codebase in accordance with Section 12 Rule 5 and Section 11 of `00_MASTER_PROMPT.md` and `07_V4_SCREENSHOT_ALIGNMENT_AND_NEW_FEATURES.md`.

## 1. Secrets Gradle Plugin
- Created `/.env.example` and `/.env` with `GEMINI_API_KEY=your_api_key_here` to satisfy the pre-configured Secrets Gradle Plugin (`com.google.android.libraries.mapsplatform.secrets-gradle-plugin`) in `app/build.gradle.kts`.

## 2. Package Name & Namespace
- Package retained as `com.example` to preserve namespace compatibility with the existing Android project structure, while application identity is `com.aistudio.eclipsecompanion.ecrlwt` and app title is `Eclipse Companion`.

## 3. Acoustic Maintenance Tones (07_V4 Section 4)
- Added `durationSeconds` (60 for Clean Dust, 30 for Drain Moisture) and amplitude limits (`0.25f` for Clean Dust, `0.50f` for Drain Moisture) to `ToolMode` in `SoundToolsEngine.kt`.
- Added `override fun onStop()` in `MainActivity.kt` ensuring all maintenance audio and relaxation tones automatically stop when the application is backgrounded.

## 4. Screenshot 1–4 Visual Alignment (Prompt 07)
- Integrated `DevicesHero` layout on Home for disconnected state (matching Screenshot 1) with `CONNECT ›` pill button and `+ Add Device`.
- Integrated `PillNavBar` floating pill bottom bar (`Home`, `News`, `Products`, `Profile`) and circular `Support` button (matching Screenshot 1).
- Updated `FREQUENCIES` in `EqProfile.kt` to 10 bands starting at 32Hz and including 63Hz (`32, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000`).
- Added Mood (`Default`, `Energetic`, `Calm`, `Chill`, `Focus`, `Sad`), Artists/Genres, and Custom presets with 10-bar mini-EQ visualizer thumbnails.
- Updated Touch Settings screen with `Left` / `Right` earbud tiles and official gesture names (`One Tap`, `Two Tap`, `Three Tap`, `Tap & Hold`) and actions (`No Action`, `ANC Switching`).
- Added offline `ProductsScreen` and `TipsScreen` to provide self-contained offline manuals without requiring any network permissions.
