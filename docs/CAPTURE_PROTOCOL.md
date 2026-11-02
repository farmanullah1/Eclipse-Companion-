# Protocol discovery guide

Legitimate interoperability work on the user's **own** earbuds, using their **own** official app. Do not bypass encryption or authentication.

1. On the phone: Settings → Developer options → **Enable Bluetooth HCI snoop log** (full). Toggle Bluetooth off/on.
2. Open the **official app**, connect the Eclipse, and perform **one action at a time**, with a pause of ~5 s: ANC on → Transparency → Normal → each EQ preset → each touch change → gaming on/off, etc. Write the order down with timestamps.
3. Generate a bug report (Developer options → Take bug report) and extract `btsnoop_hci.log` from the zip (or use `adb pull /sdcard/btsnoop_hci.log` where available).
4. Open it in **Wireshark**. Filter `btatt` (BLE) or `btrfcomm` (Classic/SPP). Find **ATT Write Request / Write Command** (or RFCOMM data) packets that line up with each action. Note the service/characteristic handles, UUIDs (visible in earlier Service Discovery packets), and the byte payloads.
5. Compare payloads between actions to find: header bytes, length byte (yes/no), command id, parameter bytes, checksum type (none / sum8 / xor8 / crc16-modbus). Notifications from the earbuds (ATT Handle Value Notification) show acknowledgements and battery reports.
6. Use the app's **GATT Explorer** (read-only) to confirm UUIDs and properties. Fill the JSON profile, set `"verified": true` **only** for commands whose bytes you reproduced twice and whose result you observed on the earbuds.
7. Import the profile in Settings. Test each feature while watching the earbuds. Record results in `docs/PHYSICAL_DEVICE_TEST_RESULTS.md`.

Many Yscoco-style TWS earbuds use a BLE service for control, some use Bluetooth Classic SPP; the engine supports both (`transport.type` = `gatt` or `spp`). Do not assume which.
