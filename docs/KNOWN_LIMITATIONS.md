# Known Limitations and Hardware Truths

1. **Charging Case Telemetry:**
   - The charging case does not maintain an independent permanent BLE radio. Telemetry for case battery is only broadcast when at least one earbud is seated in the charging dock with the case lid open.

2. **OTA Firmware Updating:**
   - In keeping with the offline-first and safety-first philosophy, over-the-air firmware reflashing is intentionally blocked offline to prevent bricking the user's earbuds without verified vendor binary signatures.

3. **System-Wide EQ vs In-App Equalizer:**
   - The 10-band equalizer commands are sent directly to the earbud's internal DSP. If the device firmware does not accept proprietary hardware commands from an unauthenticated session, the EQ parameters are preserved in local DataStore for companion audio playback.

4. **Hearing Assist Medical Disclaimer:**
   - Hearing Assist is an assistive listening tool using phone microphone amplification, NOT a regulated medical hearing aid or substitute for professional audiologist evaluation.
