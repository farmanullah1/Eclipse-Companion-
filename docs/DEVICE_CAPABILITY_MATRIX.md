# Device Capability Matrix — Ronin Eclipse (R-7065)

| Capability | Category | Evidence & Protocol | Status | Implementation Notes |
| :--- | :--- | :--- | :--- | :--- |
| **Discover Eclipse** | Hardware Discovery | Advertisement & scan filters | **Verified** | Discovered via BLE scanner with name filters `Eclipse` and `R-7065`. Paired devices enumerated first. |
| **GATT Connection** | Hardware Transport | Android `BluetoothGatt` (TRANSPORT_LE) | **Verified** | Connects directly to hardware peripheral with serialized operation queue (`GattOperationQueue`). |
| **GATT Service Discovery** | Hardware Inspection | Comprehensive service enumeration | **Verified** | Full enumeration of services, characteristics, properties (`[READ]`, `[WRITE]`, `[NOTIFY]`), and values. |
| **Left Earbud Battery** | Hardware Telemetry | Bluetooth SIG Battery Service (`0x180F`, `0x2A19`) | **Verified** | Validated in range `0..100`. Missing telemetry maps strictly to `Unavailable`; never defaults to 0%. |
| **Right Earbud Battery** | Hardware Telemetry | Bluetooth SIG Battery Service (`0x180F`, `0x2A19`) | **Verified** | Validated in range `0..100`. Missing telemetry maps strictly to `Unavailable`; never defaults to 0%. |
| **Charging Case Battery** | Hardware Telemetry | Dock telemetry broadcast | **Conditional** | Broadcasts only when earbuds are seated with dock lid open. Truthfully marked "Unavailable" otherwise. |
| **ANC (Noise Reduction)** | Hardware Control | JieLi RCSP (`0xFE, 0xDC, 0xBA`) & Vendor framing | **Verified** | Real command serialized to writable characteristic. UI updates only upon GATT write/telemetry confirmation. |
| **Ambient Transparency** | Hardware Control | JieLi RCSP & Vendor framing | **Verified** | Real command serialized to writable characteristic. UI updates only upon GATT write/telemetry confirmation. |
| **Normal / Off Mode** | Hardware Control | JieLi RCSP & Vendor framing | **Verified** | Real command serialized to writable characteristic. UI updates only upon GATT write/telemetry confirmation. |
| **10-Band EQ Controls** | Local + Hardware | ISO 10-band parameters + DataStore | **Verified** | Independent gain sliders (`31Hz–16kHz`, `[-12dB, +12dB]`). Stored in DataStore; sent to earbuds if writable. |
| **Touch Gesture Mapping** | Local + Hardware | L/R touch configuration table + DataStore | **Verified** | Configurable actions per gesture (1-tap, 2-tap, 3-tap, hold). Saved in DataStore and applied. |
| **Gaming Mode** | Hardware Control | Low-latency audio codec profile | **Conditional** | Available when writable control characteristic is active; disabled with explanatory message otherwise. |
| **Device Display Alias** | Local Configuration | App alias stored in DataStore | **Verified** | Updates display label in UI; keeps original Bluetooth hardware name safe. |
| **Hearing Assist (Phone Mic)**| Local Phone DSP | Android `AudioRecord` -> DSP -> `AudioTrack` | **Verified** | 100% offline, 120Hz high-pass filter, speech clarity formant boost, soft limiter, safe 15% starting gain. |
| **Relaxation Soundscapes** | Local Phone Audio | Synthesized soothing audio generator | **Verified** | 100% offline synthesizer (Rain, Ocean, Zen Bell, White Noise). Zero network permissions. |
| **Acoustic Dust Cleaning** | Local Phone Audio | 350Hz–650Hz resonant excursion sweep | **Verified** | 15s acoustic sweep designed to dislodge particles from earbud acoustic mesh. |
| **Moisture Drainage Pulse** | Local Phone Audio | 145Hz hydraulic displacement pulses | **Verified** | 15s rhythmic pressure pulses to clear liquid droplets from earbud nozzles. |
| **Firmware Revision Read** | Hardware Telemetry | Device Information Service (`0x180A`, `0x2A26`)| **Verified** | Reads UTF-8 firmware string directly from hardware characteristic. |
| **OTA Firmware Update** | Hardware Maintenance| Vendor binary signing & flash tool | **Blocked** | Disabled: Requires vendor private signing key; omitted to protect hardware integrity. |
| **Hardware Factory Reset** | Physical Interaction| Case hardware button hold | **Unsupported (In-App)**| Requires physical placement in charging case with both touch stems pressed for 10s. |
