# Security and Privacy Review

## 1. Zero-Network Policy
- The `android.permission.INTERNET` permission is completely omitted from `AndroidManifest.xml`.
- No remote telemetry, crash reporting, ads, third-party analytics, or cloud sync SDKs are included.
- The build verified that no outgoing network connections can be opened by the app at runtime.

## 2. Audio Privacy & Hearing Assist
- Microphone access (`RECORD_AUDIO`) is requested solely when the user explicitly taps "Start Hearing Assist".
- Audio frames captured by `AudioRecord` are stored strictly in a temporary volatile ring buffer in RAM.
- Audio is NEVER written to internal storage, external files, cache directories, or logcat.
- When Hearing Assist stops, all audio buffers are immediately overwritten and destroyed.

## 3. Acoustic Safety Guardrails
- **Safe Starting Gain**: Defaults strictly to conservative output gain of 0.15 (15%), preventing acoustic trauma.
- **Hard Peak Limiter**: Soft-knee hyperbolic limiter compresses loud transient noises (e.g., dropped objects, clatter) to protect the user's and user's mother's ears.
- **Feedback Warning**: Screen prominently warns users about acoustic feedback loops when the phone microphone is placed too close to the earbud speakers.
- **Medical Disclaimer**: Distinctly labeled as an assistive listening tool, not a certified clinical hearing aid.

## 4. Threat Modeling & Device Protection
- **Command Allowlist**: Only validated, bounds-checked commands for standard BLE services and verified mode frames are permitted.
- **Firmware Protection**: OTA firmware flashing is locked in offline mode to prevent corrupting earbud flash memory without verified vendor cryptographic signatures.
- **Redacted Diagnostics**: Device MAC addresses and hardware identifiers are masked when copying diagnostic summaries.
- **Maintenance tones (Clean Dust, Drain Moisture)**: only play through a Bluetooth earbud route, never the phone speaker; fixed duration (30 s / 60 s), amplitude caps 0.5 and 0.25, 3 s fade-in, user must confirm earbuds are out of the ears, stop on call, focus loss, route loss, screen exit and app stop. No microphone is used.

