# 06 — Gap analysis and change log (v2.0 → v3.0 → v4.0)

For you, the human. Do **not** attach this file to Gemini. Also do not attach the old "recovered" long spec; files 00–05 and 07 replace it.

## How I checked (v4.0)
I read all 10 documents in full and compared them with your 4 screenshots of the installed official app (Devices, device page, Audio Presets, Touch Settings). I could **not** compile Kotlin here, so the first Gradle sync and `./gradlew test lint assembleDebug` are still the real test. New code was written against the signatures already in files 01–04.

## Files to give Gemini (10)
00, 01, 02, 02B, 02C, 02D, 03, 04, 05, **07**. (File 00 said "ten" while listing nine; fixed by adding 07.) Attach your 4 screenshots too. Keep 06 and the old recovered spec for yourself.

## Mistakes in v3.0 found through the screenshots
| # | Problem | Fix in 07 |
|---|---|---|
| 1 | Docs said the official app has **no Hearing Assist**. It does (device page row) | Parity doc and master text corrected |
| 2 | **Clean Dust** and **Drain Moisture** were missing entirely | New capped phone-tone player and screen |
| 3 | Theme was light with a slate accent; official is dark with indigo | Dark-first tokens, correct system-bar icons, dark window background |
| 4 | EQ band was **64 Hz**; official is **63** | Fixed |
| 5 | Mode names/order were ANC, Transparency, Normal | Noise Reduction, Off, Transparency |
| 6 | Touch names were Single/Double/Triple/Long and "Disabled" | One Tap, Two Tap, Three Tap, Tap & Hold, No Action, ANC Switching |
| 7 | EQ used horizontal sliders and a flat chip list | 10 vertical sliders, Mood / Genres / Custom groups with thumbnails, 4 custom slots |
| 8 | Flat Material bottom bar; small hero card | Pill nav + support button; Devices hero with CONNECT; device header with L/R pills; hub rows |
| 9 | Case battery shown as if expected; official shows L and R only | Case stays "Not available" unless a verified profile reports it |
| 10 | Master file count wrong ("ten" for nine files) | Corrected |

## New in v4.0 (answering "more details, more features, more improvements")
Clean Dust, Drain Moisture, Name Change page, offline Product details and Tips pages, 5 mood presets plus 4 custom slots, vertical EQ with TalkBack actions and fine-tune mode, preset thumbnails, side picker tiles, 5 more unit tests (35 total), doc updates, theme-aware status bars.

## Deliberately not done
- **Artist presets** (real people, unknown values): replaced with a "Genres" group.
- **Official photo, logo, wordmark**: not copied; the original illustration stays.
- **News feed**: needs internet. Offline Tips replace it.
- **Fake hardware commands**: still impossible without your own capture (file 00 section 4). All hardware controls stay locked until you import a verified profile.

## What you must still do yourself
1. Capture the Bluetooth bytes from your own earbuds (guide in `docs/CAPTURE_PROTOCOL.md`), otherwise ANC/Off/Transparency, earbud battery, hardware EQ and touch writes will stay disabled. That is intended.
2. Build in Android Studio (`gradle wrapper --gradle-version 8.9`, then `./gradlew test lint assembleDebug`).
3. Test Clean Dust / Drain Moisture with earbuds **out of your ears**.
4. Record real results in `docs/PHYSICAL_DEVICE_TEST_RESULTS.md`.

## Reminders
- The screenshots show a black/gold Eclipse; your unit is white, so the app keeps its own original white illustration.
- Vendor reset tip (tap both earbuds 5 times) came from a reply to a *Mist Pro* owner and is not confirmed for the Eclipse.
