# Ronin Eclipse White — Offline Companion App
## Production Implementation Specification for Google AI Studio / Gemini

**Document type:** Master implementation prompt and engineering specification  
**Target hardware:** Ronin Eclipse (R-7065), White / Snow White  
**Target platform:** Native Android  
**Primary goal:** Build a polished, offline-first companion app specifically for the user's Ronin Eclipse earbuds, with reliable device control, accessibility-oriented listening tools, and transparent reporting of hardware capabilities.

---

# 0. Instructions to the coding agent — read first

You are acting as a senior Android engineer, Bluetooth Low Energy (BLE) protocol engineer, audio DSP engineer, accessibility UX designer, security engineer, and QA lead.

Build a real Android project, not a mockup, prototype, or collection of disconnected screens. Read this entire specification before creating files. Then implement the app, run the available build and tests, fix errors, and document anything that genuinely cannot be implemented without additional device-specific protocol evidence.

## Non-negotiable requirements

1. **Offline-first:** The app's core operation must not require an account, sign-in, server, analytics service, cloud database, or internet connection.
2. **No dummy functionality:** Never show a successful state, fake battery percentage, simulated connection, fabricated firmware version, fake device response, or pretend command result.
3. **Real hardware control only:** ANC, Transparency, Normal, EQ, touch controls, Gaming Mode, rename, meditation, and firmware functions must only be presented as controllable when the connected Eclipse firmware and verified protocol support them.
4. **No invented Bluetooth protocol:** Do not guess proprietary BLE UUIDs, command opcodes, checksums, packet layouts, device names, or acknowledgements. Discover, verify, document, and test the real protocol.
5. **Fail honestly:** If a feature cannot be verified, show “Not available” or “Protocol not verified” and explain what is missing. Do not replace it with a UI-only switch.
6. **No credentials:** No login, registration, account recovery, remote account, or cloud sync.
7. **Protect user data:** Keep settings locally on the phone. Do not collect, upload, or log audio. Hearing Assist must not record or transmit microphone input.
8. **Do not damage earbuds:** Firmware update, reset, and undocumented commands require verified vendor protocol and explicit confirmation. Never send experimental commands to the device.
9. **Do not claim medical benefits:** Hearing Assist is an assistive listening feature, not a hearing aid or a substitute for a hearing assessment.
10. **Buildable code:** Generate complete files with correct imports, dependencies, permissions, manifest entries, navigation, state management, tests, and build instructions. Do not leave TODOs for core functions, `...`, empty callbacks, placeholder services, or code that cannot compile.
11. **Separate verified from unverified:** Maintain a protocol capability matrix. Each feature must have a real implementation, an explicitly unsupported state, or a clearly identified evidence-gathering blocker.
12. **Do not falsely claim “all official app features implemented.”** Prove each feature against the official app listing, available official documentation, and actual hardware tests. A feature that depends on an unavailable proprietary service must be reported as blocked, not faked.

## Required implementation workflow

Follow these phases in order:

- Phase A — inspect official public information and identify the exact device.
- Phase B — create a capability/protocol evidence matrix.
- Phase C — implement the native Android shell, design system, persistence, permissions, and Bluetooth discovery.
- Phase D — implement only verified device protocol operations.
- Phase E — implement local audio features and safe Hearing Assist.
- Phase F — test, build, inspect logs, correct errors, and produce final documentation.

Do not stop after creating a plan. Implement as much as the evidence supports. If a critical proprietary protocol is unavailable, still deliver the complete buildable app shell and real discovery/diagnostics, but do not claim unsupported control features work.

---

# 1. Product definition

## 1.1 Product name

Use a neutral working name such as **Eclipse Companion**. It is an independent companion app concept for the user's own Ronin Eclipse earbuds, not an official Ronin product. Do not use trademarks in a way that implies endorsement, and do not copy proprietary app source code, private assets, or logos from the original APK.

## 1.2 Target device

- Product: Ronin Eclipse
- Model reference: R-7065
- Color: White / Snow White
- Advertised hardware capabilities in publicly available retailer information include ANC, transparency/ambient mode, quad-microphone call noise reduction, Bluetooth 5.3, and a charging case.
- Treat retailer specifications as secondary evidence, not as proof of the exact firmware protocol.
- The official RONiN STUDIO Play listing states that the app is intended for newer software-based Ronin earbuds and is not supported on some older models. Verify that this particular Eclipse hardware revision is compatible before enabling controls.

Reference links:
- Official Android app listing: https://play.google.com/store/apps/details?id=com.yscoco.ronin
- Ronin Eclipse product listing/specification reference: https://earbuds.pk/wireless-earbuds/ronin/
- Ronin software-based earbuds information: https://ronin.pk/collections/software-based-earbuds

Use these references for product context only. Do not scrape or redistribute copyrighted app code/assets.

## 1.3 User goals

The app must prioritize:

1. Connect to the user's actual white Ronin Eclipse earbuds.
2. Clearly show connection state and the actual supported features.
3. Provide reliable ANC, Transparency, and Normal mode control when verified.
4. Improve the usability of transparency listening without pretending to alter hardware capabilities the firmware does not expose.
5. Offer a carefully limited phone-microphone Hearing Assist mode for occasional use by the user's mother.
6. Show separate left/right battery percentages and charging-case battery only when the device reports them reliably.
7. Offer precise, independent EQ bands, presets, and locally saved profiles.
8. Configure touch gestures when the earbuds expose a verified configuration command.
9. Include official app features that the device actually supports: Gaming Mode, device rename, Meditation/Relaxation Mode, device information, manuals/help stored locally where legally available, firmware information/update only if safely supported, pairing/discovery, and reset only if verified.
10. Work without sign-up and without internet for normal operation.
11. Be accessible, readable, responsive, and usable by a non-technical family member.

---

# 2. Critical hardware/protocol truth

## 2.1 Bluetooth audio is not the same as Bluetooth device control

Android may connect to earbuds for audio through the Bluetooth system while a separate companion protocol controls settings. Standard Bluetooth audio profile support does not imply that an app can change ANC, Transparency, EQ, or touch gestures.

The application must distinguish:

- **System Bluetooth pairing/audio connection:** Managed by Android.
- **BLE discovery:** Scanning nearby advertisements, subject to Android version and permissions.
- **GATT connection:** A connection to a BLE service, if the earbuds expose one.
- **Vendor command protocol:** Proprietary service/characteristic UUIDs, opcodes, payloads, and responses.
- **Audio stream routing:** Android's audio system and communication-device routing.
- **Phone-microphone Hearing Assist:** A separate local audio-processing pipeline.

Do not assume that the device uses BLE GATT for every operation. If the official app communicates through a different supported Android transport, document and implement that transport only if it can be established legally and technically.

## 2.2 Protocol evidence requirements

Before writing any device-control implementation, establish the following from legitimate sources or controlled tests with the user's own hardware:

- Exact advertised device identity and model/revision.
- Whether BLE services are advertised while the earbuds are in the required state.
- Service UUIDs and characteristic UUIDs.
- Read, write, write-without-response, notify, and indicate properties.
- Whether a characteristic requires notifications before commands can be acknowledged.
- Actual command payloads and response payloads for each feature.
- Endianness, length, checksums, framing, sequence numbers, authentication/session requirements, and response timeouts if applicable.
- Whether commands are sent to a primary earbud and relayed to the other earbud.
- Whether the charging case is addressable, and under what conditions.
- Whether battery readings are actual values, coarse levels, or unavailable.
- Whether settings survive disconnection or power cycling.
- Whether firmware update is available and what signed package format and recovery method are required.

Use the following legitimate evidence sources in priority order:

1. Vendor-published protocol documentation or SDK.
2. Vendor-provided sample code and official manuals.
3. Observable service metadata and read-only characteristics from the user's own hardware.
4. Controlled tests using the official companion app and the user's own device, subject to applicable law and platform terms.
5. Explicitly documented compatibility reports that can be reproduced.

Do not bypass encryption, authentication, secure boot, signature checks, or access controls. Do not extract or reuse proprietary app assets or secrets. Do not issue guessed writes to unknown characteristics.

## 2.3 Capability matrix — mandatory file

Create `docs/DEVICE_CAPABILITY_MATRIX.md` with this table. Populate the status from evidence, not assumptions.

| Capability | Evidence needed | Status values |
|---|---|---|
| Discover Eclipse | Advertisement/model evidence | Verified / Blocked / Unsupported |
| Connect to control service | GATT or other protocol evidence | Verified / Blocked / Unsupported |
| Left battery | Valid response and repeatability | Verified / Blocked / Unsupported |
| Right battery | Valid response and repeatability | Verified / Blocked / Unsupported |
| Case battery | Valid case report | Verified / Blocked / Unsupported |
| ANC mode | Verified command and readback | Verified / Blocked / Unsupported |
| Transparency mode | Verified command and readback | Verified / Blocked / Unsupported |
| Normal mode | Verified command and readback | Verified / Blocked / Unsupported |
| EQ/presets | Verified command and persistence | Verified / Blocked / Unsupported |
| Touch mapping | Verified mapping command/readback | Verified / Blocked / Unsupported |
| Gaming/low-latency mode | Verified command and observable result | Verified / Blocked / Unsupported |
| Device rename | Confirm actual storage target and protocol | Verified / Blocked / Unsupported |
| Meditation audio | Establish whether played by app or earbuds | Verified / Blocked / Unsupported |
| Firmware version | Valid device response | Verified / Blocked / Unsupported |
| OTA firmware update | Official package, signature, transport, recovery | Verified / Blocked / Unsupported |
| Factory reset | Verified command and recovery procedure | Verified / Blocked / Unsupported |
| Earbud fit/wearing detection | Actual sensor/protocol evidence | Verified / Blocked / Unsupported |

The app UI must derive availability from this capability matrix/runtime discovery. Do not render unsupported controls as active.

---

# 3. Offline and privacy architecture

## 3.1 Offline guarantee

The app must not need an internet connection to:

- Open and navigate.
- Discover and connect to a compatible device.
- Read supported battery/mode/device status.
- Change verified supported settings.
- Use locally saved EQ presets and touch mappings.
- Run phone-microphone Hearing Assist after microphone permission.
- Play locally bundled relaxation sounds, if licensed and included.
- Store preferences, recent device identity, and diagnostic summaries.
- Read bundled help and troubleshooting pages.

No remote backend, account, telemetry, analytics, ads, cloud API, online fonts, remote images, or remote feature flags.

Firmware update is a special case: it must only be offered if the vendor's official update package and safe update protocol are available locally. The app must not silently download firmware. If the user imports a package, verify signature/integrity using the vendor's documented verification method before proceeding.

## 3.2 Android project approach

Use **native Android with Kotlin and Jetpack Compose**, because Bluetooth permissions, GATT lifecycle, Android audio routing, foreground microphone service behavior, and platform accessibility are central requirements.

Suggested baseline:
- Kotlin.
- Jetpack Compose + Material 3.
- Gradle Kotlin DSL.
- Minimum Android version chosen after checking needed APIs; target a currently supported Android SDK.
- Android Bluetooth APIs for discovery and GATT.
- Android `AudioRecord` for microphone input and `AudioTrack` for low-latency playback if appropriate.
- Android DataStore for non-sensitive local preferences.
- Room only if structured local device/profile history is genuinely needed.
- Kotlin coroutines and `StateFlow` for lifecycle-aware asynchronous state.
- AndroidX lifecycle and navigation.
- Unit tests and instrumented tests.
- No backend or Node.js server.

Do not create a React web page and claim it is a native Android Bluetooth app. A WebView is not an acceptable substitute for native BLE and audio routing.

## 3.3 Network policy

- Do not request `INTERNET` permission unless a clearly justified, user-requested, non-core feature needs it. The default implementation must omit it.
- No analytics dependencies.
- No crash-reporting upload SDK.
- No remote support/WhatsApp requirement. Show a local help page with the support contact only if included as plain text and clearly marked as requiring the user's external app/network to contact support.
- Avoid unnecessary package dependencies.
- Add a build check or source audit that flags the `INTERNET` permission and unexpected network libraries.

## 3.4 Local data

Persist only:
- User-selected app theme and accessibility preferences.
- Locally created EQ profiles.
- Locally created touch mapping profiles, if supported by hardware.
- Last selected compatible device identifier where Android permits it.
- Last-known battery/mode values with timestamp, clearly marked **Last reported**, never presented as live.
- User's chosen Hearing Assist settings, excluding microphone recordings.
- Locally stored troubleshooting notes if needed.

Do not store raw microphone audio. Do not store a continuous audio buffer to disk. Keep only the minimum in-memory ring buffer needed for processing.

Provide a “Clear local app data” action with a confirmation dialog. It must not claim to erase the earbuds unless a separate verified hardware reset is executed.

---

# 4. Feature inventory

## 4.1 Connection and device management

Implement:
- Intro screen explaining local Bluetooth operation and the need for nearby-device permissions.
- “Add device” scan screen with a scan indicator, cancel action, and timeout.
- Filter to identify the Eclipse by verified advertisement/model characteristics rather than name alone.
- Nearby-device list with device name, signal strength only if available, and compatibility confidence.
- Connect/disconnect controls.
- Clear states: Bluetooth off, permission denied, scan unavailable, no compatible device found, connecting, connected, disconnected, connection lost, unsupported model, protocol unavailable, and device busy.
- Reconnect only while the app is active unless the user explicitly enables a documented Android-compatible reconnect option. Do not run an invisible permanent service.
- Multiple saved compatible devices only if real device management can be implemented.
- Remove saved device from the app.
- Rename earbud only if the verified protocol supports renaming the device. Distinguish the local app alias from a name written to earbud firmware.
- Device information page with model, firmware version, protocol capability status, and last successful communication time.

Never infer a device is connected merely because it appears in the system Bluetooth paired list. Show app-control connection and audio connection separately when relevant.

## 4.2 Battery status

Main dashboard battery cards:
- Left earbud: percentage or “Not available”.
- Right earbud: percentage or “Not available”.
- Charging case: percentage or “Not available”.
- Charging/charging-case state only if explicitly reported.
- Last update timestamp.
- Refresh action where supported.

Rules:
- Never derive the case percentage from the earbuds.
- Never assume the two earbuds have the same percentage.
- Never invent percentage from RSSI, voltage, or a generic Android battery service unless the documented mapping is valid.
- If only coarse battery levels are reported, display the true resolution (e.g., “Approximately 60%” only if the device protocol defines that level).
- If stale, label the data “Last reported at 10:42” rather than live.
- If the device reports invalid data, reject it and show “Not available”.
- Battery thresholds may trigger local warnings (default 20% and 10%), configurable by the user.
- Do not claim the case battery is available unless the case exposes a verified report and the relevant lid/connection conditions are met.

## 4.3 Listening modes

Main mode selector:
- ANC.
- Transparency / Ambient.
- Normal / Off.

UX:
- Large segmented selector or three large cards.
- Clearly highlight the confirmed active mode.
- Show a pending state while a command is in flight.
- On failure, restore the last confirmed state and explain that the earbuds did not confirm the change.
- Disable mode controls when disconnected or unsupported.
- Do not show the newly selected mode as active until confirmed by device response or a documented reliable acknowledgement.
- If the protocol can write but cannot read mode back, state that clearly and distinguish “command sent” from “mode confirmed”.
- Prevent conflicting simultaneous mode commands.
- Add a short transition animation only after confirmation; animation must not be treated as hardware state.

## 4.4 Transparency improvement

First distinguish **earbud Transparency/Ambient mode** from **phone-microphone Hearing Assist**.

If firmware supports transparency level adjustment:
- Offer a level slider with the true hardware range and step count.
- Read back the setting if supported.
- Label the setting as earbud transparency gain/level, not an app-side DSP value.
- Provide a reset-to-device-default action.

If firmware exposes only an on/off mode:
- Do not show a fake transparency intensity slider.
- Offer “Transparency tips” and the separate phone-microphone Hearing Assist feature instead.

Optional local audio processing for Hearing Assist:
- Use only documented, controllable phone-side DSP such as a conservative high-pass filter, mild compression/limiting, and a small number of safe tone controls.
- Do not promise improved hardware transparency from phone-side processing when the phone mic is not the signal path.
- Do not apply app processing to the earbud's own ambient microphones unless the vendor protocol explicitly supports it.

## 4.5 Hearing Assist — phone microphone

Purpose: let the user try short, supervised, assistive listening sessions through the connected earbuds, including for their mother. This is not a medical hearing aid.

Required behavior:
- Dedicated screen with a clear explanation: “Uses this phone's microphone and routes processed sound to your connected earbuds.”
- Ask for microphone permission only when the user taps Start.
- Never begin microphone capture automatically at app launch.
- Require connected earbuds and a suitable output route. If the output route is not the intended earbuds, explain the issue and do not start silently.
- Start/Stop control always visible during the session.
- Show active microphone state and current output route.
- Local processing only; no recording, upload, transcription, or speech recognition.
- Use a bounded, low-latency audio pipeline and lifecycle-aware foreground service if Android requires one for continued microphone use.
- Display Android's required microphone foreground-service notification while active.
- Stop immediately when the user presses Stop, revokes permission, the service is destroyed, or a fatal audio error occurs.
- Handle calls/audio-focus changes safely; pause or stop Hearing Assist during a call unless a safe supported behavior is explicitly implemented.
- Provide a clear emergency Stop action.
- Use a limiter to reduce sudden peaks. Start at very low output gain.
- Add a brief feedback warning: microphone-to-earbud loops can create acoustic feedback, especially when a microphone is close to a speaker. If high-pitched feedback occurs, stop immediately, reduce gain, reposition the phone, and do not keep increasing volume.
- Avoid automatic gain behavior that causes sudden loudness changes.
- Avoid aggressive noise suppression that makes speech unnatural.
- Provide a simple profile, not a complicated clinical fitting tool.
- Never call it “hearing aid”, “medical hearing correction”, “tinnitus treatment”, or claim it can replace professional hearing care.

Initial controls:
- Input gain: conservative, limited range.
- Output level: start low; use Android media volume and app-level attenuation.
- Speech clarity: off / mild / moderate, using documented local DSP.
- Low-frequency rumble reduction: off / mild.
- Soft limiter: on by default; allow disabling only with a clear warning if the design requires it.
- Left/right balance: optional if audio routing permits and the user understands it.
- Preset: “Conversation” as a starting profile, not a medical calibration.

Safety defaults:
- Start with minimal amplification.
- Limit sudden peaks.
- Never automatically restore a high previous gain after a new session.
- When resuming, require user confirmation and reset to the safe starting level.
- Do not run in the background after the user leaves the active session without a clear visible foreground indicator.
- Do not claim clinical benefit. Encourage professional assessment if the mother has persistent hearing difficulty.
- If one earbud is missing/disconnected, stop or ask the user before switching output.

Technical implementation:
- Investigate `AudioRecord`, `AudioTrack`, `AudioManager`, audio focus, communication-device routing, and Android's supported Bluetooth output paths for the target Android versions.
- Use a dedicated audio worker/coroutine; do not process every sample on the main thread.
- Select a supported sample rate after checking device capabilities; gracefully fall back.
- Use a bounded buffer and avoid unbounded allocations in the real-time loop.
- Measure and document end-to-end latency.
- Add clipping detection and tests for buffer underrun/overrun.
- If the platform cannot route the processed microphone stream to the Bluetooth earbuds with acceptable latency on a specific Android version/device, show an explanatory compatibility state rather than claiming it works.
- Do not request Bluetooth call/microphone profiles without a legitimate requirement. Explain that some Bluetooth routes use a lower-bandwidth hands-free profile and may change other audio behavior.

## 4.6 Equalizer

Build a robust EQ screen with a genuinely independent control per band.

Requirements:
- Custom profile with independently adjustable bands. Moving one band must never move neighboring bands.
- Store profile settings locally.
- Rename, duplicate, reset, delete, and reorder user profiles only where appropriate.
- Show the actual selected device EQ state if readback is supported.
- Separate “Saved locally” from “Applied to earbuds”.
- If the EQ only works while the app processes local audio and cannot affect system-wide music, explain that limitation prominently.
- Do not imply a phone-side EQ changes audio from Spotify, YouTube, or other apps unless a real Android audio effect path supports that use case.
- For hardware EQ, only send verified vendor protocol commands.

Suggested band design if the hardware accepts a parametric/equalizer vector with matching bands:
- Use the hardware's actual band count and center frequencies.
- If hardware band count is unknown, do not invent it as the device EQ. The local editor may have a separately labelled preview only if useful, but it must not claim to apply to the earbuds.
- Use independent sliders, clear frequency labels, dB values, and a zero/reset line.
- Provide accessible numeric adjustments and keyboard/screen-reader descriptions.

Suggested optional local profiles (only mark “applied” when genuinely applied):
- Balanced
- Bass
- Bass Light
- Vocal Clarity
- Podcast
- Acoustic
- Classical
- Jazz
- Rock
- Electronic
- Late Night
- Speech
- Custom

These are local preset definitions, not claims about official factory presets. If the earbuds support fewer presets or fixed vendor presets, represent that honestly.

Prevent clipping:
- Apply appropriate headroom for local DSP.
- Show a clipping warning if a combination of boosts is unsafe.
- Do not silently normalize or couple neighboring bands.
- Add regression tests proving that adjusting one band leaves every other band's stored value unchanged.

## 4.7 Touch controls

Provide a screen for left/right earbuds and each supported gesture:
- Single tap.
- Double tap.
- Triple tap.
- Long press.
- Other gestures only if verified by the device.

Potential actions to show only when hardware supports them:
- Play/pause.
- Previous track.
- Next track.
- Volume up/down.
- Answer/end call.
- Reject call.
- ANC/Transparency/Normal cycle.
- Voice assistant.
- Gaming mode.
- Disabled.

Rules:
- Populate allowed actions from verified capabilities.
- Prevent duplicate mappings only if the hardware disallows duplicates.
- Validate conflicting combinations before applying.
- Save a local draft and provide Apply/Cancel.
- Do not show “Saved to earbuds” until a successful hardware acknowledgement.
- Clearly distinguish “Saved on this phone” from “Applied to earbuds”.
- If mappings cannot be read back, disclose this limitation.
- Include a Restore device defaults action only if a verified reset-to-default command exists.

## 4.8 Gaming Mode

- Expose only if supported by verified hardware commands.
- Explain that low-latency mode may trade battery life or audio quality for latency if that is documented for this model.
- Display actual confirmed state.
- Do not invent a latency number or promise “lag-free”.
- If not supported by this Eclipse firmware, show “Not supported by detected device” and do not send commands.

## 4.9 Meditation / Relaxation Mode

The official app listing advertises Meditation Mode. Determine whether the official feature plays audio from the phone, controls a device tone generator, or does both.

Implement the phone-side version as bundled local audio only if licensing permits:
- Include short, original or properly licensed sound assets in `app/src/main/res/raw/`.
- No streaming or internet access.
- Play/pause, stop, timer, and volume.
- Respect audio focus and interruptions.
- No autoplay on launch.
- No copyrighted commercial music.
- If the official mode controls the earbuds' internal playback, implement that only with a verified command.
- Keep the phone-audio relaxation feature clearly distinct from an earbud hardware feature.

## 4.10 Rename, firmware, reset, manuals, support

**Rename**
- Distinguish local app alias from firmware/device name.
- Write to the earbuds only when a verified rename protocol exists.
- Handle unsupported characters and device name length according to evidence.

**Firmware**
- Display actual firmware version only when reported by the device.
- OTA update is disabled unless official firmware, signature validation, update protocol, and recovery procedure are available.
- Never manufacture a firmware package or update URL.
- Require user confirmation, sufficient battery as specified by the vendor, stable connection, and clear warning against interrupting the update.
- Show progress only when real update progress can be measured.
- Provide documented recovery steps and never claim an update completed without final device verification.

**Factory reset**
- Require a confirmation dialog that describes the effect.
- Only invoke a verified reset command.
- Do not confuse clearing local app settings with resetting earbud firmware.

**Manual/help**
- Bundle only user-provided or legally redistributable manuals. Otherwise provide a local troubleshooting guide written for this app and links that require internet only when the user chooses to open them.
- Do not require internet to read the in-app guide.

**Support**
- Local troubleshooting and a copyable support email may be included.
- Do not automatically open WhatsApp or any external service.
- Make clear that external support contact may need internet.

---

# 5. Design system

## 5.1 Visual direction

Create a premium, minimal, hardware-focused interface inspired by the official RONiN STUDIO app's product-oriented layout, but do not copy its proprietary artwork, exact screenshots, source code, or brand logo. Use original UI assets and a distinct app name.

Visual goals:
- White/pearl surfaces and charcoal typography.
- Subtle silver borders and restrained cool-gray accents.
- One controlled accent color for active controls.
- Dark mode with near-black/graphite surfaces.
- Strong contrast, clear hierarchy, generous spacing, tactile large controls.
- Smooth but restrained motion.
- Avoid neon gradients, generic AI-dashboard styling, excessive glassmorphism, decorative blobs, and overuse of cards.
- White Eclipse product presentation with a subtle shadow on a neutral surface. Use original vector illustration or user-provided product photos; do not fabricate an exact product photo and pass it off as authentic.

## 5.2 Color tokens

Define colors centrally, never scattered hard-coded values.

Light theme:
- `background`: `#F6F6F4`
- `surface`: `#FFFFFF`
- `surfaceSecondary`: `#EEEEEC`
- `textPrimary`: `#181818`
- `textSecondary`: `#666663`
- `outline`: `#D7D7D2`
- `accent`: `#344C63`
- `accentContainer`: `#E3EAF0`
- `success`: `#2D6A4F`
- `warning`: `#9A6700`
- `error`: `#B3261E`

Dark theme:
- `background`: `#121314`
- `surface`: `#1B1C1E`
- `surfaceSecondary`: `#25272A`
- `textPrimary`: `#F3F3F0`
- `textSecondary`: `#B7B8B5`
- `outline`: `#424447`
- `accent`: `#B8CBDC`
- `accentContainer`: `#283743`
- `success`: `#82C9A5`
- `warning`: `#F1C46A`
- `error`: `#F2A09A`

Validate contrast against WCAG AA for text and interactive controls. These are initial design tokens and can be tuned after contrast testing.

## 5.3 Typography and spacing

- Use platform system typography / Compose Material typography. Do not load remote fonts.
- Page title: 24–28sp, semibold.
- Section title: 18–20sp, semibold.
- Body: 14–16sp.
- Supporting label: at least 12sp; never use tiny low-contrast labels for critical states.
- Minimum interactive target: 48dp.
- Standard horizontal padding: 20dp on phone, adapt on large screens.
- Common gaps: 8, 12, 16, 20, 24, 32dp.
- Respect Android font scaling, display size, and edge-to-edge insets.
- Do not clip translated text or large fonts.

## 5.4 Navigation

Use a simple bottom navigation bar on compact phones:
1. **Home**
2. **Sound**
3. **Controls**
4. **Assist**
5. **Settings**

If five destinations are too crowded at a specific width, use a navigation rail or a compact adaptive layout. Do not hide important controls behind unlabeled icons.

Screens:
- Home dashboard.
- Add/Connect device.
- Device details.
- Sound modes.
- EQ.
- Touch controls.
- Gaming mode (conditional).
- Hearing Assist.
- Relaxation.
- Device settings.
- Firmware/device information.
- Permissions and privacy.
- Diagnostics.
- Local help/troubleshooting.

## 5.5 Home dashboard layout

Top:
- App name and connection indicator.
- Device name/alias.
- Settings shortcut.

Device panel:
- Original illustration of white Eclipse earbuds and case.
- Connected / disconnected state.
- Connect or Manage device button.

Battery:
- Three clearly separated readings: L, R, Case.
- Show percentages only when verified.
- Use “Not available” rather than a guessed number.
- Show a stale-data timestamp when relevant.

Quick controls:
- ANC / Transparency / Normal selector.
- Gaming toggle only if supported.
- Hearing Assist entry, never auto-started.

Recent/current status:
- Last confirmed mode.
- Last successful device communication.
- Warnings and permissions that require attention.

Avoid presenting an elaborate dashboard full of irrelevant metrics. Keep the most common tasks within one or two taps.

## 5.6 Interaction and animation

- Use short 120–220ms transitions for local UI changes.
- Respect system “Remove animations” / reduced motion preferences.
- Provide haptic feedback only for meaningful confirmed actions and only where appropriate.
- Never use animation to imply an unconfirmed hardware operation.
- Disable controls during an operation when duplicate commands could conflict.
- Provide visible loading, success, failure, and timeout states.
- Use accessible labels for every icon-only button.
- Confirmation dialogs for destructive or potentially disruptive actions.
- Ensure UI works in portrait and landscape, compact and large phones, tablets, split screen, and large font settings.

---

# 6. Icons, app icon, and assets

## 6.1 Icons

Use Material Symbols / Material Icons through the native Android icon library or original vector drawables. Do not depend on internet-loaded icons.

Create a consistent icon set for:
- Home
- Sound / equalizer
- ANC
- Ambient / transparency
- Normal mode
- Battery
- Bluetooth
- Connect / disconnect
- Left earbud
- Right earbud
- Case
- Gaming
- Touch gesture
- Microphone
- Hearing Assist
- Relaxation
- Settings
- Help
- Privacy
- Firmware
- Warning / error / success
- Refresh
- Stop
- Play / pause
- Reset

Every icon must have a text label or content description where necessary. Do not use emoji as the primary UI icon set.

## 6.2 App icon

Create original vector-based app icon artwork:
- Rounded-square adaptive icon.
- Minimal white earbud/case silhouette on a graphite background with a muted steel-blue accent.
- Distinct from the official Ronin logo.
- Foreground and background adaptive icon layers.
- Monochrome adaptive icon for themed Android icons.
- Launcher icon variants generated through Android resource tooling.
- No copied logo, no downloaded trademarked artwork, no text that becomes illegible at small sizes.

Required resources:
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/drawable/ic_launcher_foreground.xml`
- `app/src/main/res/values/ic_launcher_background.xml` or equivalent supported resource
- Monochrome layer where supported
- Native vector drawables for key UI icons

## 6.3 Favicon

A native Android app does not require a web favicon. Still, include an optional original `assets/favicon.svg` in the documentation/assets package for a future local help page or companion web documentation. Do not make a web app a dependency for Android operation.

## 6.4 Product illustration

Create an original vector illustration representing white wireless earbuds and a charging case, with no claim that it is an exact photographic depiction. If the user later supplies an authorized product photo, use it instead. Provide a vector placeholder that is a real design asset—not a fake device-status image—and label it as an illustration.

All assets must be local and included in the project. No remote asset URLs.

---

# 7. Android permissions and lifecycle

Implement permissions based on Android API level, not a single obsolete permission list.

Potential permissions, only where necessary:
- `BLUETOOTH_SCAN` on Android 12+ for scanning.
- `BLUETOOTH_CONNECT` on Android 12+ for connecting and communicating.
- Legacy Bluetooth permissions with correct `maxSdkVersion` where required for older Android versions.
- `ACCESS_FINE_LOCATION` only if required for Bluetooth scanning on the relevant older Android versions; explain why and do not request it on versions where not needed.
- `RECORD_AUDIO` only when the user starts Hearing Assist.
- `FOREGROUND_SERVICE` and the appropriate microphone foreground-service permission/type on Android versions that require them for an active microphone session.
- Notification permission only if the target Android version requires it for the relevant notification experience.
- Do not request contacts, phone state, storage, camera, SMS, call logs, or location for unrelated reasons.
- Do not request `INTERNET` by default.

Lifecycle rules:
- Cancel BLE scans on timeout, navigation away, permission loss, or lifecycle stop.
- Close GATT connections safely when appropriate.
- Unregister callbacks and release audio resources.
- Prevent duplicate scans and duplicate GATT connections.
- Use lifecycle-aware collectors for UI state.
- Handle Bluetooth being disabled or the user revoking permissions at runtime.
- Keep microphone capture only for an explicitly active Hearing Assist session with the required visible notification.
- Never use hidden background services to maintain a permanent connection.

---

# 8. Suggested project structure

Generate a complete project with a structure similar to:

```text
EclipseCompanion/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/eclipsecompanion/
│       │   │   ├── MainActivity.kt
│       │   │   ├── app/
│       │   │   │   ├── EclipseCompanionApp.kt
│       │   │   │   └── AppDestinations.kt
│       │   │   ├── core/
│       │   │   │   ├── Result.kt
│       │   │   │   ├── Clock.kt
│       │   │   │   └── AppError.kt
│       │   │   ├── bluetooth/
│       │   │   │   ├── BluetoothRepository.kt
│       │   │   │   ├── AndroidBluetoothRepository.kt
│       │   │   │   ├── ScanResult.kt
│       │   │   │   ├── GattSession.kt
│       │   │   │   ├── GattOperationQueue.kt
│       │   │   │   ├── ProtocolTransport.kt
│       │   │   │   ├── DeviceProtocol.kt
│       │   │   │   ├── ProtocolCapabilities.kt
│       │   │   │   ├── DeviceIdentity.kt
│       │   │   │   └── protocol/
│       │   │   │       ├── VerifiedEclipseProtocol.kt
│       │   │   │       └── ProtocolEvidence.kt
│       │   │   ├── domain/
│       │   │   │   ├── EarbudState.kt
│       │   │   │   ├── BatteryState.kt
│       │   │   │   ├── ListeningMode.kt
│       │   │   │   ├── EqProfile.kt
│       │   │   │   └── TouchMapping.kt
│       │   │   ├── data/
│       │   │   │   ├── SettingsRepository.kt
│       │   │   │   └── LocalSettingsRepository.kt
│       │   │   ├── audio/
│       │   │   │   ├── HearingAssistEngine.kt
│       │   │   │   ├── HearingAssistService.kt
│       │   │   │   ├── AudioRouteMonitor.kt
│       │   │   │   ├── AudioProcessor.kt
│       │   │   │   └── AudioSessionState.kt
│       │   │   ├── ui/
│       │   │   │   ├── theme/
│       │   │   │   │   ├── Color.kt
│       │   │   │   │   ├── Theme.kt
│       │   │   │   │   └── Type.kt
│       │   │   │   ├── components/
│       │   │   │   ├── home/
│       │   │   │   ├── connect/
│       │   │   │   ├── sound/
│       │   │   │   ├── controls/
│       │   │   │   ├── assist/
│       │   │   │   ├── settings/
│       │   │   │   └── help/
│       │   │   └── permissions/
│       │   │       └── PermissionManager.kt
│       │   └── res/
│       │       ├── drawable/
│       │       ├── mipmap-anydpi-v26/
│       │       ├── raw/
│       │       ├── values/
│       │       └── xml/
│       ├── test/
│       └── androidTest/
├── assets/
│   ├── favicon.svg
│   └── eclipse-earbuds-illustration.svg
├── docs/
│   ├── README.md
│   ├── DEVICE_CAPABILITY_MATRIX.md
│   ├── BLUETOOTH_PROTOCOL_EVIDENCE.md
│   ├── PRIVACY.md
│   ├── TEST_PLAN.md
│   ├── TROUBLESHOOTING.md
│   └── BUILD_AND_INSTALL.md
└── scripts/
    └── verify_offline_dependencies.sh
```

This is a target structure, not permission to create empty files. If a file is unnecessary, omit it and document the architectural reason. Every referenced file must exist and compile.

---

# 9. Core domain models

Use explicit unknown states. Do not use numeric defaults such as zero for missing battery values.

Example Kotlin domain models:

```kotlin
package com.example.eclipsecompanion.domain

import java.time.Instant

sealed interface ReportedValue<out T> {
    data class Available<T>(
        val value: T,
        val reportedAt: Instant
    ) : ReportedValue<T>

    data class Unavailable(
        val reason: String
    ) : ReportedValue<Nothing>
}

data class BatteryState(
    val left: ReportedValue<Int>,
    val right: ReportedValue<Int>,
    val case: ReportedValue<Int>
) {
    companion object {
        fun unavailable(reason: String) = BatteryState(
            left = ReportedValue.Unavailable(reason),
            right = ReportedValue.Unavailable(reason),
            case = ReportedValue.Unavailable(reason)
        )
    }
}

enum class ListeningMode {
    ANC,
    TRANSPARENCY,
    NORMAL
}

enum class ConnectionPhase {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    DISCOVERING_SERVICES,
    SUBSCRIBING,
    READY,
    RECONNECTING,
    ERROR
}

data class ProtocolCapabilities(
    val batteryLeft: Boolean = false,
    val batteryRight: Boolean = false,
    val batteryCase: Boolean = false,
    val listeningModes: Set<ListeningMode> = emptySet(),
    val transparencyLevel: Boolean = false,
    val hardwareEq: Boolean = false,
    val touchMapping: Boolean = false,
    val gamingMode: Boolean = false,
    val deviceRename: Boolean = false,
    val meditationControl: Boolean = false,
    val firmwareRead: Boolean = false,
    val firmwareUpdate: Boolean = false,
    val factoryReset: Boolean = false
)
```

Validation rules:
- Battery percentages must be in the range 0..100.
- Reject malformed or implausible protocol responses.
- Use timestamps from the actual report time.
- Never create `Available(0)` to represent unknown.
- Do not make capabilities `true` just because a screen exists.
- Capabilities must be derived from verified device/protocol evidence.

---

# 10. Bluetooth architecture and command discipline

## 10.1 Abstractions

Use interfaces so UI code never writes directly to Bluetooth characteristics:

```kotlin
package com.example.eclipsecompanion.bluetooth

import com.example.eclipsecompanion.domain.BatteryState
import com.example.eclipsecompanion.domain.ListeningMode
import com.example.eclipsecompanion.domain.ProtocolCapabilities
import kotlinx.coroutines.flow.StateFlow

interface BluetoothRepository {
    val connectionState: StateFlow<ConnectionState>
    val nearbyDevices: StateFlow<List<NearbyDevice>>

    suspend fun scan(timeoutMillis: Long = 12_000L)
    suspend fun stopScan()
    suspend fun connect(deviceId: String)
    suspend fun disconnect()
    suspend fun readCapabilities(): ProtocolCapabilities
    suspend fun readBattery(): BatteryState
    suspend fun readListeningMode(): ListeningMode?
    suspend fun setListeningMode(mode: ListeningMode): DeviceCommandResult
}

data class NearbyDevice(
    val id: String,
    val displayName: String,
    val rssi: Int?,
    val compatibility: Compatibility
)

enum class Compatibility {
    VERIFIED_ECLIPSE,
    POSSIBLE_MATCH,
    UNSUPPORTED,
    UNKNOWN
}

data class ConnectionState(
    val phase: com.example.eclipsecompanion.domain.ConnectionPhase,
    val deviceName: String? = null,
    val message: String? = null
)

sealed interface DeviceCommandResult {
    data class Confirmed(val description: String) : DeviceCommandResult
    data class SentButUnconfirmed(val description: String) : DeviceCommandResult
    data class Rejected(val reason: String) : DeviceCommandResult
    data class Unsupported(val reason: String) : DeviceCommandResult
}
```

The actual implementation must include imports, state transitions, cancellation, timeouts, resource cleanup, error mapping, and tests. This interface is a contract, not a fake repository. Do not add an implementation that always returns canned success.

## 10.2 GATT session

Implement a serialized GATT operation queue. Android GATT operations are asynchronous and should not be launched concurrently without controlling callback ordering.

Requirements:
- One active GATT session per target device unless multi-device support is fully tested.
- One in-flight operation at a time unless a verified protocol and Android implementation safely support more.
- Explicit timeout per operation.
- Handle `onConnectionStateChange`, `onServicesDiscovered`, `onCharacteristicRead`, `onCharacteristicWrite`, descriptor write, notifications, MTU changes, and disconnection.
- Subscribe to notifications before depending on notification responses.
- Verify characteristic properties before reads/writes.
- Handle `GATT_SUCCESS` separately from application-level command acknowledgement.
- A successful GATT write is not proof the earbud accepted or applied the command.
- Close stale GATT objects and release callbacks on disconnect.
- Avoid automatic retry loops that can flood the device.
- Backoff reconnect attempts and stop after a small bounded number.
- Surface actionable errors to the UI.

## 10.3 Verified protocol adapter

The `VerifiedEclipseProtocol` implementation must contain only confirmed values and documented behavior. Every UUID and opcode must have an evidence note in `docs/BLUETOOTH_PROTOCOL_EVIDENCE.md`.

Example documentation format:

```text
Feature: Listening mode
Evidence source: [describe official document or reproducible test]
Service UUID: [verified value only]
Characteristic UUID: [verified value only]
Operation: [read/write/notify]
Payload format: [byte-level format]
Valid request examples: [captured/tested payloads]
Expected response: [byte-level format]
Timeout: [measured/justified]
Readback behavior: [confirmed/unavailable]
Test result: [device firmware/revision and date]
Confidence: Verified / Partial / Blocked
```

Do not put guessed UUIDs in code. If protocol information is missing, return `Unsupported` or `Protocol not verified`, and document the exact evidence needed to continue.

## 10.4 Device identity and filtering

- Do not identify Eclipse solely by a user-visible Bluetooth name.
- Use vendor/model identifiers only when documented and actually available.
- Treat RSSI as signal strength, not identity.
- If identity cannot be proven, label the scan result “Possible match” and require user confirmation.
- Never automatically write commands to every nearby Ronin device.
- Never send a command to an unverified device.

---

# 11. State handling and user feedback

All hardware operations must use a state machine, not independent booleans that can contradict each other.

For each operation:
1. Validate permissions and connection.
2. Validate the capability is supported.
3. Set a visible pending state.
4. Send the verified command.
5. Wait for a protocol-level acknowledgement or readback when supported.
6. Update state only from confirmed evidence.
7. On timeout or failure, restore the last confirmed state and show an actionable message.
8. Record a privacy-safe local diagnostic event without storing audio or secrets.

Example messages:
- “Connecting to Eclipse…”
- “Connected. Checking supported controls…”
- “ANC change requested; waiting for device confirmation.”
- “ANC enabled.” — only after confirmation.
- “The device did not confirm the change. The displayed mode has not been changed.”
- “Battery information is not available from this device/firmware.”
- “This feature is not exposed by the detected firmware.”
- “Bluetooth permission is needed to find nearby earbuds.”
- “Microphone permission is needed only while Hearing Assist is running.”

Do not use “Success!” for an operation that was merely attempted.

---

# 12. Local preferences and EQ implementation

Use DataStore for small settings and profiles. Use a versioned schema and safe migration defaults.

Example EQ data model:

```kotlin
package com.example.eclipsecompanion.domain

data class EqBand(
    val centerHz: Int,
    val gainDb: Float
)

data class EqProfile(
    val id: String,
    val name: String,
    val bands: List<EqBand>,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long
)

fun validateEqProfile(profile: EqProfile): Result<EqProfile> {
    if (profile.name.isBlank()) {
        return Result.failure(IllegalArgumentException("Profile name cannot be empty"))
    }
    if (profile.bands.isEmpty()) {
        return Result.failure(IllegalArgumentException("At least one EQ band is required"))
    }
    if (profile.bands.any { !it.gainDb.isFinite() }) {
        return Result.failure(IllegalArgumentException("EQ values must be finite"))
    }
    return Result.success(profile)
}
```

Adapt band count, center frequencies, and valid gain range to the actual device protocol if hardware EQ is used. Do not use this local model to claim the device accepts a particular band layout.

Required tests:
- Editing one band changes only that band's gain.
- Save/reload preserves exact band values.
- Reset restores documented defaults.
- Rename and delete affect only the intended profile.
- Invalid, infinite, and NaN values are rejected.
- Hardware apply failure does not mark the profile as applied.

---

# 13. Hearing Assist audio-engine design

Implement the audio engine as a separate module with a clear lifecycle:

```kotlin
package com.example.eclipsecompanion.audio

enum class HearingAssistPhase {
    STOPPED,
    REQUESTING_PERMISSION,
    PREPARING_AUDIO,
    RUNNING,
    PAUSED,
    ERROR
}

data class HearingAssistSettings(
    val inputGain: Float = 1.0f,
    val outputGain: Float = 0.15f,
    val speechClarity: SpeechClarity = SpeechClarity.OFF,
    val rumbleReduction: RumbleReduction = RumbleReduction.MILD,
    val limiterEnabled: Boolean = true
)

enum class SpeechClarity { OFF, MILD, MODERATE }
enum class RumbleReduction { OFF, MILD }
```

Implementation rules:
- Validate gain ranges; do not accept arbitrary unbounded values.
- Start at a conservative output level.
- Never assume the phone microphone can be streamed to a Bluetooth media route with low latency on all Android devices.
- Test actual routing on physical Android hardware.
- Do not use a Web Audio API, browser microphone, or remote AI processing.
- Do not write captured audio to a file.
- Do not add recording or transcription features.
- Keep audio processing off the main thread.
- Use a bounded ring buffer and preallocated audio buffers where practical.
- Use a simple stable filter/limiter implementation with documented coefficients and unit tests. Do not invent clinical hearing correction.
- Stop on fatal errors and release `AudioRecord`, `AudioTrack`, audio focus, and foreground service resources.
- Test calls, alarms, screen lock, app backgrounding, route changes, unplug/disconnect, permission revocation, and low-memory conditions.
- Show “Hearing Assist stopped because the audio route changed” when appropriate.
- Do not automatically resume after a route change without user confirmation.

Important Bluetooth limitation: standard Bluetooth media output may not expose the phone microphone loopback at acceptable latency. Some communication routes use a hands-free profile and may affect music quality. Implement only what can be verified on physical hardware, and show a compatibility explanation when not available.

---

# 14. Compose UI component contracts

Build reusable components with real state and accessibility semantics:
- `ConnectionStatusPill`
- `EarbudBatteryCard`
- `CaseBatteryCard`
- `ListeningModeSelector`
- `DeviceIllustration`
- `FeatureAvailabilityNotice`
- `OperationProgressRow`
- `ErrorBanner`
- `EmptyState`
- `PermissionExplanationDialog`
- `ConfirmActionDialog`
- `EqBandSlider`
- `TouchGestureRow`
- `HearingAssistGainControl`
- `AudioRouteStatus`
- `PrivacyStatusCard`

All UI state must come from ViewModels/repositories. Never hard-code “Connected”, `100%`, “ANC active”, or “Firmware up to date” in production screens.

Each screen must support:
- Loading state.
- Empty state.
- Unsupported state.
- Permission-denied state.
- Connection-lost state.
- Error state with retry where safe.
- Success state backed by real evidence.

Compose state rules:
- Use `StateFlow` from ViewModels.
- Collect with lifecycle-aware collection.
- Do not store hardware state only in `remember` if it must survive recomposition.
- Do not launch Bluetooth writes directly from composable rendering.
- Debounce slider writes and apply them in a controlled queue when appropriate.
- Provide explicit Apply/Cancel for settings that should not stream every movement to hardware.
- Do not send a command from `LaunchedEffect` merely because a screen was composed unless it is a safe, idempotent read.

---

# 15. Manifest and configuration checklist

Generate a complete `AndroidManifest.xml` with:
- Correct app label and icon.
- Bluetooth permissions conditioned on SDK versions.
- Microphone permission only for Hearing Assist.
- Foreground service declaration only if required by the implemented audio session.
- Correct service type and permission declarations for target SDK.
- No unnecessary exported components.
- Explicit `android:exported` for components with intent filters.
- No `INTERNET` permission by default.
- No cleartext traffic.
- No broad storage permission.
- No boot receiver or automatic startup behavior.
- No hidden service that continues running after the user stops a session.

Use current stable, mutually compatible dependency versions at generation time. Do not invent versions. Check that Gradle, Kotlin, Compose compiler/plugin, and Android Gradle Plugin versions are compatible. Include the Gradle wrapper.

---

# 16. Error handling

Provide typed errors for:
- Bluetooth unavailable.
- Bluetooth disabled.
- Permission denied.
- Scan timeout.
- No compatible device.
- Ambiguous device identity.
- GATT connection failure.
- Service discovery failure.
- Missing characteristic.
- Unsupported operation.
- Protocol not verified.
- Command timeout.
- Invalid response.
- Device rejected command.
- Disconnected mid-operation.
- Audio route unavailable.
- Microphone permission denied.
- Audio initialization failure.
- Audio underrun/overrun.
- Firmware package invalid.
- Firmware update unsupported.
- Local settings read/write failure.

User-facing error messages must be understandable and should offer a safe next step. Technical details can be available in a diagnostics view without leaking personal data.

---

# 17. Diagnostics and privacy-safe logging

Add an on-device diagnostics screen showing:
- Android version.
- App version.
- Bluetooth enabled/permission state.
- Device name/alias and verified identity status.
- Connection phase.
- Protocol capability status.
- Last successful battery/mode read.
- Last operation result and error category.
- Audio route and Hearing Assist status, if relevant.

Never log:
- Raw microphone audio.
- Audio buffers.
- Credentials or encryption keys.
- Proprietary authentication tokens.
- Unique identifiers that are not needed.
- Full nearby-device scan history.
- Sensitive notification contents.

Logs must be local, bounded, and clearable. No upload function. If exporting diagnostics is implemented, require explicit user action and preview the contents before sharing. Redact MAC addresses and other identifiers by default.

---

# 18. Testing strategy

## 18.1 Unit tests

Test:
- Battery percentage validation.
- Unknown vs zero battery.
- Stale data labels.
- Capability-gated UI logic.
- Protocol response parsing.
- Malformed and truncated payload handling.
- Command queue serialization.
- Operation timeout and cancellation.
- State machine transitions.
- Independent EQ band editing.
- EQ profile persistence and migration.
- Touch mapping validation.
- Hearing Assist gain clamping.
- DSP stability, finite output, clipping limiter, and filter behavior.
- No success state on failed or unconfirmed commands.

## 18.2 Instrumented tests

Test:
- First launch with no permissions.
- Bluetooth permission grant/deny/permanent deny.
- Bluetooth disabled.
- Scan and cancellation.
- Connection and disconnect lifecycle.
- Rotation/recomposition while connecting.
- App background/foreground.
- Audio route changes.
- Microphone permission denied.
- Hearing Assist stop/release.
- Theme and font scaling.
- Navigation on small and large screens.

## 18.3 Physical-device matrix

At minimum, test on:
- The actual Ronin Eclipse White hardware.
- The user's primary Android phone.
- At least one additional Android version/device if available.

For each test, record:
- Earbud model/revision and firmware version.
- Phone model and Android version.
- Whether Android reports the earbuds as connected.
- Whether the companion control connection succeeds.
- Command sent and response evidence, without secrets.
- Expected and actual result.
- Whether settings survive disconnect/power cycle.

Do not mark a hardware feature as verified based only on emulator tests.

## 18.4 Acceptance checklist

- [ ] App launches in airplane mode.
- [ ] No sign-in or account flow exists.
- [ ] No internet permission or network dependency is present by default.
- [ ] Scan and connection states are honest.
- [ ] Battery values are real or “Not available”.
- [ ] Left, right, and case battery are not conflated.
- [ ] ANC/Transparency/Normal changes are confirmed by the device when supported.
- [ ] Unsupported controls are disabled and explained.
- [ ] EQ bands are independent.
- [ ] Touch mappings are only applied when verified.
- [ ] Hearing Assist asks for permission only when started.
- [ ] Hearing Assist never records or uploads audio.
- [ ] Hearing Assist has a visible stop action and conservative gain defaults.
- [ ] Audio resources are released on every exit path.
- [ ] No unverified firmware update or reset command can be sent.
- [ ] All screens have loading, error, empty, and unsupported states.
- [ ] Dynamic font scaling and screen-reader descriptions work.
- [ ] Unit tests pass.
- [ ] Android build succeeds.
- [ ] Installation and physical-device test instructions are complete.
- [ ] No TODOs, placeholder success handlers, fake values, or dead buttons remain in core flows.

---

# 19. Build, install, and final deliverables

Generate these deliverables:

1. Full Android Studio project with all source files.
2. All local vector icons and app icon assets.
3. Optional original SVG favicon and product illustration.
4. `README.md` with prerequisites, build, install, permissions, and offline behavior.
5. `DEVICE_CAPABILITY_MATRIX.md`.
6. `BLUETOOTH_PROTOCOL_EVIDENCE.md`.
7. `PRIVACY.md`.
8. `TEST_PLAN.md` and actual test results.
9. `TROUBLESHOOTING.md`.
10. `BUILD_AND_INSTALL.md`.
11. A list of verified features, blocked features, and exact missing evidence needed for blocked features.
12. APK only if the environment can genuinely build it; never claim an APK was generated when it was not.

Run the project build and tests if the environment supports it. Fix compile errors and lint findings. If the environment cannot execute Gradle or access an Android device, state that clearly and provide the exact commands the user must run locally.

Suggested verification commands (adjust only if the generated project requires a documented alternative):

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Windows PowerShell:

```powershell
.\gradlew.bat test
.\gradlew.bat lint
.\gradlew.bat assembleDebug
```

Do not report these commands as passed unless they were actually executed and succeeded.

---

# 20. README required content

The README must explain:
- What the app is and is not.
- Exact supported hardware model.
- Why proprietary Bluetooth protocol verification is required.
- Which features have been physically verified.
- Which features are blocked or unsupported.
- Offline guarantee and permissions.
- How to build and install.
- How to connect.
- How to interpret “Not available”.
- Hearing Assist limitations and safety.
- Firmware-update safety status.
- Privacy model.
- Troubleshooting steps.
- How to submit a useful local diagnostic report without exposing sensitive identifiers.

Do not describe the app as an official Ronin app. Do not imply official endorsement.

---

# 21. Specific corrections and quality improvements to enforce

The following are mandatory quality safeguards and should be implemented wherever applicable:

1. **Independent EQ bands:** A known class of EQ UX defect is neighboring sliders moving together. Each band must be independently editable and covered by regression tests.
2. **Confirmed ANC state:** Do not display the requested mode as active before confirmation. Separate command-sent from device-confirmed.
3. **Honest battery:** Show “Not available” when the protocol does not report a battery value. Never substitute a guessed case percentage.
4. **Reliable touch mapping:** Persist settings only after validation; distinguish local draft from confirmed device configuration.
5. **Stable connection lifecycle:** Avoid app-start crashes, stale GATT callbacks, duplicate connection attempts, and stuck loading states.
6. **No phantom features:** Every button must work, be explicitly disabled with a reason, or be removed. Never leave a decorative button that appears functional.
7. **No fake firmware support:** Do not add an OTA page with a fake “Check for updates” result if no official update service/package protocol is available offline.
8. **No misleading transparency slider:** Only expose a real hardware level if the firmware supports it.
9. **No unsafe audio loop:** Hearing Assist must use explicit start/stop, low initial gain, peak limiting, route checks, and no recording.
10. **No false global EQ:** Do not claim an in-app EQ changes all phone audio unless the implemented Android audio route actually supports it.
11. **No account or cloud sync:** All supported settings must remain local.
12. **No copied assets:** Create original icons and artwork rather than extracting proprietary app images.

---

# 22. Final implementation response format

After implementation, report:

## Build status
- Build attempted: Yes/No
- Build result: Passed/Failed/Not executable in this environment
- Tests attempted and actual results
- APK path only if it exists

## Hardware verification
- Exact device and firmware tested
- Connection method
- Verified controls and evidence
- Unverified/blocked controls and why

## Feature table

| Feature | UI implemented | Hardware/local implementation | Verified on actual Eclipse |
|---|---|---|---|
| Discovery/connect | Yes/No | Yes/No | Yes/No |
| L/R battery | Yes/No | Yes/No | Yes/No |
| Case battery | Yes/No | Yes/No | Yes/No |
| ANC/Transparency/Normal | Yes/No | Yes/No | Yes/No |
| Transparency level | Yes/No | Yes/No | Yes/No |
| EQ | Yes/No | Hardware/local/unsupported | Yes/No |
| Touch mapping | Yes/No | Yes/No | Yes/No |
| Gaming mode | Yes/No | Yes/No | Yes/No |
| Hearing Assist | Yes/No | Local audio route status | Yes/No |
| Meditation | Yes/No | Local audio/hardware command | Yes/No |
| Rename | Yes/No | Local alias/device rename | Yes/No |
| Firmware info/update | Yes/No | Read-only/update/unsupported | Yes/No |
| Factory reset | Yes/No | Yes/No | Yes/No |

## Outstanding blockers
For every blocked feature, state:
- The exact missing protocol/documentation/hardware evidence.
- What can already be tested.
- What must not be claimed as working.

## Final quality gate
Do not call the app “complete” if key hardware controls are only mocked or unverified. A truthful, buildable app with verified controls and clearly documented blockers is better than a visually complete but nonfunctional imitation.

---

# 23. Execution instruction

Now implement this specification as a complete native Android project. Start by writing the protocol evidence and capability matrix. Build the app shell and all local functionality, then implement only verified Eclipse commands. Use original local assets, provide the complete codebase and documentation, run the available build/tests, and correct all failures you can reproduce.

Do not stop after producing another plan. Do not generate filler screens or fake device data. Do not invent undocumented device commands. Do not ask the user to sign in. Do not add network dependencies. Do not claim untested features are working.

If proprietary protocol evidence is the only blocker for a feature, finish the rest of the app and provide a concise, precise evidence request instead of simulating that feature.


---

# 24. Expanded review findings and missing requirements

This section extends and strengthens the requirements above. If any earlier section conflicts with this addendum, use the more conservative, more testable requirement here.

## 24.1 Official-app feature parity checklist

The official RONiN STUDIO public listing describes these features for compatible software-based Ronin earbuds: left/right battery status, ANC/Transparency/Normal switching, custom EQ and presets, touch control customization, Gaming Mode, device renaming, Meditation Mode, OTA software updates, instant pop-up pairing, product manuals/details, Hearing Assist using the phone microphone, support access, pairing/connection stability improvements, touch-control improvements, factory reset, and EQ improvements.

Do not assume every generic app feature is supported by every Ronin model. Build a feature-by-feature compatibility record for **Ronin Eclipse R-7065, Snow White**, and distinguish:
- Advertised for the companion app generally.
- Advertised for Eclipse specifically.
- Verified on the user's exact hardware/firmware.
- Available as local app functionality.
- Not available on this hardware/firmware.
- Blocked pending protocol evidence.

Add `docs/OFFICIAL_FEATURE_PARITY.md` with columns:

| Feature | Official listing says | Eclipse-specific evidence | Implementation type | Test result | Final status |
|---|---|---|---|---|---|
| Left/right battery | Yes | Record source/test | Device protocol | Record result | Verified/Blocked/Unsupported |
| Charging-case battery | Do not assume | Verify explicitly | Device protocol | Record result | Verified/Blocked/Unsupported |
| ANC/Transparency/Normal | Yes | Product listing confirms modes; commands still need verification | Device protocol | Record result | Verified/Blocked/Unsupported |
| Transparency intensity | App/product wording must be checked | Verify adjustable level range | Device protocol | Record result | Verified/Blocked/Unsupported |
| Custom EQ | Yes | Verify actual EQ band format | Device protocol or local DSP | Record result | Verified/Blocked/Unsupported |
| Artist/factory presets | Yes in app listing/product context | Obtain real preset list or label app-created presets separately | Device protocol/local profiles | Record result | Verified/Blocked/Unsupported |
| Touch control customization | Yes | Verify gesture/action matrix | Device protocol | Record result | Verified/Blocked/Unsupported |
| Gaming Mode | Generic app listing mentions it; Eclipse-specific support must be verified | Do not assume supported | Device protocol | Record result | Verified/Blocked/Unsupported |
| Device rename | Yes | Distinguish local alias from earbud firmware name | Device protocol/local alias | Record result | Verified/Blocked/Unsupported |
| Meditation Mode | Yes | Establish whether phone audio or internal earbud feature | Local audio/device protocol | Record result | Verified/Blocked/Unsupported |
| OTA firmware update | Yes | Official package, signing, transport, recovery required | Device protocol | Record result | Verified/Blocked/Unsupported |
| Instant pairing popup | Yes | Verify Android implementation and device protocol | Android UI/device protocol | Record result | Verified/Blocked/Unsupported |
| Manuals/product details | Yes | Bundle redistributable material or write local help | Local documents | Record result | Verified/Blocked/Unsupported |
| Hearing Assist | Added in official app listing | Phone microphone and output routing must be tested | Local audio engine | Record result | Verified/Blocked/Unsupported |
| Factory reset | Listing/update notes mention improved reset | Verify exact reset method for this model | User-confirmed hardware action | Record result | Verified/Blocked/Unsupported |
| Local support | Official app offers support access | Offline help first; external contact is optional | Local help/external link | Record result | Verified/Blocked/Unsupported |

Do not mark the project “official-feature complete” until every row has an evidence-based final status.

## 24.2 Hardware facts must not be conflated

The public Eclipse product listing identifies the model as **R-7065**, Bluetooth 5.3, 10 mm drivers, 60 mAh battery per earbud, 700 mAh charging case, ANC and transparency, and no dual-device connection. Published battery and standby figures vary across listings, so do not hard-code marketing runtime as a live prediction. These facts are useful for identifying the product, but do not establish proprietary protocol details.

Rules:
- Do not advertise multipoint or two-device simultaneous connection for this model unless new official evidence changes that specification.
- Do not infer actual remaining playback time from battery percentage alone.
- Do not promise a specific ANC reduction in dB unless verified for this model under a documented measurement method.
- Do not confuse **ENC** (microphone noise reduction for outgoing calls) with **ANC** (noise reduction heard by the listener).
- Do not label the call microphone noise-reduction feature as a general-purpose user-adjustable ANC setting.
- Do not claim a water-resistance rating; the cited product specification does not list one.
- Do not infer that the case exposes a readable battery percentage just because it has a battery.
- Do not infer that the Eclipse supports Gaming Mode solely because the generic app listing mentions Gaming Mode.
- Do not infer multipoint from Bluetooth 5.3.

## 24.3 Device compatibility and first-run diagnostics

Add a first-run compatibility wizard:
1. Explain that the app is independent and works offline.
2. Explain that it needs Nearby Devices permission to find/connect to earbuds.
3. Ask the user to put both earbuds in the documented pairing/discovery state.
4. Scan for a bounded interval with a visible cancel action.
5. Show candidate device name, signal strength if available, and confidence level.
6. Ask the user to select the correct device if identity is ambiguous.
7. Connect using only the verified transport.
8. Discover services/capabilities without sending undocumented writes.
9. Read safe, documented status values.
10. Display a clear report: “Compatible”, “Partially compatible”, “Device found but protocol unverified”, or “No compatible device found”.
11. Save only the selected device/alias and necessary local preferences.
12. Provide a “Run connection diagnostics” action that is read-only unless the user explicitly chooses a verified control test.

Add a safe diagnostic report with:
- Scan success/failure.
- Device identity confidence.
- GATT/service discovery outcome if GATT is the verified transport.
- Supported characteristic properties.
- Notification subscription outcome.
- Read-only status read results.
- Permission state.
- Android version and app version.
- No raw audio, no encryption secrets, and no unnecessary permanent identifiers.

## 24.4 Connection reliability enhancements

Implement:
- Bounded scan timeout and cancel action.
- Connection timeout and service-discovery timeout.
- Operation queue with one command in flight unless protocol evidence supports another approach.
- Bounded retry policy with backoff.
- No endless auto-reconnect loops.
- Clear “Retry” and “Forget device” actions.
- Stale callback rejection using a session/generation identifier.
- Safe GATT cleanup on activity recreation, disconnect, Bluetooth disable, permission revocation, and app process death.
- Recovery when the user opens Android Bluetooth settings and returns.
- Duplicate tap protection on Connect and mode controls.
- A visible “Last connected” timestamp, not a fake live indicator.
- A distinction between Android audio pairing and the companion control link.
- A diagnosis explaining that earbuds can play audio even if proprietary app-control protocol is unavailable.
- No automatic firmware reset as a troubleshooting step.
- No automatic “factory reset” as a connection repair.

Test rapid connect/disconnect, leaving and re-entering the screen, screen rotation, phone Bluetooth toggle, one earbud removed, earbuds put in the case, and app process recreation.

## 24.5 Connection pop-up / pairing experience

Implement an app-local pairing card when the app detects a verified compatible device while it is open. Do not promise an operating-system-level pop-up identical to the official app unless Android APIs and the device support it.

- Provide a single-tap “Connect” action.
- Avoid interrupting a different active audio session.
- Do not scan continuously without user intent.
- Do not start an activity unexpectedly from the background.
- Do not require location permission on modern Android when it is not needed for the chosen scan method.
- Evaluate Android Companion Device Manager where appropriate, but do not assume it supports the proprietary control channel by itself.
- Keep the normal manual scan flow available if the pop-up flow is not supported.

## 24.6 Battery details and battery health

Battery UI enhancements:
- Left and right battery display must remain separate.
- Case battery is a separate capability.
- Show whether a value is live, recently reported, stale, approximate, or unavailable.
- Use an explicit last-read time.
- Allow manual refresh if supported.
- Avoid polling so frequently that it drains the phone or earbud battery.
- Use a conservative, documented polling interval only after connection behavior is tested.
- Stop polling when disconnected or when the app is not in a state where updates are needed.
- Show a local low-battery warning at user-configurable thresholds.
- Do not claim battery health, cycle count, or degradation unless the device reports that information.
- Do not display “charging” unless the protocol reports charging or a documented reliable inference exists.
- If left/right values are reported as a combined value only, label them combined; never split one number into two invented values.
- Provide an explanatory help sheet describing why the case may be unavailable (for example, the case may not expose data while closed or disconnected), but only state model-specific reasons when verified.

## 24.7 ANC and Transparency quality-of-life features

Add the following only where the device protocol supports them:
- Quick mode selection.
- Optional remembered preferred mode.
- Optional prompt to restore a preferred mode after reconnecting, instead of silently writing settings.
- Adjustable ANC strength or transparency level only if the hardware provides real steps/range.
- A clear “ANC intensity not supported” state where only mode on/off exists.
- A local explanation of ANC vs ENC vs Transparency.
- A safety reminder that transparency does not guarantee awareness of traffic or other hazards.
- An optional timer/reminder for extended listening, fully local and user-controlled.
- A low-battery recommendation to disable power-intensive features only if the manufacturer behavior is documented or the recommendation is clearly described as general advice.

Do not claim to improve the physical microphone array's transparency quality through a phone-side slider. The phone-microphone Hearing Assist is a separate signal path.

## 24.8 EQ accuracy, sound quality and regression prevention

EQ is a priority area. A past user review of the official app complained that the custom EQ did not allow independent frequency adjustment. The replacement app must explicitly prevent this failure.

Required design:
- Each band has its own value and state key.
- Do not derive adjacent band values from the selected band's value.
- Do not use one shared mutable variable for every slider.
- Provide accessible numeric values in dB.
- Add keyboard/stepper adjustments for precision.
- Provide a visible flat/reset line.
- Allow a user to edit a band, leave the screen, return, and see the exact same value.
- Clearly label factory presets versus app-created presets.
- Never invent the names of official “artist presets”.
- If a list of official presets is available only from the device, read it from the device. Otherwise call custom presets “Eclipse Companion presets”.
- Prevent clipping in local DSP and warn when boosts exceed the safe headroom.
- Keep local EQ data separate from the last confirmed hardware EQ state.
- Do not mark a profile as applied if the command fails or cannot be confirmed.
- If device readback is unavailable, label the state “Last command sent” rather than “Current earbud EQ”.
- If hardware uses a fixed number of EQ points, map exactly to those points; do not pretend an arbitrary 10-band layout maps to a different device format.

Mandatory regression test:
1. Create an EQ profile with several bands at different values.
2. Change one band by a known amount.
3. Assert that every other band remains byte-for-byte/value-for-value unchanged.
4. Save and reload the profile.
5. Assert all values are preserved.
6. Simulate a device command rejection.
7. Assert that the local profile remains saved but the device-applied status is not falsely updated.

Add tests for extreme bass boost, simultaneous slider drags, profile switching, reset, invalid values, and screen recreation.

## 24.9 Music playback and sound settings boundaries

The companion app is not automatically a system-wide audio equalizer or a replacement music player.

- Do not advertise a system-wide EQ unless an actual supported Android audio effect path is implemented and tested.
- Do not capture, record, or re-route music from other apps using inaccessible/private APIs.
- Do not request accessibility-service privileges to intercept audio.
- Do not request notification access merely to identify songs.
- If the app includes its own local test tone or relaxation player, label its processing as applying only to audio played by this app.
- Do not bundle copyrighted commercial tracks.
- Respect audio focus, phone calls, alarms, other media apps, and Bluetooth route changes.
- Do not claim the app changes earbud firmware EQ if it only changes local app audio.
- Add an “Audio settings scope” help page explaining exactly which audio paths are affected.

## 24.10 Call audio and microphone regression tests

Because current public app reviews report problems with call microphone/noise reduction and distorted sound after updates, add a dedicated regression test plan.

Test on the physical Eclipse:
- Incoming and outgoing calls.
- Both earbuds connected versus only one earbud connected.
- Music playing before a call.
- Call interruption and return to music.
- ANC/Transparency mode change during a call, if supported.
- Hearing Assist stopped before a call.
- Hearing Assist start attempt while a call is active.
- Bluetooth route changes.
- Media volume versus call volume behavior.
- Left/right channel behavior.
- Distortion, clipping, choppy audio, dropouts, echo, feedback, and unexpectedly distant microphone pickup.
- Repeated connection/disconnection during a call.
- After changing EQ and then making a call, verify the call path is not corrupted.
- After disconnect/reconnect, verify the selected EQ profile has not falsely changed the microphone/ENC state.

Important:
- App-side code must not claim to improve the earbuds' proprietary outgoing-call ENC unless the vendor exposes a supported setting.
- Do not alter call microphone routing in an undocumented way.
- Keep Hearing Assist capture and outgoing call microphone use separate.
- Stop Hearing Assist during calls by default.
- If a defect appears, capture a local redacted diagnostic record and give the user a safe recovery suggestion. Do not silently reset the earbuds.

## 24.11 Hearing Assist advanced safeguards

Treat Hearing Assist as an assistive listening experiment, not a clinical hearing device.

Add:
- A short setup checklist: quiet environment, phone positioned near the speaker, earbuds fitted comfortably, start at low level.
- An on-screen “Stop now” control with high visual priority.
- Safe starting output level every time.
- A short fade-in and fade-out to reduce abrupt changes.
- A hard maximum digital gain enforced in code.
- A peak limiter with unit tests.
- Clipping and buffer-underrun monitoring.
- A route-change watcher.
- Automatic stop or pause on calls, route loss, audio focus loss, microphone permission revocation, service failure, or app crash recovery.
- No automatic restart after a crash, route switch, or device reconnection.
- No raw audio saved to disk, cache, logs, or backups.
- No cloud processing, transcription, voice recognition, or speech analytics.
- A clear microphone privacy indicator while the session is active.
- A clear Android foreground notification when required.
- A session timer and optional reminder to take a break.
- A quick sound-check step before starting amplification.
- No promise of a hearing-aid-like experience, audiogram correction, or clinical gain profile.
- If there is persistent difficulty hearing speech, recommend an appropriate hearing assessment rather than progressively increasing amplification.

Technical note: Do not claim zero latency. Measure the actual end-to-end latency on physical devices. If the route is too delayed, unavailable, or unstable, explain the limitation and do not expose a false “ready” state.

## 24.12 Local sound profiles and presets

Create an optional preset library for local use:
- Balanced
- Bass
- Bass Light
- Vocal Clarity
- Podcast / Spoken Word
- Acoustic
- Classical
- Jazz
- Rock
- Electronic
- Late Night
- Speech
- Custom

These are **new app-defined presets**, not official factory or artist presets unless verified from an official source or actual device data.

Requirements:
- Presets must have actual stored values, not just a changed title.
- Preset values must match the actual supported local/hardware EQ band format.
- Users can duplicate, rename, reset, and delete custom presets.
- Include a “Flat” profile with zero gain for all supported bands.
- Switching a local preset must not send hardware commands unless the user selects Apply or the design explicitly documents immediate application.
- When applying to hardware, use a command queue and display confirmed/pending/failed states.
- Do not use a generic equalizer curve if it cannot be represented by the hardware; show a compatibility warning instead.

## 24.13 Touch-control mapping enhancements

Add:
- Clear left/right earbud tabs.
- Gesture list based only on gestures the firmware exposes.
- Current mapping, local draft mapping, and device-confirmed mapping as distinct states.
- An action-description sheet explaining each available action.
- Conflict validation if the hardware forbids duplicate assignments.
- A “Review changes” screen before applying multiple mappings.
- Rollback to the last confirmed mapping when the command fails, if protocol supports readback/rollback.
- Explicit user confirmation before any reset-to-default operation.
- No undocumented “five taps” or other reset sequence in the app unless the manufacturer documents that sequence for the exact model/revision or it has been safely confirmed from official support. If a physical reset procedure is included in help, label its evidence and tell the user to consult the current model-specific manual.
- Do not send a touch mapping command when the user merely opens the screen.

## 24.14 Firmware update security and offline behavior

The app must remain offline-first. Firmware updating is permitted only if the vendor's official firmware package and protocol can be verified.

If a local firmware package import is supported:
- Use Android's system file picker.
- Restrict accepted file types/extensions based on verified vendor package formats.
- Do not trust the filename or extension.
- Verify digital signature using the vendor's documented public key and signing scheme, or use an official integrity mechanism.
- A hash alone detects accidental changes only if the expected hash is trusted; it does not prove authenticity.
- Reject unknown firmware model, hardware revision, version, or signature.
- Reject truncated, malformed, or oversized packages.
- Never unpack to an unsafe path or execute package contents.
- Require adequate battery/charging conditions based on official instructions.
- Warn against interrupting the update.
- Use a state machine: preflight, verified, ready, transferring, validating, applying, rebooting, post-update verification, success/failure/recovery.
- Show real progress only when the protocol provides a reliable progress measure.
- Do not fake a progress bar with a timer.
- Never claim rollback/recovery is possible unless it is documented and implemented.
- Do not send firmware data through an unverified GATT characteristic.
- Do not provide a generic firmware download URL or query a server.
- If no official package/protocol is available, show “Firmware update unavailable in offline mode” with a precise explanation and keep all other features working.

## 24.15 Factory reset and recovery

Keep three separate actions:
1. Clear this app's local settings.
2. Forget this device from this app.
3. Reset the earbuds themselves.

Each must have a different label, confirmation, and outcome.

For earbud reset:
- Require a verified model-specific method.
- Explain which device settings may be erased.
- Ask the user to place the earbuds in the required state.
- Avoid resetting when the battery or connection state is unsafe if the protocol requires a threshold.
- Show actual completion only when the device confirms it.
- If the device does not support reset through the app, give model-specific manual instructions only if they are verified.
- Never conflate Android Bluetooth “Forget” with factory reset.

## 24.16 Product details and local help

Create an offline help center containing:
- Eclipse R-7065 identification guidance.
- ANC vs Transparency vs ENC explanation.
- Pairing and reconnect steps.
- Battery display limitations.
- EQ profile scope.
- Touch-control setup.
- Hearing Assist safety and troubleshooting.
- Bluetooth permissions explanation.
- Privacy explanation.
- Firmware update status and why it may be unavailable.
- Factory reset distinction.
- Connection diagnostic steps.
- Common errors and safe recovery actions.
- A “Copy diagnostics summary” button that exports a short redacted text report after user review.

Use locally stored text and original diagrams. Include official manual files only if legally redistributable or supplied by the user. External official product/support links may be offered as optional links, but the app must never require them for core operation.

## 24.17 Optional local quick actions

Consider these as optional, low-priority enhancements after core reliability is complete:
- Android Quick Settings tile for connecting or opening the app, only if platform rules and actual connection lifecycle allow it.
- A notification with a shortcut back to the active Hearing Assist session while the session is running.
- A battery/mode widget only if it can show a truthful timestamp and does not promise continuous live data.
- Local listening timer.
- Optional low-battery notification when reliable battery data is available.
- Local export/import of app settings as a versioned JSON file, excluding device secrets and audio.
- A “Restore app preferences” flow that never resets earbuds.
- English and Urdu localization if complete, accurate translations can be maintained. Do not mix languages randomly or use machine-generated strings that break layouts.
- Reduced-motion setting that respects Android accessibility settings.
- Large-text layout mode if Android font scaling alone is insufficient.

Do not add an always-on background scanner, invasive notification listener, accessibility-service hack, location tracking, ad SDK, or cloud account.

## 24.18 App settings and data portability

Add a settings page with:
- Theme: system/light/dark.
- App language: system/English/Urdu only if translations are complete.
- Battery warning thresholds.
- Preferred mode on launch (suggestion only unless automatic application is explicitly enabled).
- Confirmation before applying device settings.
- Hearing Assist safety defaults.
- Diagnostics retention and clear action.
- Export local profiles.
- Import local profiles.
- Restore app defaults.
- Privacy and permission status.

For profile export/import:
- Use a versioned, documented JSON schema.
- Validate every field and size limit.
- Reject unknown or malformed versions safely.
- Never import executable content.
- Do not include Bluetooth keys, authentication secrets, raw audio, MAC addresses by default, or private device identifiers.
- Preview the number of profiles to import and require confirmation.
- Handle duplicate names with a user-visible choice.
- Do not write imported settings to earbuds until the user chooses Apply.

## 24.19 Accessibility and inclusive UX

The app must support:
- TalkBack labels and sensible traversal order.
- Minimum 48dp targets.
- High contrast.
- System font scaling without clipped controls.
- Meaning conveyed by text/icon as well as color.
- Large, readable battery values.
- Clear active/pending/error distinctions.
- No flashing animation.
- Reduced motion.
- One-handed use on compact screens.
- Simple mode for family members who want only battery, ANC/Transparency, and Hearing Assist.
- Confirmation before destructive actions.
- A non-technical error explanation with optional technical details.

The app must never suggest that the user increase Hearing Assist gain repeatedly to compensate for an unknown hearing problem.

## 24.20 Security review

Add a `docs/SECURITY_REVIEW.md` containing:
- Data inventory.
- Permission justification.
- Threat model for nearby-device spoofing and wrong-device control.
- Command allowlist.
- Firmware authenticity strategy.
- Local data storage choices.
- Log redaction strategy.
- Audio privacy design.
- Dependency audit.
- Release signing guidance.
- Known limitations.

Rules:
- Verify device identity as far as the available protocol allows.
- Do not send commands to an arbitrary nearby device.
- Do not store secrets in plaintext preferences.
- Do not log proprietary command payloads if they may contain secrets.
- Use a strict allowlist of supported commands.
- Validate payload length, range, enum values, and checksum before writing.
- Reject unknown protocol versions rather than guessing.
- No dynamic code loading.
- No remote configuration.
- No analytics or advertising.
- No cleartext network traffic.
- Do not embed private signing keys in the app or repository.

## 24.21 Dependency and reproducibility controls

- Pin dependency versions.
- Use Gradle dependency verification/checksums where practical.
- Avoid unnecessary libraries.
- Do not include a library just to draw a basic icon.
- Document any native audio/DSP library and its license.
- Do not fetch dependencies at runtime.
- The build may need internet access on the developer's machine to obtain Gradle dependencies the first time; distinguish this from the app's runtime offline guarantee.
- After dependencies are cached, verify whether the project can build offline with Gradle's offline mode.
- Add a dependency inventory and license notice.
- Ensure there are no hidden telemetry libraries.
- Do not claim fully reproducible builds unless the toolchain and dependencies are actually pinned and verified.

## 24.22 Performance and stability targets

Define measurable targets and record real results:
- No main-thread Bluetooth I/O.
- No main-thread audio processing.
- No unbounded polling or retry loops.
- No unbounded audio buffers.
- No battery polling while disconnected.
- No ANR or crash during repeated connection/disconnection tests.
- UI controls respond immediately with pending feedback.
- Audio session starts/stops cleanly on tested devices.
- App handles loss of Bluetooth, microphone permission, and audio route without crashing.
- Profile editing remains smooth with large fonts and screen rotation.
- No false success after timeout.
- No stale connection callback overwrites a newer session state.

Do not invent benchmark results. Record the device, OS, test steps, and actual measured results.

## 24.23 Expanded QA matrix

Add a test matrix for:
- Fresh install offline.
- App launched without any earbuds paired.
- Earbuds paired in Android settings but proprietary protocol unavailable.
- Wrong Ronin model nearby.
- Two similar devices nearby.
- Earbuds in case versus out of case.
- One earbud disconnected.
- Case not reporting battery.
- Bluetooth switched off mid-command.
- Permission revoked mid-scan.
- Permission revoked during Hearing Assist.
- Rapid mode changes.
- Duplicate Apply taps.
- Malformed response payload.
- Command acknowledged at GATT level but rejected at application protocol level.
- App killed during pending command.
- Device reconnect with stale callback.
- Android system call during Hearing Assist.
- Audio focus loss.
- Headphone route switched.
- User stops Hearing Assist while audio buffer is active.
- Extreme font scale.
- TalkBack navigation.
- Dark/light theme switch.
- EQ editing with independent band assertions.
- Import malformed profile file.
- Firmware package with wrong model/signature.
- Local data clear versus forget device versus earbud reset.
- Repeated open/close and process recreation.

For every test, record expected behavior and actual result. Do not mark “passed” merely because the app did not crash.

## 24.24 Release readiness and store compliance

Before a release:
- Use a unique application ID selected for the user's project.
- Do not use `com.yscoco.ronin` or impersonate the official app package.
- Use an original app name, icon, screenshots, and store listing.
- Explain that this is an independent companion project and is not endorsed by Ronin.
- Request only permissions required by actual implemented features.
- Provide a privacy policy appropriate to the actual offline data behavior if distributing publicly.
- Do not claim medical certification or hearing-aid status.
- Do not promise compatibility with every Ronin model.
- Include device-specific limitations.
- Verify Android target SDK and foreground-service rules at release time.
- Do not publish firmware or proprietary files without authorization.
- Include an app version and a documented release checklist.

---

# 25. Corrected source-of-truth and research workflow

Use official and primary sources first:
1. Google Play listing for RONiN STUDIO: `https://play.google.com/store/apps/details?id=com.yscoco.ronin`
2. Ronin Eclipse R-7065 official product page: `https://ronin.pk/products/eclipse-earbuds`
3. Android developer documentation for Bluetooth permissions, BLE lifecycle, audio routing, and foreground services.
4. Official Eclipse user manual and feature booklet if the user provides them or they are publicly accessible and legitimate.
5. Actual hardware tests on the user's Eclipse earbuds.

Do not treat a retailer's product listing as a protocol specification. If two public pages disagree about battery standby time or a feature, preserve the discrepancy in the research notes and do not bake it into app behavior.

Create `docs/SOURCE_AND_ASSUMPTION_REGISTER.md`:
- Claim or requirement.
- Source URL or test ID.
- Source type: official / retailer / observed / assumption.
- Date checked.
- Confidence.
- How the claim affects implementation.
- Whether the claim is safe to expose to the user.

Any feature based on an assumption must remain unavailable as a hardware control until verified.

---

# 26. Mandatory deliverable expansion

In addition to the files listed in Section 19, generate:
- `docs/OFFICIAL_FEATURE_PARITY.md`
- `docs/SOURCE_AND_ASSUMPTION_REGISTER.md`
- `docs/SECURITY_REVIEW.md`
- `docs/RELEASE_CHECKLIST.md`
- `docs/AUDIO_ROUTING_AND_HEARING_ASSIST.md`
- `docs/PHYSICAL_DEVICE_TEST_RESULTS.md`
- `docs/KNOWN_LIMITATIONS.md`
- `docs/DEPENDENCY_AND_LICENSE_INVENTORY.md`

Add a test-results table that includes actual outcomes, not only planned tests.

For each feature, include:
- Source files implementing it.
- Capability gate used.
- Unit tests.
- Instrumented tests.
- Physical device test result.
- Current limitations.
- Whether it works without internet.
- Whether it writes to the earbuds or only changes local app data.

---

# 27. Revised final instruction to Gemini

Implement the complete native Android app described in this master prompt and its expanded review requirements.

First, research the official public feature list and Eclipse R-7065 specifications. Create the source register, official feature parity matrix, capability matrix, and protocol evidence log. Then build the app architecture, all screens, local persistence, offline help, original assets, robust BLE discovery/connection handling, and the local Hearing Assist audio engine where Android routing can be implemented and tested safely.

Next, implement actual hardware controls only where exact Eclipse protocol evidence is available. Do not invent UUIDs, opcodes, battery values, EQ layouts, factory presets, firmware files, or reset sequences. Every button must perform a real action, be visibly disabled with a truthful reason, or be omitted.

Prioritize these regressions:
1. Independent EQ-band editing and persistence.
2. Correct mode confirmation.
3. Stable connection and reconnection.
4. Accurate battery reporting.
5. Hearing Assist safe audio routing and stop behavior.
6. Call/music audio coexistence and no microphone-routing regressions.
7. Honest firmware/update/reset handling.
8. Full offline operation and no unwanted data collection.

Generate all source code, resources, tests, and documentation as real files. Build and test what the environment allows. Fix reproducible failures. Never say a feature is verified unless the required test was actually performed.

At the end, provide:
- A downloadable project or exact project file set.
- Actual build/test results.
- The verified feature table.
- The blocked feature table with exact missing evidence.
- A clear distinction between UI complete, local feature complete, protocol implemented, and physically verified on the user's Eclipse.
- Any specific material the user must provide (for example, official manual, firmware package, or protocol evidence) to unlock a blocked feature.

The app must be production-oriented, offline-first, secure, accessible, and truthful. A fully documented limitation is acceptable; fake functionality is not.
