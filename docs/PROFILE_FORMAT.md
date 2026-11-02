# Protocol profile format (schema 1)

- `transport.type`: `gatt` (BLE) or `spp` (Bluetooth Classic serial). `gatt` needs `service`, `write`, and usually `notify` UUIDs.
- `framing`: frame = `header` + optional length byte (= body size) + body + optional checksum. `checksum` is `none`, `sum8`, `xor8` or `crc16modbus` (low byte first), computed from frame byte `checksumFrom` to the end of the body.
- `commands.*.request` is a template of hex bytes and tokens. `{name}` inserts one byte (0-255). `{name*}` inserts ASCII text.
  Tokens used by the app: setMode `{mode}`; setEq `{b0}`..`{b9}` (value = round(dB / stepDb) + offset); setTouch `{side}` `{gesture}` `{action}`;
  setGaming `{on}`; setTransparencyLevel `{level}`; setAncLevel `{level}`; setWearDetection `{on}`; setVoicePrompts `{on}`; setName `{name*}`.
- `commands.*.ack` is an optional hex prefix of the earbuds' reply body. With an ack the app waits up to 3 s and shows "Confirmed" only on a match.
- `commands.*.verified` must be `true` for the app to ever send it. Set it only after reproducing the command twice and seeing the earbuds react.
- `parse.battery`: `{ "prefix": "hex", "left": index, "right": index, "case": index, "invalid": [255] }`. `parse.mode`: `{ "prefix": "hex", "index": n, "map": { "ANC": v, "TRANSPARENCY": v, "NORMAL": v } }`. `parse.firmware`: `{ "prefix": "hex", "index": n }`.
- `eq`: `{ "bands": 10, "offset": 0, "stepDb": 1.0 }`. `touch`: `{ "sides": {"LEFT": v, "RIGHT": v}, "gestures": {"TAP": v, "DOUBLE": v, "TRIPLE": v, "LONG": v}, "actions": {"Play/Pause": v, ...} }`. `transparencyLevel`: `{ "min": 0, "max": 10 }`. `ancLevel`: `{ "min": 0, "max": 3 }` (use the real range you captured; both ranges must satisfy 0 ≤ min ≤ max ≤ 255). The app shows a level slider only when the matching command is verified **and** the range is defined.

Everything above is a structure description only. No value is known for the Eclipse R-7065 until captured.
