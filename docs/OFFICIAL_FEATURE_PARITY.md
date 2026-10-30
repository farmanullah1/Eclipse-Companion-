# Official RONiN STUDIO App Feature Parity Matrix

| Feature | Official App Listing | Eclipse (R-7065) Evidence | Implementation Type | Test Result | Final Status |
|---|---|---|---|---|---|
| Left/Right Battery Status | Yes | Battery Service (0x180F) | Device Protocol | Passed | Verified |
| Charging Case Battery | Conditional | Only in dock with lid open | Conditional Telemetry | Passed | Conditional |
| ANC / Off / Transparency | Yes | Product specs & protocol frame | Device Protocol | Passed | Verified |
| Transparency Intensity Slider | Not supported on Eclipse | Firmware exposes toggle only | N/A | Honest "Off" state | Not supported by firmware |
| Custom 10-Band EQ | Yes | 10 center frequencies verified | Local DSP & Device Protocol | Passed (Regression test) | Verified |
| Mood Presets | Yes (Default, Energetic, Calm, Chill, Focus, Sad) | Screenshots & official app | Built-in Presets | Passed | Verified |
| Artist Presets | Yes (Hasan Raheem, Annural Khalid, etc.) | Official app Pakistani artists | Built-in Presets | Passed | Verified |
| Touch Customization | Yes (1-tap, 2-tap, 3-tap, hold) | Screenshots & mapping table | Device Protocol & DataStore | Passed | Verified |
| Gaming Mode (Low Latency) | Yes | Bluetooth low-latency toggle | Device Protocol | Passed | Verified |
| Device Rename | Yes | Local alias & BLE name | DataStore & Protocol | Passed | Verified |
| Meditation / Relaxation | Yes | Bundled local synthesizer | Local AudioTrack Engine | Passed | Verified (100% Offline) |
| Clean Dust | Yes (Sound Tools) | High-energy 350–650Hz sweep | Local AudioTrack Engine | Passed | Verified |
| Drain Moisture | Yes (Sound Tools) | 145Hz hydraulic pulses | Local AudioTrack Engine | Passed | Verified |
| Hearing Assist | Yes | Real-time mic -> DSP -> Earbuds | Native AudioRecord/AudioTrack | Passed | Verified (Safe Defaults) |
| OTA Firmware Update | Yes | Blocked: requires vendor signing | Offline Protected | Disabled with reason | Blocked (Safety First) |
| Factory Reset | Yes (Manual) | Case button / touch pad hold | Hardware Manual Guide | Passed | Documented in Help |
