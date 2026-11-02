# Eclipse Companion — Earbud Connection & Emulator Testing Guide

Plan for enabling interactive earbud feature testing in the cloud/browser emulator environment via a designated **Emulator Device Test Mode**, paired with complete guidance for real hardware pairing on physical Android devices.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The browser emulator environment does not have access to a physical Bluetooth Low Energy controller or nearby hardware earbuds. To make every feature interactive and testable immediately in your browser tab, we will provide an explicit, toggleable **"Emulator Test Mode"** with a clear indicator banner, while keeping real Bluetooth LE hardware scanning active for real devices.

- **Confirmed Environment**: User is using the browser emulator or a virtual device without physical Ronin Eclipse hardware nearby.
- **Interactive Solution**:
  - Add a one-tap **"Connect Virtual Ronin Eclipse"** option on the Devices / Scan screen when no physical Bluetooth radio is detected or when running in the emulator.
  - Display a prominent **"Emulator Test Mode"** indicator in the UI to maintain strict separation between hardware BLE telemetry and virtual test mode.
  - Allow the user to test real-time battery status changes (Left: 85%, Right: 80%, Case: 90%), ANC listening modes (Noise Reduction, Off, Transparency), touch gesture mapping writes, EQ profile transfers, and diagnostic GATT logs.
  - Provide complete, concise instructions for installing the APK on a physical Android phone to pair real hardware.

---

## 1. Overview & Core Concept

- **What It Does**: Provides an immediate, functional way to test all earbud controls (battery indicators, listening modes, touch customization, diagnostic logs, and 10-band EQ) in the browser streaming emulator without physical hardware, while preserving full Android Bluetooth Low Energy scanning and pairing for physical devices.
- **Target Audience**: Developers, QA testers, and reviewers running the app in cloud or virtual environments without physical Ronin Eclipse earbuds attached.
- **Key Value**: Enables 100% end-to-end verification of the UI, state transitions, audio DSP tools, and device workflows directly from the browser window.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Testing in the Browser Streaming Emulator**:
   - Navigate to the **Scan / Devices** screen (`DevicesScanScreen`).
   - An "Emulator Test Mode" card appears with a button: **"Connect Virtual Ronin Eclipse"**.
   - Tapping it establishes a virtual connection:
     - Home screen immediately shows connected status: `Ronin Eclipse (Virtual Demo)`.
     - Battery indicators light up: Left 85%, Right 80%, Case 90%.
     - Listening mode selector enables live toggling between **Noise Reduction**, **Off**, and **Transparency** with instant feedback and toast notifications.
     - Touch Settings screen enables modifying gestures and applying them to the virtual device.
     - Diagnostics screen logs simulated GATT command packets (`0x55 0xAA ...`) matching the real protocol.
     - A subtle amber badge `[TEST MODE]` appears in the top bar to clearly distinguish simulated telemetry from physical hardware.

2. **Connecting Physical Earbuds on a Real Android Device**:
   - Download the APK generated via the project's settings menu or run on a connected phone.
   - Turn on Bluetooth on your Android phone and grant the "Nearby Devices" permission when prompted.
   - Place Ronin Eclipse earbuds in pairing mode (open the charging case or hold the case pairing button until the LED pulses white/blue).
   - In Eclipse Companion, tap **"Scan for Devices"**.
   - Select **"Ronin Eclipse"** from the list of discovered devices to pair.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Explicit "Emulator Test Mode" vs Automatic Fallback**:
  - *Chosen Approach*: Show a designated, prominent button on the scan screen and display an active badge when virtual mode is engaged.
  - *Why*: Strictly complies with Android developer policy and project guidelines requiring a prominent indicator when simulated data is used, preventing confusion with physical Bluetooth connections.
- **Decision 2: Preservation of Real Bluetooth Stack**:
  - *Chosen Approach*: Real `AndroidBluetoothRepository` continues to use genuine `BluetoothLeScanner` and `BluetoothGatt` APIs. The virtual mode interacts through the same `BluetoothRepository` interface without tampering with hardware code paths.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                          DevicesScanScreen                             │
│  ┌─────────────────────────┐  ┌─────────────────────────────────────┐  │
│  │ Real BLE Hardware Scan  │  │ Emulator Test Mode Card             │  │
│  │ (Physical Earbuds)      │  │ "Connect Virtual Ronin Eclipse"     │  │
│  └───────────┬─────────────┘  └──────────────────┬──────────────────┘  │
└──────────────┼───────────────────────────────────┼─────────────────────┘
               │                                   │
┌──────────────▼───────────────────────────────────▼─────────────────────┐
│                            MainViewModel                               │
│  - connectDevice(deviceId)                                             │
│  - connectVirtualDemoDevice()                                          │
│  - isVirtualDemoActive: StateFlow<Boolean>                             │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
┌────────────────────────────────────▼───────────────────────────────────┐
│                          BluetoothRepository                           │
│  - connectionState (Connected / Disconnected / Connecting)             │
│  - batteryState (Left: 85%, Right: 80%, Case: 90%)                     │
│  - currentListeningMode (NOISE_REDUCTION, OFF, TRANSPARENCY)           │
│  - diagnosticsLogs (Simulated or Real GATT packets)                    │
└────────────────────────────────────────────────────────────────────────┘
```

### Implementation Tasks

1. **`MainViewModel.kt`**:
   - Add `isVirtualDemoActive` StateFlow.
   - Implement `connectVirtualDemoDevice()` to populate connected state, battery readings, default listening mode, and sample diagnostic logs.
   - Add `disconnectVirtualDemoDevice()`.
2. **`DevicesScanScreen.kt`**:
   - Add an **"Emulator Testing / Virtual Earbuds"** card with clear explanation and a prominent **"Connect Virtual Ronin Eclipse"** button.
3. **`HomeScreen.kt` & Top Bar**:
   - Add a subtle `[TEST MODE]` chip next to the device name when connected in virtual mode.
4. **Verification**:
   - Run `compile_applet` and `testDebugUnitTest` to ensure zero compilation or regression errors.
