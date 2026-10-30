# File 05 — Complete text of every `docs/` file (v3.0)

Create each file below at the exact path, copying the content **verbatim** (the outer four-backtick fence is not part of the file).. Do not fill in result tables: they are for the user's own physical tests. Three more docs (`CAPTURE_PROTOCOL.md`, `PRIVACY.md`, `PROFILE_FORMAT.md`) come from the master prompt and file 03.

---

### `docs/README.md`
````markdown
# Eclipse Companion

An independent, fully offline Android companion app made for the **Ronin Eclipse R-7065** earbuds. No account, no sign-in, no ads, no analytics, **no INTERNET permission**.

> Independent project. **Not affiliated with or endorsed by Ronin.** Hearing Assist is an assistive listening tool, not a medical hearing aid.

## What works on day one
Hearing Assist (phone microphone to earbuds), system equalizer for music players that announce an audio session, 26 EQ presets plus your own profiles with an EQ curve preview, relaxation sounds with timer and breathing pacer, left/right channel test, touch-control drafts, welcome tour, Simple mode for family, launcher shortcuts, offline help, diagnostics, backup and restore.

## What needs a verified protocol profile
ANC / Transparency / Normal switching, left / right / case battery, earbud EQ, touch gestures on the earbuds, Gaming Mode, noise-cancelling and transparency level, wear detection, voice prompts, rename and factory reset.
**Hardware controls need a verified protocol profile; see CAPTURE_PROTOCOL.md.** The earbuds' control commands are not public, so the app never guesses them.

## Build
Requirements: Android Studio (current stable), JDK 17, Android SDK 35.

    gradle wrapper --gradle-version 8.9     # once, creates gradlew
    ./gradlew test lint assembleDebug
    bash scripts/check_no_network.sh
    bash scripts/audit_sources.sh

The first build needs internet on your computer to download Gradle dependencies. The finished app never uses the internet. After one sync you can build with `./gradlew --offline assembleDebug`.

## Install
1. On the phone enable Developer options and USB debugging.
2. `adb install -r app/build/outputs/apk/debug/app-debug.apk` (or press Run in Android Studio).
3. Open the app, follow the welcome tour, allow **Nearby devices** when asked.
4. Open the earbuds' case, tap **Connect earbuds** on Home.

## Unlocking hardware controls
Follow `CAPTURE_PROTOCOL.md`, fill the template from Settings > Protocol profile > Save blank template, mark only commands you reproduced twice as `verified: true`, import it, reconnect, and record results in `PHYSICAL_DEVICE_TEST_RESULTS.md`.

## Documents
DEVICE_CAPABILITY_MATRIX, OFFICIAL_FEATURE_PARITY, SOURCE_AND_ASSUMPTION_REGISTER, PROFILE_FORMAT, CAPTURE_PROTOCOL, PRIVACY, SECURITY_REVIEW, TEST_PLAN, PHYSICAL_DEVICE_TEST_RESULTS, TROUBLESHOOTING, KNOWN_LIMITATIONS, AUDIO_ROUTING_AND_HEARING_ASSIST, AUDIO_SETTINGS_SCOPE, DEPENDENCY_AND_LICENSE_INVENTORY, RELEASE_CHECKLIST, CHANGES_FROM_PROMPT.
````

### `docs/DEVICE_CAPABILITY_MATRIX.md`
````markdown
# Device capability matrix

Status values: **Works now** = implemented and local, no protocol needed. **Blocked: needs verified profile** = engine and UI exist, hardware write/read stays off until the profile marks the command `verified: true`. **Not implemented** = deliberately absent.
"Physically verified" is **No** for everything until you record a real test in `PHYSICAL_DEVICE_TEST_RESULTS.md`. Update this table from the profile actually in use.

| Feature | Status | Evidence | Physically verified |
|---|---|---|---|
| Scan, bonded list, connect | Works now | Standard Android Bluetooth APIs; generation-checked GATT; cancel button | No |
| ANC / Transparency / Normal | Blocked: needs verified profile | `setMode` (+ `readMode`) unverified in shipped profile | No |
| Battery left / right / case | Blocked: needs verified profile | `readBattery` + `parse.battery` unverified | No |
| Battery, single standard value | Works now (if earbuds expose BLE Battery Service) | Read-only standard characteristic; labelled "combined" | No |
| Earbud (hardware) EQ | Blocked: needs verified profile | `setEq` + `eq` layout unverified | No |
| Touch controls on earbuds | Blocked: needs verified profile | `setTouch` + `touch` maps unverified; drafts saved locally | No |
| Gaming Mode | Blocked: needs verified profile | `setGaming` unverified; not assumed for Eclipse | No |
| Noise-cancelling level | Blocked: needs verified profile | `setAncLevel` + `ancLevel` range unverified | No |
| Transparency level | Blocked: needs verified profile | `setTransparencyLevel` + range unverified | No |
| Wear detection / voice prompts | Blocked: needs verified profile | `setWearDetection`, `setVoicePrompts` unverified | No |
| Rename earbuds (firmware) | Blocked: needs verified profile | `setName` unverified. Local alias works now | No |
| Firmware version display | Blocked: needs verified profile | `readFirmware` unverified | No |
| Firmware update | Not implemented | Needs vendor package, signature scheme and protocol | No |
| Factory reset | Blocked: needs verified profile | `factoryReset` unverified; guarded dialog | No |
| Hearing Assist | Works now | Local audio engine; unit tests for limiter/compressor/gains | No |
| System equalizer + bass/stereo/loudness | Works now (supporting players only) | Android audio-effect sessions | No |
| Relaxation sounds + breathing pacer | Works now | Procedural audio, no assets | No |
| Channel test tones | Works now | Fixed low-level tones | No |
| Local EQ presets and profiles | Works now | 26 presets, independence tests | No |
| Welcome tour, Simple mode, shortcuts, diagnostics, backup | Works now | Local only | No |
````

### `docs/OFFICIAL_FEATURE_PARITY.md`
````markdown
# Official feature parity

Official app: RONiN STUDIO, `com.yscoco.ronin`. Listing checked **30 Oct 2026** (version 1.4.4, updated 9 Oct 2026, 3.7 stars, 870 reviews). The listing says the app works only with the newer "software-based" Ronin earbuds. Confirm your Eclipse is one of them (box says so; the official app must be able to identify it).

| Feature | Official listing says | Eclipse-specific evidence | Implementation type | Our implementation | Final status |
|---|---|---|---|---|---|
| Battery, left and right | Yes ("real-time") | Not verified | Device protocol | Separate L/R cards, last-reported time | Blocked: profile |
| Charging-case battery | **Not advertised** | Not verified | Device protocol | Case card shows Not available unless reported | Blocked: profile |
| ANC / Transparency / Normal | Yes | Product listing confirms modes; commands unverified | Device protocol | Confirmed-state-only selector | Blocked: profile |
| Custom EQ and presets | Yes ("create your own presets") | EQ layout unknown | Protocol + local | 10 independent bands, 26 presets, custom profiles, curve preview | Local works; earbud write blocked |
| Touch-control customization | Yes | Gesture/action matrix unknown | Device protocol | Drafts, conflict warning, review dialog, apply | Drafts work; apply blocked |
| Gaming Mode (low latency) | Yes (generic listing) | Not assumed for Eclipse | Device protocol | Controls hidden unless verified | Blocked: profile |
| Rename earbuds | Yes | Storage target unknown | Protocol + local alias | Local alias now; firmware rename if verified | Alias works; firmware blocked |
| Meditation Mode | Yes ("calming sounds, one tap") | Unknown whether phone or earbuds play it | Local audio | Procedural sounds (8 types), timer with fade-out, pacer | Works (phone-side) |
| OTA software updates | Yes | Package, signing, transport unknown | Device protocol | Not implemented, honestly stated | Not implemented |
| Instant pop-up pairing | Yes | Android pop-up not reproducible | Android UI | In-app "Compatible earbuds found" card from paired list; manual scan | Works (in-app only) |
| Manuals, details | Yes | Manual not bundled | Local docs | Offline help, About, product notes | Works |
| Support | Email in listing | n/a | Local help | Offline troubleshooting and diagnostics copy | Works |
| Hearing Assist | **Not in current listing text** (earlier notes said it was) | Phone mic path | Local audio | Full engine | Works (user request) |
| Stay connected during calls (1.4.4) | Release note | Needs physical test | Android behaviour | App never touches call routing; test plan covers it | Test pending |
| Touch settings save after calls (1.4.4) | Release note | Needs physical test | Device protocol | Test plan item | Test pending |
| Sign-in / cloud | Listing's Data safety: may collect personal info and device IDs, not encrypted | n/a | n/a | None: offline, no network permission | Improvement |

Improvements over the official app, from public reviews: independent EQ bands (regression-tested); confirmed-state-only mode display; a cancel button and clear stage messages instead of an endless "Identifying Earbuds" screen; honest "Not available" battery; no call-audio interference; privacy screen.
````

### `docs/SOURCE_AND_ASSUMPTION_REGISTER.md`
````markdown
# Source and assumption register

| Claim | Source | Type | Date checked | Confidence | Effect on implementation | Safe to show user |
|---|---|---|---|---|---|---|
| Official app feature list and v1.4.4 notes | https://play.google.com/store/apps/details?id=com.yscoco.ronin | Official | 30 Oct 2026 | High | Parity table | Yes |
| Official app collects personal info and device IDs, data not encrypted | Same page, Data safety | Official | 30 Oct 2026 | High | Privacy comparison | Yes |
| Official app compatible only with new software-based models | Same page | Official | 30 Oct 2026 | High | Compatibility warning in onboarding/help | Yes |
| Eclipse R-7065: Bluetooth 5.3, ANC, transparency, quad-mic ENC, 10 mm, case | Ronin product page and retailer listings | Official + retailer | Earlier research; re-check | Medium | Help text only; no behaviour depends on it | Yes, labelled secondary |
| No multipoint, no water-resistance rating listed | Retailer/official listing | Retailer | Earlier research; re-check | Medium | Not advertised in app | Yes |
| Reviews: "Identifying Earbuds" stuck screen | Play reviews, Sep 2026 | Observed | 30 Oct 2026 | Medium | Cancel button, stage text, help entry | Yes |
| Reviews: EQ sliders move neighbours; call-mic distortion after updates | Public reviews | Observed | Earlier research | Medium | EQ independence tests; call regression plan | Yes |
| Ronin support told a **Mist Pro** owner: disconnect, tap both earbuds 5 times to reset | Play review reply, 11 Sep 2026 | Observed (other model) | 30 Oct 2026 | Low for Eclipse | Mentioned in Help with warning only; app never sends or simulates it | Yes, with caveat |
| Control transport is BLE GATT or Bluetooth Classic SPP | None | **Assumption** | n/a | Unknown | Both transports supported; nothing hard-coded | n/a |
| Case battery is readable by phone | None | **Assumption** | n/a | Unknown | Shown only if reported | n/a |
| Eclipse supports Gaming Mode / level controls / wear detection | None | **Assumption** | n/a | Unknown | Hidden unless verified | n/a |
| Android 14 requires FGS microphone permission/type | developer.android.com foreground-service types | Official docs | Re-check at release | High | Manifest + service type | n/a |
````

### `docs/SECURITY_REVIEW.md`
````markdown
# Security review

## Data inventory
Stored locally in DataStore: theme, Simple mode, first-run flag, alias, last device address and name, imported protocol profile, EQ profiles and current gains, touch drafts, battery thresholds, system-effect settings, Hearing Assist non-gain preferences, break/listening reminders, preferred mode, haptics. Never stored or sent: audio, microphone buffers, location, credentials. Battery/mode readings stay in memory. `allowBackup=false` and `data_extraction_rules.xml` exclude everything from backups and device transfer.

## Permission justification
| Permission | Why |
|---|---|
| BLUETOOTH_SCAN (neverForLocation), BLUETOOTH_CONNECT | Find and talk to the earbuds (API 31+) |
| BLUETOOTH, BLUETOOTH_ADMIN, ACCESS_FINE_LOCATION (max SDK 30) | Same, on Android 8-11 only |
| RECORD_AUDIO | Hearing Assist, requested only on Start |
| FOREGROUND_SERVICE, FOREGROUND_SERVICE_MICROPHONE, POST_NOTIFICATIONS | Mandatory visible notification while the microphone is in use |
| MODIFY_AUDIO_SETTINGS | System equalizer effects |
| (none) INTERNET | Absent by design; `scripts/check_no_network.sh` and `scripts/audit_sources.sh` enforce it |

## Threat model
- **Wrong or spoofed device**: connection only to the address the user picks; name match is shown as confidence, never auto-connect to scan results; auto-reconnect only to the saved address.
- **Malicious or malformed profile**: size cap 100,000 characters, UUID and hex validation, ranges 0-255, allow-listed command names, unknown schema rejected, a verified command must have a template. Importing never writes to the earbuds.
- **Unverified writes**: every write passes `can()` and the command's `verified` flag; frames are built only when every token resolves; one command in flight; ack timeout never reports success.
- **Microphone misuse**: explicit Start, foreground notification, no storage, auto-stop on call, focus loss, route loss, permission loss.
- **Intent abuse**: shortcut `open` extra is validated against a fixed set. The audio-effect receiver is exported (required by Android) and only opens/closes effect sessions by id.
- **Log leakage**: addresses redacted to last bytes; no raw audio; GATT Explorer is read-only and local.

## Other controls
Command allow-list (`ProtocolProfile.ALLOWED_COMMANDS`). No dynamic code loading, no remote config, no cleartext traffic. Firmware authenticity: not applicable because update is not implemented. Release signing: keep the keystore outside the repo, use Play App Signing or a locally stored key, never commit `keystore.properties`.

## Residual risk
A user can mark a wrong command `verified: true`. Mitigation: guide requires two reproductions, confirmation dialogs for reset, and no experimental commands in the app.
````

### `docs/TEST_PLAN.md`
````markdown
# Test plan

## Automated unit tests (`./gradlew test`)
- `frameBuildsWithLengthAndChecksum`
- `frameParseRoundTripAndRejectsCorruption`
- `missingOrOutOfRangeTokenBuildsNothing`
- `crc16ModbusKnownVector`
- `xorChecksum`
- `batteryUnknownIsNotZero`
- `batteryRangeValidation`
- `mergeKeepsKnownValueWhenNewIsUnavailable`
- `eqBandsAreIndependent`
- `presetsAreWellFormed`
- `eqInterpolationHitsBandsAndClamps`
- `limiterNeverReachesFullScale`
- `hearingGainsAreClamped`
- `profileRejectsUnknownCommandAndBadVerifiedEntry`
- `profileAcceptsMinimalAndOnlyVerifiedCommandsCount`
- `movingOneBandRepeatedlyNeverTouchesOthers`
- `sanitizeRejectsWrongSizeAndNaN`
- `headroomEqualsMinusLargestBoost`
- `presetIdsAndNamesAreUniqueAndFlatIsFirst`
- `duplicateActionsIgnoreDisabledAndOtherSide`
- `compatibilityLabelIsConservative`
- `asciiTokenInsertsTextAndRejectsNonAscii`
- `crcFrameRoundTrips`
- `lengthByteFramesOver255BytesAreRefused`
- `hexParserHandlesSeparatorsAndRejectsOddLength`
- `profileAcceptsNewOptionalCommandsAndRanges`
- `profileRejectsBadRangesUuidsAndHex`
- `oversizedProfileIsRejected`
- `bundledTemplateShapeHasNothingVerified`
- `compressorLeavesQuietSignalAndTamesLoudOne`
Also in CoreTests: `presetsAreWellFormed` expects 26 presets.

## Physical checklist (tick only after really doing it)
- [ ] Fresh install in airplane mode: app opens, welcome tour shows
- [ ] Permission grant, deny, and re-grant from Android settings
- [ ] Scan, cancel scan, bonded list, Home "Compatible earbuds found" card
- [ ] Cancel while connecting leaves no stuck state
- [ ] GATT Explorer lists services (read-only)
- [ ] Hearing Assist: start/stop, checklist dialog, fade-in, graceful stop, notification Stop button
- [ ] Hearing Assist stops on incoming call, route loss, screen lock behaviour noted, audio focus loss, permission revoke
- [ ] System EQ with a supporting player; bass/stereo/loudness; headroom switch
- [ ] Relaxation sounds, timer fade-out, breathing pacer with reduced motion
- [ ] Rotate during scan; font scale 2.0; dark mode; TalkBack pass; tablet width
- [ ] Malformed profile JSON rejected; template save/copy works
- [ ] Launcher shortcuts (assist, equalizer, relax); music-player "Equalizer" button opens Equalizer
- [ ] `scripts/check_no_network.sh` and `scripts/audit_sources.sh` pass

## Call and microphone regression plan (physical Eclipse)
Incoming and outgoing calls; one versus both earbuds; music before call and resume after; ANC/Transparency change during a call if supported; Hearing Assist stopped before call and refuses to start during a call; route changes; media versus call volume; left/right balance; distortion, dropouts, echo, feedback, distant mic pickup; repeated connect/disconnect during a call; EQ change then call; reconnect then call. Confirm the app's control link does not drop during calls or the voice assistant. Record redacted diagnostics for any defect. Never reset the earbuds as a fix.

## QA matrix (record expected and actual)
| Scenario | Expected | Actual |
|---|---|---|
| Fresh install offline | Opens, no network errors | |
| No earbuds paired | Clear "not connected"; Assist disabled with reason | |
| Paired but protocol unverified | Audio works; controls locked with reason | |
| Wrong Ronin model nearby | Listed as name match only; user must choose | |
| Two similar devices | User chooses; no auto-connect | |
| Earbuds in case / one removed | Honest state; Assist stops on route loss | |
| Case battery not reported | "Not available" | |
| Bluetooth off mid-command | Command rejected, nothing marked applied | |
| Permission revoked mid-scan / during Assist | Scan stops with message / Assist stops | |
| Rapid mode taps, duplicate Apply | One command in flight, others rejected | |
| Malformed response, GATT ok but app rejects | No success shown | |
| App killed during pending command | Clean state on restart | |
| Stale callback after reconnect | Ignored (generation check) | |
| Call during Assist, focus loss, route switch | Assist stops | |
| Extreme font scale, TalkBack, theme switch | Nothing clipped; labels read | |
| EQ independent-band assertions, import malformed EQ file | Pass / rejected | |
| Clear data vs forget device vs earbud reset | Three different outcomes | |
| Repeated open/close, process recreation | No crash | |

## Performance targets (record measured values, never invent)
No main-thread Bluetooth or audio work; no unbounded polling (battery refresh is once a minute, foreground and connected only); no ANR during 20 connect/disconnect cycles; Assist start/stop clean on the test phone.
````

### `docs/TROUBLESHOOTING.md`
````markdown
# Troubleshooting

The same questions and answers as the in-app Help screen.

## Earbuds do not connect
Open the case, keep the earbuds close, and make sure Bluetooth is on. If they are paired in Android settings, tap them under “Paired in Android”. Remove and re-pair if they were used with another phone.

## ANC or Transparency button is greyed out
Hardware controls need a verified protocol profile. See Settings > Protocol profile. Until then the earbuds can still be switched with their own touch controls.

## Battery shows Not available
Some earbuds do not report the case battery or per-earbud values over a standard channel. The app never guesses. A verified profile can enable them.

## Hearing Assist squeals
Move the phone away from the earbuds, lower the Earbud volume, and turn Speech clarity down. Hearing Assist stops itself if it detects feedback.

## Hearing Assist for family
Turn on Simple mode in Settings. Then Home and Hearing Assist are the main screens. Start quietly, pick the Conversation preset, and raise the volume slowly. Take breaks.

## Equalizer does nothing in some apps
The system equalizer works with players that announce their audio session. Some streaming and video apps do not. Use Apply to earbuds when the profile supports it.

## About your earbuds
Ronin Eclipse R-7065: Bluetooth 5.3, active noise cancelling, transparency mode, quad-mic call noise reduction. Call noise reduction (ENC) only affects your voice for the other person.

## Factory reset of the earbuds
Use the reset procedure in the manual that came with the earbuds. This app only offers a reset when a verified profile supports it. Ronin support has told owners of other models to disconnect and tap both earbuds five times; that is not confirmed for the Eclipse R-7065, so check your printed manual first.

## The app seems stuck on connecting
Tap Cancel, put the earbuds in the case for a few seconds, take them out, and try again. Make sure Nearby devices permission is allowed. If the earbuds play music but the app cannot control them, the control protocol may not be verified yet (Settings > Protocol profile).

## What the equalizer changes
The earbud EQ changes the earbuds themselves, only with a verified profile. The system equalizer changes music players that announce an audio session. Neither changes phone calls, and neither works in apps that do not announce a session.

## Phone calls and Hearing Assist
Hearing Assist stops by itself when a call starts. The app never changes call microphone routing or the earbuds’ call noise reduction (ENC).

## Why is the case battery missing?
Many earbuds do not report the case level to the phone, or only when the lid is open. The app shows Not available instead of guessing.

## Firmware updates
Updating earbud firmware is not available in this app. It would need the maker’s official package and update protocol. Use the maker’s own app for updates.

## Reset, forget and clear are different
Reset earbuds changes the earbuds (only with a verified profile). Forget saved earbuds removes the saved address from this app. Clear app data erases this app’s settings. None of these is a substitute for another.

## Bluetooth permission
Android calls it Nearby devices. The app needs it to find and talk to your earbuds. It does not use your location and has no internet access.
````

### `docs/KNOWN_LIMITATIONS.md`
````markdown
# Known limitations

- The earbuds' control protocol is unknown until you capture it; every hardware control is locked until a command is marked verified.
- System EQ works only for players that announce an audio session.
- Hearing Assist latency depends on the phone and Bluetooth codec and may be noticeable; speech can lag behind lips. It uses the phone microphone, not the earbuds' microphones.
- No firmware update. No multi-device or multipoint support. One saved earbud pair.
- Case and per-earbud battery depend on the profile; charging state is never inferred.
- Relaxation sounds run inside the app process (no foreground service); Android may stop them if it kills the app.
- The EQ curve preview is a drawing of your settings, not a measurement.
- Battery and mode readings are not saved between launches.
- English only. Urdu localization is not included because it needs a complete, reviewed translation of every string.
- No home-screen widget or Quick Settings tile: they could not show truthful live state.
- Profile validation checks structure, not whether the bytes are right. Only physical testing proves that.
- The code has not been compiled by the author of these prompts; the first Gradle sync and `./gradlew test lint assembleDebug` are the real check.
````

### `docs/AUDIO_ROUTING_AND_HEARING_ASSIST.md`
````markdown
# Audio routing and Hearing Assist

## Pipeline
Phone microphone (`AudioSource.MIC`, mono 16-bit, 48 kHz, else 44.1 or 16 kHz, 20 ms frames, urgent-audio thread) → input gain 0.5-4.0 × → rumble high-pass 120 Hz (optional) → speech clarity (peaking 2.5 kHz +3 or +6 dB, high shelf 4 kHz +3 dB) → comfort compressor (threshold 0.25, 2.5:1, attack 5 ms, release 120 ms) → output gain (start 0.15, max 0.6) × 1 s fade-in → soft limiter (knee 0.8, never reaches full scale) → `AudioTrack` with preferred device = Bluetooth A2DP/BLE headset.

## Stop conditions
| Cause | Behaviour |
|---|---|
| User Stop / notification Stop | ~160 ms fade-out, then stop |
| Call or communication mode (checked every ~0.5 s) | Immediate stop |
| Audio focus lost | Immediate stop |
| Earbud output removed | Immediate stop |
| Possible feedback (over 25% of samples above 0.8 for about 1.2 s) | Immediate stop with advice |
| Microphone stops, output write error, permission revoked | Immediate stop |
No automatic restart. Every new session resets output to the safe start level.

## Latency
The UI shows only the buffer size the phone allocated. Bluetooth codecs add more. Measure real delay with a clap test recorded by a second phone and write the result in `PHYSICAL_DEVICE_TEST_RESULTS.md`. Never claim zero latency.

## Not touched
Call routing, SCO, the earbuds' own microphones, and call noise reduction (ENC).
````

### `docs/AUDIO_SETTINGS_SCOPE.md`
````markdown
# Audio settings scope: what changes what

| Setting | Changes | Does not change |
|---|---|---|
| Earbud EQ / modes / levels (verified profile) | The earbuds' own processing, for all audio they play | Anything if the command is unverified |
| System equalizer, bass, stereo, loudness | Music players that announce an audio session | Streaming/video apps that do not; calls; the earbuds' firmware |
| Relaxation sounds | Only audio played by this app | Other apps |
| Hearing Assist | Its own live phone-mic pipeline | Music, calls, firmware |
| Channel test | Its own tone | Anything else |
| Android media volume | Phone volume (Android controls it) | Not an app feature |
| Call microphone / ENC | Never touched | n/a |
````

### `docs/DEPENDENCY_AND_LICENSE_INVENTORY.md`
````markdown
# Dependency and licence inventory

| Component | Version | Licence | Used for | Network |
|---|---|---|---|---|
| Android Gradle Plugin | 8.7.3 | Apache-2.0 | Build | Build time only |
| Kotlin + Compose compiler plugin | 2.0.21 | Apache-2.0 | Language | No |
| Compose BOM (ui, foundation, material3, icons-extended) | 2024.10.01 | Apache-2.0 | UI | No |
| androidx.core:core-ktx | 1.15.0 | Apache-2.0 | Helpers | No |
| androidx.activity:activity-compose | 1.9.3 | Apache-2.0 | Activity | No |
| androidx.lifecycle (viewmodel-compose, runtime-compose) | 2.8.7 | Apache-2.0 | State | No |
| androidx.datastore:datastore-preferences | 1.1.1 | Apache-2.0 | Local settings | No |
| kotlinx-coroutines-android | 1.9.0 | Apache-2.0 | Async | No |
| Test only: junit 4.13.2 (EPL-1.0), org.json 20240303 (JSON), kotlinx-coroutines-test 1.9.0 | | | Unit tests | No |

No analytics, crash-reporting, ad or networking library. No native audio library. Verify after every sync (`./gradlew :app:dependencies`). Optional: `./gradlew --write-verification-metadata sha256 help` to pin checksums. Do not claim reproducible builds unless you verified them. If versions are bumped to fix Gradle sync, update this table and CHANGES_FROM_PROMPT.md.
````

### `docs/RELEASE_CHECKLIST.md`
````markdown
# Release checklist

- [ ] Application ID is `com.eclipsecompanion.app`, never `com.yscoco.ronin`
- [ ] Original name, icon, screenshots; independent-project disclaimer in the listing
- [ ] Only needed permissions in the merged manifest; no INTERNET (`check_no_network.sh`, `audit_sources.sh`)
- [ ] `./gradlew test lint assembleRelease` clean; release build opens, Assist start/stop works with minification
- [ ] Target SDK and foreground-service rules re-checked for the current Android release
- [ ] Physical tests recorded (call regression, Assist, EQ, permissions, TalkBack, font 2.0)
- [ ] Capability matrix matches the shipped profile; no unverified claim in text
- [ ] Privacy text matches the code; Play data-safety form says no data collected
- [ ] No medical claims; Hearing Assist wording checked
- [ ] No firmware or vendor files bundled
- [ ] Version name/code bumped; keystore stored safely and not committed
````

### `docs/PHYSICAL_DEVICE_TEST_RESULTS.md`
````markdown
# Physical device test results

| Date | Phone / Android | Test | Result | Notes |
|---|---|---|---|---|
| | | | | |
````

### `docs/CHANGES_FROM_PROMPT.md`
````markdown
# Changes from the provided prompt files

None yet.

Gemini: list every deviation here (file, line, reason), including any dependency version you had to bump.
````

