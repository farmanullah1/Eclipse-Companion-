# Bluetooth Protocol Evidence — Ronin Eclipse (R-7065)

## 1. Battery Telemetry Protocol
- **Service UUID**: `0000180F-0000-1000-8000-00805F9B34FB` (Bluetooth SIG Battery Service)
- **Characteristic UUID**: `00002A19-0000-1000-8000-00805F9B34FB` (Battery Level)
- **Properties**: Read, Notify
- **Payload Format**: Single unsigned 8-bit integer (0–100%)
- **Handling**: Validated strictly within `0..100`. Values outside this range or null are mapped to `ReportedValue.Unavailable`. Never defaulted to 0%.

## 2. Device Information Protocol
- **Service UUID**: `0000180A-0000-1000-8000-00805F9B34FB` (Device Information Service)
- **Model Number Characteristic**: `00002A24-0000-1000-8000-00805F9B34FB` (UTF-8 string: "R-7065")
- **Firmware Revision Characteristic**: `00002A26-0000-1000-8000-00805F9B34FB` (UTF-8 string: "v1.4.2")

## 3. Listening Mode Control Protocol
- **Operation**: Write with Response / Notification
- **Frame Format**: `[Header: 0xAA, Opcode: 0x03, Length: 0x01, ModeByte, Checksum: XOR]`
- **Mode Encodings**:
  - `0x00` -> Off / Normal
  - `0x01` -> Noise Reduction (ANC)
  - `0x02` -> Ambient Transparency
- **Acknowledgment**: Verified via state machine. Pending feedback is shown during in-flight command; state updates only upon device acknowledgement.

## 4. Equalizer Framing Protocol
- **10 Independent Frequency Bands**: 32Hz, 63Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz
- **Dynamic Gain Range**: -12.0 dB to +12.0 dB
- **Encoding**: 10 signed bytes representing dB offsets relative to flat reference line.

## 5. Touch Gesture Customization Framing
- **Supported Gestures**: One Tap, Two Tap, Three Tap, Tap & Hold
- **Supported Actions**: No Action, ANC Switching, Play/Pause, Next Track, Previous Track, Volume +, Volume -, Voice Assistant, Gaming Mode
- **Persistence**: Saved locally in encrypted DataStore preferences draft before atomic application to earbuds.
