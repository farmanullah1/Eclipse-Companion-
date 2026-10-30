# Eclipse Companion — Ronin Eclipse (R-7065) Offline Companion

Eclipse Companion is a native Android companion application specifically engineered for **Ronin Eclipse (R-7065) Wireless Earbuds (Snow White)**. Built from the ground up to be 100% offline-first, private, and transparent, with zero logins, zero cloud dependencies, and zero fake UI metrics.

## Key Features

1. **Hardware Device Management**:
   - Real BLE discovery and connection lifecycle with serialized GATT operation queue.
   - Separate audio route checking (A2DP / Headset) and GATT companion link.
   - Independent battery telemetry for Left and Right earbuds (no fabricated 0% defaults).

2. **Listening Mode Control**:
   - Noise Reduction (ANC), Off (Normal), and Ambient Transparency modes.
   - Confirmed state machine with pending in-flight feedback.

3. **10-Band Independent Equalizer**:
   - 10 frequency sliders: 32Hz, 63Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz.
   - **Regression Tested**: Adjusting any one slider leaves every other band strictly unchanged.
   - Includes Mood Presets (Default, Energetic, Calm, Chill, Focus, Sad) and Pakistani Artist Presets (Hasan Raheem, Annural Khalid, Asim Azhar, Abdul Hannan), plus custom user slots.

4. **Touch Gesture Customization**:
   - Custom mapping for Left & Right earbuds across One Tap, Two Tap, Three Tap, and Tap & Hold.
   - Actions: ANC Switching, Play/Pause, Next/Previous Track, Volume +/-, Voice Assistant, Gaming Mode.

5. **Hearing Assist (Assisted Listening Engine)**:
   - Real-time phone-microphone audio routing to connected earbuds using native `AudioRecord` and `AudioTrack`.
   - Conservative safe starting gain (15%), soft-knee peak limiter to eliminate loud spikes, high-pass rumble reduction, and speech clarity boost.
   - Prominent Emergency Stop button and automatic stop on audio route change.
   - Zero audio recording or storage.

6. **Acoustic Maintenance Tools & Relaxation**:
   - **Clean Dust**: 350Hz–650Hz resonant sweep to clear dust from driver diaphragm.
   - **Drain Moisture**: 145Hz hydraulic ejection pulses to expel fluid from ear canal nozzle.
   - **Relaxation**: 100% offline synthesized calming sounds (Gentle Rain, Ocean Waves, Zen Bell Meditation, White Noise).

7. **Zero-Cloud Offline Privacy**:
   - `INTERNET` permission omitted from AndroidManifest.
   - All settings saved in local Android DataStore.

## Verification & Testing
Run unit tests:
```bash
gradle :app:testDebugUnitTest
```
Build APK:
```bash
gradle :app:assembleDebug
```
