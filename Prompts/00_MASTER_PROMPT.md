# Eclipse Companion — Master Build Prompt (Google AI Studio / Gemini)

**Version 3.0 · 30 Oct 2026 · For: Ronin Eclipse R-7065 (White / Snow White)**

This is file **00**. Give Gemini **all ten files** (00-05, with 02B/02C/02D) together, in this order. `06_GAP_ANALYSIS_AND_CHANGELOG.md` is for the human only; do not give it to Gemini:

| # | File | Contents |
|---|------|----------|
| 00 | `00_MASTER_PROMPT.md` | Rules, product scope, design system, UX, testing, acceptance (this file) |
| 01 | `01_CODE_PART1_BUILD_DOMAIN_PROTOCOL_BLUETOOTH.md` | Gradle, manifest, resources, domain models, protocol-profile engine, BLE + SPP transport, controller |
| 02 | `02_CODE_PART2_AUDIO_DATA_UI.md` | DSP, Hearing Assist, system EQ, relaxation, storage, ViewModel, MainActivity, unit tests |
| 02B | `02B_CODE_PART2B_UI.md` | Compose theme and reusable components |
| 02C | `02C_CODE_PART2C_SCREENS.md` | App shell, Home, Connect, Sound (modes/EQ/effects), Controls |
| 02D | `02D_CODE_PART2D_SCREENS.md` | Assist, Settings, Relaxation, Explorer, Protocol, Help, Diagnostics, Privacy |
| 03 | `03_ASSETS_AND_PROFILE.md` | Launcher icon, favicon, illustration, extra resources (shortcuts, notification icon), protocol profile JSON, scripts, Gradle wrapper properties, docs index |
| 04 | `04_CODE_PART4_V2_EXTRAS.md` | System monitor, EQ curve, welcome tour, About screen, extra unit tests |
| 05 | `05_DOCS_COMPLETE_TEXT.md` | Full verbatim text of every `docs/` file |

> **How to use in AI Studio:** paste/attach all ten files, and also attach 4–8 screenshots of the official RONiN STUDIO app (Home, EQ, Touch, Settings). Then send: *"Implement exactly what these files specify. Create every file listed. Copy the provided code verbatim, fix only real compile errors, and do not replace any real logic with placeholders."* Then follow the generation protocol in section 12.

---

## 1. Read this first — what is real and what is not

1. **This is a native Android app (Kotlin + Jetpack Compose).** A web page or WebView cannot do Bluetooth control or low-latency audio. If AI Studio's preview cannot run native Android, export the project and build with Android Studio. Do not convert it to React.
2. **The earbuds' control protocol is proprietary and not public.** Nobody (including this prompt) can honestly know the byte commands for ANC, EQ, touch, battery. Inventing them would make a dummy app, or worse, send random bytes to the earbuds.
3. **Solution: "Protocol Profile" engine.** The app contains a complete, generic engine (framing, checksums, templates, response parsing, confirmation logic, BLE and Bluetooth-Classic SPP transports). The device-specific bytes live in one JSON file (`eclipse_r7065.json`, file 03) which ships **with every command marked `"verified": false`**. A command is sent **only** when its `verified` flag is `true`. The user captures the real bytes from their own earbuds (guide in section 4) and imports the JSON from inside the app (Settings → Protocol profile). Nothing needs recompiling.
4. **Things that work with no protocol knowledge** (must work on day one): permissions, scanning, bonded-device list, GATT Explorer (read-only), standard BLE Battery Service read (if present), **Hearing Assist**, **system equalizer for players that announce an audio session**, relaxation sounds, channel test tones, local EQ profiles/presets, touch-mapping drafts, offline help, diagnostics, theme, import/export.
5. **Things that need the real bytes:** ANC/Transparency/Normal, per-earbud and case battery (unless standard service exists), hardware EQ, touch mapping on device, gaming mode, device rename, firmware info, reset. Until verified, the UI shows them as **disabled with a truthful reason** ("Protocol not verified — import a verified profile"), never as working.
6. **"All official features":** every official feature has a screen, an engine path, and a profile slot (see section 5 parity table). Whether it *controls hardware* depends on the profile. Gemini must never claim otherwise.

## 2. Non-negotiable rules for Gemini

1. Create real, complete files. No `TODO`, no `...`, no empty callbacks, no stub classes, no fake data, no hard-coded "Connected"/"100%".
2. Never show success for an unconfirmed device command. Distinguish **Pending → Confirmed / Sent-unconfirmed / Rejected / Unsupported**.
3. Never invent UUIDs, opcodes, checksums, battery values, firmware versions, preset names of the vendor, or reset sequences.
4. Never send any write to a device unless a profile command has `verified:true` **and** the target device identity matches the user's selected device.
5. **No `INTERNET` permission**, no analytics, ads, accounts, login, cloud, remote fonts/images, or dynamic code loading.
6. Hearing Assist: explicit Start only, never auto-start, no recording/saving/uploading audio, conservative gain, limiter always on, visible Stop, foreground-service notification, stops on call/route loss/focus loss/permission loss, never claims to be a medical hearing aid.
7. Do not copy the official app's logo, artwork, source code, or strings. Use the original assets in file 03. Package/application ID must **not** be `com.yscoco.ronin`; use `com.eclipsecompanion.app`.
8. Factory reset is a guarded action that stays disabled unless the profile marks `factoryReset` verified. Firmware update is shown as unavailable (no transfer code); never fake progress.
9. Keep copied code identical unless it fails to compile; if you change anything, list the change in `docs/CHANGES_FROM_PROMPT.md`.
10. Report honestly: say which commands (gradlew test/lint/assembleDebug) actually ran. If Gradle cannot run in your environment, say so and give the commands.

## 3. Target hardware and devices

- **Earbuds:** Ronin Eclipse, model R-7065, white. Public listings: Bluetooth 5.3, ANC + transparency, quad-mic call noise reduction (ENC), 10 mm drivers, charging case. No dual-device (multipoint) support, no water-resistance rating listed. Treat retailer data as secondary evidence only.
- **ANC ≠ ENC:** ANC reduces what the listener hears; ENC cleans the outgoing call microphone. Never merge the two in UI or help text.
- **Primary phone (design target):** Infinix, XOS 14.5 (Android 14, so API 34 behaviors apply: foreground-service type permissions, nearby-devices permission), 6.78" AMOLED, 20:9, 1080×2436 px (≈ 411×890 dp at ~2.6 density), punch-hole camera, rounded corners → handle status-bar/cutout insets with edge-to-edge.
- **Future tablet:** support ≥ 600 dp width with a navigation rail and two-column layouts (section 7.6).
- **minSdk 26, targetSdk/compileSdk 35.** Verify toolchain versions at generation time (AGP, Kotlin, Compose BOM). If a version in file 01 fails Gradle sync, bump to the nearest compatible stable release and note it.

## 4. Protocol discovery guide (put this verbatim in `docs/CAPTURE_PROTOCOL.md`)

Legitimate interoperability work on the user's **own** earbuds, using their **own** official app. Do not bypass encryption or authentication.

1. On the phone: Settings → Developer options → **Enable Bluetooth HCI snoop log** (full). Toggle Bluetooth off/on.
2. Open the **official app**, connect the Eclipse, and perform **one action at a time**, with a pause of ~5 s: ANC on → Transparency → Normal → each EQ preset → each touch change → gaming on/off, etc. Write the order down with timestamps.
3. Generate a bug report (Developer options → Take bug report) and extract `btsnoop_hci.log` from the zip (or use `adb pull /sdcard/btsnoop_hci.log` where available).
4. Open it in **Wireshark**. Filter `btatt` (BLE) or `btrfcomm` (Classic/SPP). Find **ATT Write Request / Write Command** (or RFCOMM data) packets that line up with each action. Note the service/characteristic handles, UUIDs (visible in earlier Service Discovery packets), and the byte payloads.
5. Compare payloads between actions to find: header bytes, length byte (yes/no), command id, parameter bytes, checksum type (none / sum8 / xor8 / crc16-modbus). Notifications from the earbuds (ATT Handle Value Notification) show acknowledgements and battery reports.
6. Use the app's **GATT Explorer** (read-only) to confirm UUIDs and properties. Fill the JSON profile, set `"verified": true` **only** for commands whose bytes you reproduced twice and whose result you observed on the earbuds.
7. Import the profile in Settings. Test each feature while watching the earbuds. Record results in `docs/PHYSICAL_DEVICE_TEST_RESULTS.md`.

Many Yscoco-style TWS earbuds use a BLE service for control, some use Bluetooth Classic SPP; the engine supports both (`transport.type` = `gatt` or `spp`). Do not assume which.

## 5. Feature inventory and official-app parity

Legend — **E** = engine + UI complete in code files; **P** = hardware control requires verified profile command; **L** = fully local, works now.

| Official-app feature | Our implementation | Needs profile? |
|---|---|---|
| Left/right battery | Battery cards, separate L/R/Case, "Last reported" timestamps, low-battery alerts; fallback to standard BLE Battery Service (single combined value, labeled so) | P (L/R/Case); standard service optional |
| ANC / Transparency / Normal | 3-card selector with pending/confirmed states, read-back confirmation, rollback | P |
| Custom EQ + presets | 10-band independent EQ, 18 presets (incl. Flat), custom profiles; applied to (a) earbuds if `setEq` verified, (b) system effects for supporting players, (c) preview locally | P for (a), L for (b)(c) |
| Touch controls | Left/Right × tap/double/triple/long, action list from profile, draft/apply/cancel, conflict check | P to apply; drafts L |
| Gaming Mode | Toggle shown only if verified | P |
| Rename device | Local alias (L) + firmware rename (P, `{name*}` token) | P for firmware |
| Meditation Mode | Phone-generated sounds (white/pink/brown/ocean/rain/fan), timer, breathing pacer | L |
| OTA update | Firmware status screen only (version if reported). Update transfer is intentionally NOT implemented: it needs the vendor spec and signed packages; never network | Blocked |
| Instant pairing popup | In-app "Compatible device nearby" card while app is open; manual scan always available | L |
| Manuals / product details | Offline help center + R-7065 info | L |
| Hearing Assist | Full engine (file 02) | L |
| Factory reset | Separate guarded action, only if verified | P |
| Gaming/low-latency, noise-cancelling level, transparency level, wear detection, voice prompts | Optional profile slots; each control appears only if its command is verified and (for levels) a real range is defined | P |
| Stable connection (1.4.4 notes: stay connected during calls) | Control link independent of call audio; call regression plan | L (test) |
| Support | Offline troubleshooting; optional copy-able contact text | L |

*Checked against the live listing on 30 Oct 2026 (v1.4.4): it advertises left/right battery, three modes, EQ with own presets, touch controls, Gaming Mode, rename, Meditation, OTA, instant pairing, manuals. It does **not** advertise case battery or Hearing Assist; those are user requests here and stay profile-gated or local respectively.*

### 5.1 Known weaknesses of the official app → fixes required here

| Problem (from public reviews / design analysis) | Fix |
|---|---|
| EQ sliders move neighbors together | Independent per-band state, regression tests (`CoreTests.kt` in file 02) |
| Requires sign-in/online features | None; fully offline |
| Shows requested ANC mode before the earbuds confirm | Confirmed-state-only UI with pending indicator |
| Battery shown stale/guessed, case value unclear | `Reported<T>` type: value + timestamp or "Not available" |
| Connection drops / stale callbacks / app-start crash | Generation-checked GATT callbacks, serialized queue, bounded retries |
| Reported call-mic distortion after updates | App never touches call routing; Hearing Assist auto-stops during calls; regression test plan |
| Support is only via external chat | Offline help center and diagnostics summary |
| No privacy transparency (listing: collects personal info and device IDs, unencrypted) | Privacy screen listing exactly what is stored; no network permission |
| Reviews (Sep 2026): app stuck on "Identifying Earbuds", never proceeds | Visible connection stages, 15 s step timeouts, **Cancel connecting** button, stuck-connection help entry |
| Sound changes after updates, no way back | Local profiles never overwrite the earbuds; Flat reset; "Last command sent" wording when no read-back |

### 5.2 Suggested extra features (all included in code)

1. **Hearing Assist** with presets (Conversation, TV, Quiet room), speech-clarity filter, rumble cut, limiter, feedback auto-stop, session timer + break reminder, safe-start every time.
2. **System Equalizer** via Android audio-effect session broadcast (works with players that announce sessions, e.g. many music players; **not** with apps that do not). Includes bass boost, virtualizer, loudness gain.
3. **18 EQ presets** + custom save/duplicate/rename/delete/import/export (JSON).
4. **Relaxation sounds + breathing pacer** (procedurally generated, no licensed assets).
5. **Channel test** (left/right tones at a safe fixed low level) and **Hearing comfort tips**.
6. **GATT Explorer** (read-only) and **Protocol profile import** for the real bytes.
7. **Battery alerts** (20%/10% user-configurable) while the app is open.
8. **Simple Mode** for family: Home shows only battery, three modes, Hearing Assist.
9. **Listening timer reminder** (local, in-app).
10. **Diagnostics report** (redacted, copyable) and offline help.
11. **Settings backup** to JSON (no identifiers, no audio).
12. Dark/light/system theme, reduced-motion respect, large-font support, TalkBack labels.
13. **Welcome tour** on first run (what is real, permission explanation, what needs a profile).
14. **26 EQ presets**, EQ **curve preview**, automatic **headroom** for the system EQ, clipping warning.
15. **Touch review dialog**, per-action descriptions, duplicate-action warning.
16. **"Compatible earbuds found"** card from the paired list (in-app pairing prompt), Bluetooth-off banner, separate display of Android audio route and app control link.
17. **Preferred listening mode** suggestion after connecting (never applied silently), optional **listening reminder**, optional haptic tick only after confirmed changes.
18. **Cancel connecting**, MTU request, battery refresh once a minute while connected and visible.
19. **Hearing Assist** extras: pre-start checklist, 5 presets (adds Restaurant, Outdoors), comfort compressor, graceful fade-out, buffer-size readout.
20. Relaxation: 8 sounds (adds Soft wind, Stream), 10 s end-of-timer fade-out.
21. **Launcher shortcuts** (Hearing Assist, Equalizer, Relax) and the music-player "Equalizer" button opens the EQ.
22. Protocol screen: save/copy a blank profile template; About and licences screen.

### 5.3 What "full control" and "improve transparency" can honestly mean

- **Transparency** is processed inside the earbuds. An app can only (a) switch it, and (b) set its level if the firmware exposes a level command. This app does (a) and (b) when verified. It never claims a phone-side slider improves the earbuds' microphones. For hearing speech better, **Hearing Assist** is a separate phone-microphone path.
- **ANC strength** likewise appears only if `setAncLevel` is verified with a real range.
- **"Same as the official app"**: layout language (hero earbuds, battery row, big mode cards, feature rows) is reproduced with original art; exact pixel matching needs the screenshots listed in the How-to-use note.

## 6. Privacy and data policy (put in `docs/PRIVACY.md`)

Stored locally only: theme, simple-mode flag, first-run flag, EQ profiles and current gains, touch drafts, alias, last selected device address and name, battery thresholds, system-effect settings, Hearing Assist non-gain preferences, reminder and haptic choices, preferred listening mode, imported protocol profile. Battery and mode readings are kept in memory only (shown as "Last reported HH:mm" while the app runs) and are not saved. **Never stored or sent:** audio, microphone buffers, credentials, location. No network permission exists in the manifest. "Clear app data" erases local settings only and never claims to reset the earbuds. "Forget device" removes the saved address only. "Reset earbuds" is a separate verified-only action.

## 7. Design system

The official app is a clean, product-centered layout: a large earbud image at the top, status/battery beneath, big mode buttons, then feature rows. Reproduce that **layout language** with original assets (do not copy artwork). Use the attached screenshots to match spacing and structure.

### 7.1 Colors (define once in `Theme.kt`; WCAG AA checked)

| Token | Light | Dark |
|---|---|---|
| background | `#F6F6F4` | `#121314` |
| surface | `#FFFFFF` | `#1B1C1E` |
| surfaceSecondary | `#EEEEEC` | `#25272A` |
| textPrimary | `#181818` | `#F3F3F0` |
| textSecondary | `#666663` | `#B7B8B5` |
| outline | `#D7D7D2` | `#424447` |
| accent | `#344C63` | `#B8CBDC` |
| accentContainer | `#E3EAF0` | `#283743` |
| success | `#2D6A4F` | `#82C9A5` |
| warning | `#9A6700` | `#F1C46A` |
| error | `#B3261E` | `#F2A09A` |

### 7.2 Typography and spacing

System font (no remote fonts). Title 26sp SemiBold; section 18sp SemiBold; body 15sp; label ≥ 12sp. Spacing scale 4/8/12/16/20/24/32 dp. Phone side padding 20 dp. Cards radius 24 dp, controls radius 16 dp, chips 12 dp, 1 dp outline border, elevation ≤ 2 dp. Minimum touch target 48 dp. Never rely on color alone.

### 7.3 Navigation

Bottom bar (compact width): **Home · Sound · Controls · Assist · Settings**, with text labels. Sub-screens (Connect, Explorer, Relaxation, Help, Diagnostics, Privacy) open from Home/Settings with a back arrow and `BackHandler`. Width ≥ 600 dp: `NavigationRail` + content max width 840 dp, EQ and battery in two columns.

### 7.4 Screen layouts (phone, portrait)

**Home** — top row: app name + connection pill ("Connected" / "Not connected" / "Connecting…"). Hero card: original earbuds+case illustration (Canvas), device alias, Connect/Manage button. Battery row: three equal cards L / R / Case, each showing `NN%` or "Not available" plus "Last reported 10:42" when stale. Mode selector: three large cards (ANC, Transparency, Normal) with icon + label; active one filled with `accentContainer` and a check; pending one shows a small progress ring; disabled ones show a reason line. Gaming switch only if verified. "Hearing Assist" shortcut card. Notice banners for permission/protocol/Bluetooth-off state; a line separating the Android audio route from the app control link; "Compatible earbuds found" card; "Switch back to your preferred mode?" card; "Cancel connecting" during a connection attempt; Wear detection / Voice prompts rows only if verified.

**Sound** — segmented tabs: *Modes* (mode selector + transparency level slider only if verified + explainer ANC/ENC/Transparency), *Equalizer* (preset chips scroll row, 10 vertical or horizontal sliders with dB value and ± steppers, "Flat" reset, custom profile menu, status lines "Saved on phone" / "Applied to earbuds" / "Applied to system effects"), *Effects* (bass boost, virtualizer, loudness, enable system EQ), *Relax* (opens Relaxation).

**Controls** — Left/Right tabs; rows for single, double, triple, long press; each opens a dropdown of actions; draft state; **Apply** and **Cancel**; honest banner if the device cannot be written yet ("Saved on this phone only").

**Assist** — safety text, device/route status, big Start/Stop button (Stop in `error` color while running), sliders (input gain, output level), speech clarity chips (Off/Mild/Moderate), rumble cut switch, limiter (on, locked), preset chips, level meter, session timer, break reminder.

**Settings** — Theme, Simple mode, Preferences (preferred mode, haptics, listening reminder, welcome tour), About and licences, app-permissions shortcut, Battery thresholds, Alias, Device rename (if verified), Firmware info, Protocol profile (import/reset/status), Backup/restore, Privacy, Help, Diagnostics, Forget device, Clear app data, About ("Independent project, not affiliated with Ronin").

### 7.5 Motion

120–220 ms transitions; no flashing; skip animations if `Settings.Global.ANIMATOR_DURATION_SCALE == 0`; never animate to suggest a hardware state change before confirmation.

### 7.6 Responsiveness checklist

Test at: 411×890 dp (Infinix), 360×640 dp small, 600×960 dp, 840×1200 dp tablet portrait, landscape of each, font scale 0.85–2.0, display size small/large, split-screen. Use `WindowInsets` (edge-to-edge), `verticalScroll`/`LazyColumn` for every screen, no fixed heights for text containers, `Modifier.widthIn(max = 840.dp)` on tablets.

### 7.7 Accessibility

`contentDescription` for every icon button; `semantics { stateDescription }` for mode cards and sliders; TalkBack order top→bottom; high-contrast tokens; haptics only after confirmed actions.

## 8. Project structure (create exactly)

```
EclipseCompanion/
├── settings.gradle.kts, build.gradle.kts, gradle.properties
├── app/build.gradle.kts, app/proguard-rules.pro
├── app/src/main/AndroidManifest.xml
├── app/src/main/assets/protocol/eclipse_r7065.json
├── app/src/main/java/com/eclipsecompanion/app/
│   ├── MainActivity.kt, AppViewModel.kt
│   ├── domain/Models.kt
│   ├── protocol/ProtocolProfile.kt, FrameCodec.kt
│   ├── bluetooth/Transports.kt, EarbudController.kt, Scanner.kt, SystemMonitor.kt
│   ├── data/SettingsStore.kt
│   ├── audio/Dsp.kt, HearingAssist.kt, SystemEffects.kt, Relaxation.kt
│   └── ui/Theme.kt, Components.kt, Screens.kt, Screens2.kt, Extras.kt   (files 02B, 02C, 02D, 04)
├── app/src/main/res/ (values, values-night, drawable, mipmap-anydpi-v26, xml/shortcuts.xml, xml/data_extraction_rules.xml)
├── app/src/test/java/com/eclipsecompanion/app/CoreTests.kt, ExtraTests.kt
├── gradle/wrapper/gradle-wrapper.properties, .gitignore
├── assets/favicon.svg, assets/eclipse-illustration.svg
├── docs/ (README, CAPTURE_PROTOCOL, PROFILE_FORMAT, DEVICE_CAPABILITY_MATRIX, OFFICIAL_FEATURE_PARITY,
│         SOURCE_AND_ASSUMPTION_REGISTER, PRIVACY, SECURITY_REVIEW, TEST_PLAN, TROUBLESHOOTING, KNOWN_LIMITATIONS,
│         AUDIO_ROUTING_AND_HEARING_ASSIST, AUDIO_SETTINGS_SCOPE, DEPENDENCY_AND_LICENSE_INVENTORY,
│         RELEASE_CHECKLIST, PHYSICAL_DEVICE_TEST_RESULTS, CHANGES_FROM_PROMPT)
└── scripts/check_no_network.sh, audit_sources.sh
```

Docs: full text is in file 05. `DEVICE_CAPABILITY_MATRIX.md` uses the statuses **Works now / Blocked: needs verified profile / Not implemented** plus a separate "Physically verified" column that stays **No** until the user records a real test. Update it from the profile actually shipped.

## 9. Permissions

- `BLUETOOTH_SCAN` (`neverForLocation`), `BLUETOOTH_CONNECT` (API 31+); legacy `BLUETOOTH`/`BLUETOOTH_ADMIN` with `maxSdkVersion=30`; `ACCESS_FINE_LOCATION` with `maxSdkVersion=30` only.
- `RECORD_AUDIO` requested only on tapping Start in Hearing Assist.
- `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MICROPHONE` (service `foregroundServiceType="microphone"`), `POST_NOTIFICATIONS` (API 33+, requested at Start).
- `MODIFY_AUDIO_SETTINGS` for audio effects. Nothing else. No `INTERNET`, no `VIBRATE` (haptics use the view system's feedback), no location on Android 12+.

## 10. Testing and acceptance

Unit tests (`CoreTests.kt` in file 02, `ExtraTests.kt` in file 04, 30 tests in total): frame build/parse, checksums incl. CRC round trip, ASCII tokens, battery validation, unknown ≠ 0, EQ band independence (repeated edits), 26 presets, headroom, EQ interpolation, limiter bound, comfort compressor, gain clamp, profile validation (ranges, UUID, hex, size, new commands), conservative compatibility wording, touch duplicate warning. Run `./gradlew test lint assembleDebug`.

Physical checklist (record real results): airplane-mode launch; permission grant/deny; scan/cancel; bonded list; Explorer reads services; Hearing Assist start/stop, call interruption, route loss, screen lock; system EQ with a supporting player; relaxation timer; rotate/recreate during scan; font 2.0; dark mode; TalkBack; profile import with malformed JSON rejected; no `INTERNET` in merged manifest (`scripts/check_no_network.sh`).

Acceptance: app builds; zero dead buttons (each works, is disabled with reason, or is absent); no fabricated hardware state; offline; Hearing Assist safe; docs generated.

## 11. Final report format Gemini must produce

1. Files created (tree). 2. Commands actually run and results. 3. Feature table: *UI done / Engine done / Needs verified profile / Physically verified (No until user tests)*. 4. Blocked features and the exact evidence needed (bytes captured per section 4). 5. Any deviations from files 01–03 (including 02B/02C/02D).

## 12. Generation protocol for Gemini (follow exactly)

1. Read all ten files first. List the files you will create (use section 8) before writing any.
2. Create files in this order: Gradle + manifest + resources (01, 03) → domain/protocol/bluetooth (01) → audio/data/ViewModel/MainActivity (02) → UI (02B, 02C, 02D, 04) → tests (02, 04) → assets/scripts (03) → docs (05).
3. If your output limit is reached, stop after a complete file, say exactly which files are done and which remain, and wait for "continue". Never shorten code with `...`, "rest unchanged", or comments instead of code.
4. After each group, re-open the files you wrote and check: package line, imports, every function referenced elsewhere exists with the same signature (for example `SystemMonitor`, `EqCurve`, `OnboardingScreen`, `AboutScreen`, `compatibilityLabel`, `duplicateActions`).
5. Do not refactor, rename or "improve" the provided code. The only allowed edits are real compile fixes, each logged in `docs/CHANGES_FROM_PROMPT.md`.
6. If a library version fails Gradle sync, bump it to the nearest compatible stable release and log it in the dependency inventory too.

## 13. Final self-check (answer yes/no for each in the final report)

- Every path in section 8 exists; no `TODO`, `...`, empty click handlers, or placeholder text.
- The merged manifest has no `INTERNET`; both scripts pass (or you said you could not run them).
- Every button works, is disabled with a visible reason, or is absent.
- No hard-coded battery, mode, firmware or connection state; nothing shown as confirmed without a device acknowledgement.
- The shipped profile has no verified command; the capability matrix says so.
- Hearing Assist starts only from the Start button after the checklist, and every stop path in `docs/AUDIO_ROUTING_AND_HEARING_ASSIST.md` is implemented.
- Docs in `docs/` match the code (Help Q and A equals TROUBLESHOOTING.md; test list equals the real tests).
- You stated honestly whether `./gradlew test lint assembleDebug` was actually run.
