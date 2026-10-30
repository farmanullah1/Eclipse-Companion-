# Privacy Policy — Eclipse Companion

**Last Updated:** October 2026  
**Product:** Eclipse Companion for Ronin Eclipse Earbuds (Model R-7065)

## 1. Zero Cloud Data Collection
Eclipse Companion operates strictly as an offline, device-local application:
- **No Internet Connectivity**: The application does not request the Android `INTERNET` permission and has no technical capability to send or receive data over the internet.
- **No User Accounts**: There is no sign-in, login, registration, email collection, or account system.
- **No Analytics or Telemetry**: No tracking identifiers, usage analytics, crash logs, or telemetry are transmitted to any server.

## 2. On-Device Data Storage
All application data is stored exclusively on the user's local smartphone storage using encrypted Android Jetpack DataStore:
- Locally created 10-band equalizer profiles.
- Touch gesture mappings for Left and Right earbuds.
- User theme preference (Dark / Light / System).
- Last connected Bluetooth device alias and MAC address.
- Low battery alert warning threshold.
Users can completely erase all stored preferences at any time via **Settings > Clear Local App Preferences**.

## 3. Microphone & Audio Privacy (Hearing Assist)
- The Android `RECORD_AUDIO` permission is requested **only** when the user explicitly taps "Start Hearing Assist".
- Microphone audio is processed in real-time in a temporary RAM ring buffer for immediate playback through connected Bluetooth earbuds.
- **Zero audio is ever recorded, saved to storage, transcribed, or uploaded.**
- When the session is stopped, all audio memory buffers are immediately zeroed and released.

## 4. Bluetooth & Nearby Device Permissions
- `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT` permissions are used exclusively to discover nearby Ronin Eclipse earbuds and exchange GATT settings.
- The app uses `neverForLocation` flags on Android 12+ so that user location is never accessed.
