# File 07 — v4.0: official-app alignment (from real screenshots), corrections, new features, patches

**Give this file to Gemini together with 00, 01, 02, 02B, 02C, 02D, 03, 04 and 05.** (That is **ten** files. Master file 00 wrongly said "ten" while listing nine; this file makes the count correct.)
**Where this file conflicts with 00–05, this file wins.** Apply every patch in section 3 while creating the earlier files (so the final project contains the patched version, not the old one). Rules are unchanged: copy verbatim, fix only real compile errors, log every deviation in `docs/CHANGES_FROM_PROMPT.md`, never add `INTERNET`, never invent hardware commands.

---

## 1. What the official app really looks like (evidence: 4 screenshots, 30 Oct 2026)

| # | Observation in the official app | What was wrong or missing in v3.0 | v4.0 fix |
|---|---|---|---|
| 1 | **Dark UI only**: near-black page, charcoal cards, indigo/periwinkle accent, white headings | v3.0 default was a light pearl theme with slate accent | Dark-first theme, new tokens (section 2) |
| 2 | First screen titled **"Devices"**: big product image, name in capitals, `● READY`, huge rounded **CONNECT ›** button, **+ Add Device** | v3.0 Home was a small card | `DevicesHero` |
| 3 | Floating **pill bottom bar** (Home / News / Products / Profile) plus a separate round **support button** | v3.0 used a flat Material bar | `PillNavBar`; News = offline Tips, Products = offline Product details, Profile = Settings, support button = Help |
| 4 | Device page header: product name in accent colour, **L / R battery pills**, product image at right. **Only L and R are shown, no case battery** | v3.0 showed a Case card (kept, but gated) | `DeviceHeader`; Case stays "Not available" unless profile reports it. This confirms the case is not advertised |
| 5 | Mode selector order and names: **Noise Reduction · Off · Transparency** (segmented pill) | v3.0 said "Normal" and ordered ANC, Transparency, Normal | Labels and order changed; "Off" replaces "Normal" |
| 6 | Two tinted cards: **Touch Settings** (rose) and **Sound Settings** (indigo) | Not present | `HubTintCard` |
| 7 | Rows: **Name Change, Hearing Assist, Relaxation, Clean Dust, Drain Moisture** | **Clean Dust and Drain Moisture were completely missing.** Docs also wrongly said the official app has no Hearing Assist | New feature + corrected parity doc |
| 8 | EQ: **10 vertical sliders** labelled 32, **63**, 125, 250, 500, 1k, 2k, 4k, 8k, 16k | v3.0 used **64** Hz and horizontal sliders | Band fixed to 63; `VerticalEqPanel` |
| 9 | Preset groups **Mood** (Default, Energetic, Calm, Chill, Focus, Sad), **Artists** (4 named people), **Custom** (Custom 1, Custom 2) with mini bar thumbnails | No grouping, no thumbnails, no slots | Mood group, Genres group (replaces Artists), 4 Custom slots, `PresetTile` |
| 10 | Touch screen: Left/Right image tiles; gestures **One Tap, Two Tap, Three Tap, Tap & Hold**; actions **No Action**, **ANC Switching** | v3.0 used chips and "Single/Double/Triple/Long press", "Disabled" | `SidePicker`, official gesture and action names |

Not copied on purpose: the official product photo, logo, wordmark and the **artist names** (real people; their preset values are unknown and must not be invented). The "Artists" group is replaced by our own "Genres" group.

## 2. New design tokens (replace master section 7.1)

Dark is the **default** theme. Light remains available in Settings.

| Token | Dark (default) | Light |
|---|---|---|
| background | `#121214` | `#F6F6F4` |
| surface (cards) | `#2A2A32` | `#FFFFFF` |
| surfaceSecondary | `#1E1E25` | `#EEEEEC` |
| textPrimary | `#F5F5FA` | `#181818` |
| textSecondary | `#A8A8B8` | `#666663` |
| outline | `#3F3F4C` | `#D7D7D2` |
| accent (text/icons) | `#B4B6FF` | `#344C63` |
| accentFill (buttons, sliders) | `#5B5FEF` | `#344C63` |
| accentContainer (selected pill) | `#4A4D86` | `#E3EAF0` |
| onAccent (text on accentFill) | `#FFFFFF` | `#FFFFFF` |
| success | `#4CC38A` | `#2D6A4F` |
| warning | `#F1C46A` | `#9A6700` |
| error | `#F2A09A` | `#B3261E` |
| tintRose / tintRoseIcon (Touch card) | `#3A2830` / `#F28CA5` | `#F7E6EA` / `#B3405A` |
| tintIndigo (Sound card) | `#2B2B4D` | `#E3EAF0` |

Contrast: white on `#5B5FEF` is about 4.9:1; `#B4B6FF` on `#2A2A32` is above 7:1. Corner radii: cards 24 dp, pill nav 36 dp, CONNECT button fully rounded.
App/launcher/shortcut/favicon base colour changes from `#344C63` to `#3D41B0` (white ring stays). Update `ic_launcher_background.xml`, `ic_shortcut.xml` (the first path) and `assets/favicon.svg` (the `rect` fill).

## 3. Patches to existing files (exact)

### 3.1 `domain/Models.kt`
1. `val FREQS = listOf(32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)` → `val FREQS = listOf(32, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)`
2. Directly after the closing `)` of `val PRESETS` add:
```kotlin
    /** Mood group (mirrors the official app's group names; values are our own). "Default" in the UI selects Flat. */
    val MOODS: List<EqProfile> = listOf(
        p("mood_energetic", "Energetic", 3f, 3f, 2f, 1f, 0f, 1f, 3f, 4f, 4f, 3f),
        p("mood_calm", "Calm", 2f, 2f, 1f, 0f, 0f, 0f, -1f, -2f, -2f, -2f),
        p("mood_chill", "Chill", 3f, 3f, 2f, 1f, 0f, -1f, -1f, 0f, 1f, 1f),
        p("mood_focus", "Focus", -3f, -2f, -1f, 0f, 1f, 2f, 2f, 1f, 0f, -1f),
        p("mood_sad", "Sad", 3f, 2f, 1f, 0f, -1f, -2f, -2f, -1f, -1f, -2f)
    )

    val ALL_BUILTIN: List<EqProfile> get() = PRESETS + MOODS
```
3. `enum class ListeningMode`: change `NORMAL("Normal", "NORMAL")` to `NORMAL("Off", "NORMAL")` (key unchanged, so profiles stay valid).
4. `enum class Gesture`: replace with `TAP("One Tap"), DOUBLE("Two Tap"), TRIPLE("Three Tap"), LONG("Tap & Hold")`.
5. `duplicateActions`: change `.filter { it != "Disabled" }` to `.filter { it != "Disabled" && it != "No Action" }`.
6. `TOUCH_ACTION_HELP`: add `"No Action" to "Does nothing."` and rename the key `"Cycle ANC/Transparency/Normal"` to `"ANC Switching"`.
7. `GENERIC_TOUCH_ACTIONS`: replace `"Cycle ANC/Transparency/Normal"` with `"ANC Switching"` and `"Disabled"` with `"No Action"`.

### 3.2 `AppViewModel.kt`
- `fun allEqProfiles(): List<EqProfile> = EqDefaults.PRESETS + settings.value.customEq` → `EqDefaults.ALL_BUILTIN + settings.value.customEq`.
- `fun resetEq() = selectEq(EqDefaults.PRESETS.first())` stays.

### 3.3 `data/SettingsStore.kt`
Default theme becomes dark: in `AppSettings` `val theme: String = "system"` → `"dark"`; in `flow` `p[K.theme] ?: "system"` → `p[K.theme] ?: "dark"`. In `AppViewModel.restoreBackup` change `o.optString("theme", "system")` to `o.optString("theme", "dark")`.

### 3.4 `ui/Theme.kt` — replace the whole file with:
```kotlin
package com.eclipsecompanion.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
class EclipseColors(
    val background: Color, val surface: Color, val surfaceSecondary: Color,
    val textPrimary: Color, val textSecondary: Color, val outline: Color,
    val accent: Color, val accentFill: Color, val accentContainer: Color,
    val success: Color, val warning: Color, val error: Color, val onAccent: Color,
    val tintRose: Color, val tintRoseIcon: Color, val tintIndigo: Color
)

val LightEclipse = EclipseColors(
    background = Color(0xFFF6F6F4), surface = Color(0xFFFFFFFF), surfaceSecondary = Color(0xFFEEEEEC),
    textPrimary = Color(0xFF181818), textSecondary = Color(0xFF666663), outline = Color(0xFFD7D7D2),
    accent = Color(0xFF344C63), accentFill = Color(0xFF344C63), accentContainer = Color(0xFFE3EAF0),
    success = Color(0xFF2D6A4F), warning = Color(0xFF9A6700), error = Color(0xFFB3261E), onAccent = Color(0xFFFFFFFF),
    tintRose = Color(0xFFF7E6EA), tintRoseIcon = Color(0xFFB3405A), tintIndigo = Color(0xFFE3EAF0)
)

val DarkEclipse = EclipseColors(
    background = Color(0xFF121214), surface = Color(0xFF2A2A32), surfaceSecondary = Color(0xFF1E1E25),
    textPrimary = Color(0xFFF5F5FA), textSecondary = Color(0xFFA8A8B8), outline = Color(0xFF3F3F4C),
    accent = Color(0xFFB4B6FF), accentFill = Color(0xFF5B5FEF), accentContainer = Color(0xFF4A4D86),
    success = Color(0xFF4CC38A), warning = Color(0xFFF1C46A), error = Color(0xFFF2A09A), onAccent = Color(0xFFFFFFFF),
    tintRose = Color(0xFF3A2830), tintRoseIcon = Color(0xFFF28CA5), tintIndigo = Color(0xFF2B2B4D)
)

val LocalEclipse = staticCompositionLocalOf { DarkEclipse }

object E {
    val c: EclipseColors @Composable @ReadOnlyComposable get() = LocalEclipse.current
}

private val EclipseTypography = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 12.sp)
)

@Composable
fun EclipseTheme(mode: String, content: @Composable () -> Unit) {
    val dark = when (mode) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    val e = if (dark) DarkEclipse else LightEclipse
    val scheme = if (dark) darkColorScheme(
        primary = e.accentFill, onPrimary = e.onAccent, primaryContainer = e.accentContainer, onPrimaryContainer = e.textPrimary,
        background = e.background, onBackground = e.textPrimary, surface = e.surface, onSurface = e.textPrimary,
        surfaceVariant = e.surfaceSecondary, onSurfaceVariant = e.textSecondary, outline = e.outline, error = e.error,
        secondaryContainer = e.accentContainer, onSecondaryContainer = e.textPrimary
    ) else lightColorScheme(
        primary = e.accentFill, onPrimary = e.onAccent, primaryContainer = e.accentContainer, onPrimaryContainer = e.textPrimary,
        background = e.background, onBackground = e.textPrimary, surface = e.surface, onSurface = e.textPrimary,
        surfaceVariant = e.surfaceSecondary, onSurfaceVariant = e.textSecondary, outline = e.outline, error = e.error,
        secondaryContainer = e.accentContainer, onSecondaryContainer = e.textPrimary
    )
    CompositionLocalProvider(LocalEclipse provides e) {
        MaterialTheme(colorScheme = scheme, typography = EclipseTypography, content = content)
    }
}
```

### 3.5 `ui/Components.kt`
In `PrimaryButton` change `containerColor = if (danger) E.c.error else E.c.accent` to `containerColor = if (danger) E.c.error else E.c.accentFill`.

### 3.6 `res/values/themes.xml` and `res/values-night/themes.xml` — both files become (avoids a white flash before the dark UI draws):
```xml
<resources>
    <style name="Theme.EclipseCompanion" parent="android:Theme.Material.NoActionBar">
        <item name="android:windowBackground">#121214</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLayoutInDisplayCutoutMode">shortEdges</item>
    </style>
</resources>
```

### 3.7 `MainActivity.kt`
1. Add imports: `android.graphics.Color as AndroidColor`, `androidx.activity.SystemBarStyle`, `androidx.compose.foundation.isSystemInDarkTheme`, `androidx.compose.runtime.LaunchedEffect`, `com.eclipsecompanion.app.audio.MaintenanceHub`.
2. Replace the `setContent { ... }` block with:
```kotlin
        setContent {
            val s by vm.settings.collectAsStateWithLifecycle()
            val dark = s.theme == "dark" || (s.theme == "system" && isSystemInDarkTheme())
            // Keeps status-bar and navigation-bar icons readable in both themes.
            LaunchedEffect(dark) {
                val style = if (dark) SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                else SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }
            EclipseTheme(s.theme) { EclipseApp(vm, this@MainActivity, route.value) { route.value = null } }
        }
```
3. `override fun onStop() { vm.setForeground(false); super.onStop() }` → `override fun onStop() { vm.setForeground(false); MaintenanceHub.stopAll(); super.onStop() }` (Clean Dust and Drain Moisture tones never continue in the background).

### 3.8 `ui/Screens.kt` (app shell and screens)
**EclipseApp**
- After the line `BackHandler(enabled = sub != null) { sub = null }` add:
```kotlin
    val openRoute: (String) -> Unit = { r ->
        if (r.startsWith("tab:")) { tabName = r.removePrefix("tab:").uppercase(); sub = null } else sub = r
    }
```
- In the `bottomBar` lambda replace the whole `NavigationBar(containerColor = E.c.surface) { ... }` with `PillNavBar(tabs, tab, { tabName = it.name }) { sub = "help" }` (keep the surrounding `if (!wide && sub == null && s.firstRunDone)`).
- In the `when (sub)` block add these cases before `"help"`:
```kotlin
                    "rename" -> NameScreen(vm) { sub = null }
                    "dust" -> MaintenanceScreen(com.eclipsecompanion.app.audio.MaintMode.DUST) { sub = null }
                    "drain" -> MaintenanceScreen(com.eclipsecompanion.app.audio.MaintMode.DRAIN) { sub = null }
                    "products" -> ProductsScreen { sub = null }
                    "tips" -> TipsScreen { sub = null }
```
- Replace every `open = { sub = it }` (Home, Sound, Settings calls) with `open = openRoute`.

**HomeScreen**
- `Title("Eclipse")` → `Title("Devices")`.
- Replace the first `ECard { EclipseIllustration() ... }` block (the one with Refresh/Disconnect/Connect earbuds/Reconnect buttons) with:
```kotlin
        if (connected) DeviceHeader(name ?: s.alias, bat.left, bat.right, { vm.refresh() }, { vm.disconnect() })
        else DevicesHero(
            name = s.alias,
            status = when (phase) { Phase.CONNECTING, Phase.DISCOVERING -> "Connecting"; Phase.ERROR -> "Needs attention"; else -> "Ready to connect" },
            onConnect = { host.requestBluetooth { if (s.deviceAddress != null) vm.reconnectSaved() else open("connect") } },
            onAdd = { host.requestBluetooth { open("connect") } }
        )
```
- Replace the `Row(...) { listOf(Triple(ListeningMode.ANC, ...)) ... }` mode-card block in HomeScreen **and** the identical block in `ModesTab` with `ModeSelectorRow(vm)`.
- Replace the final `ECard(onClick = goAssist) { ... Hearing Assist ... }` with `DeviceHub(open = open, goAssist = goAssist, simple = s.simpleMode)`.

**ControlsScreen**: replace `ChipRow(Side.values().map { it.label }, sd.label) { ... }` with `SidePicker(sd) { side = it.name }`.

**EqualizerTab**
- Replace the `ChipRow(all.map { it.name }, ...)` line with `EqPresetGroups(vm)`.
- Inside the `ECard`, replace `EqDefaultsFreqLabels(eq.gains) { i, v -> vm.setBand(i, v) }` with:
```kotlin
        VerticalEqPanel(eq.gains) { i, v -> vm.setBand(i, v) }
        var fine by remember { mutableStateOf(false) }
        SecondaryButton(if (fine) "Hide fine tune" else "Fine tune with − / + buttons", { fine = !fine }, Modifier.fillMaxWidth())
        if (fine) EqDefaultsFreqLabels(eq.gains) { i, v -> vm.setBand(i, v) }
```

### 3.9 `ui/Screens2.kt`
- `HelpScreen` list: append the three new Q&A pairs from section 7 (and the same text goes into `docs/TROUBLESHOOTING.md`).
- Settings "Preferred listening mode" chip labels: replace `"Normal"` with `"Off"` in all three places of that block (the stored key stays `NORMAL`).

### 3.10 Document fixes (master file 00 and file 05)
- File 00 header: "all **ten** files (00-05, with 02B/02C/02D)" → "all **ten** files (00, 01, 02, 02B, 02C, 02D, 03, 04, 05, 07)". Add a row `| 07 | 07_V4_SCREENSHOT_ALIGNMENT_AND_NEW_FEATURES.md | Official-app alignment, corrections, Clean Dust, Drain Moisture, vertical EQ, pill navigation, extra tests |`. Section 12 step 2: add "07 patches are applied while creating the earlier files; 07 new files after 04".
- File 00 section 5 footnote: delete "It does **not** advertise case battery or Hearing Assist". Replace with: "The installed official app (screenshots, 30 Oct 2026) shows Hearing Assist, Relaxation, Name Change, Clean Dust, Drain Moisture, Touch Settings and Sound Settings, and shows only left and right battery (no case)."
- File 00 section 5 parity table: add rows `Clean Dust | local tone player (phone-side) | L` and `Drain Moisture | local tone player (phone-side) | L`; rename "Meditation Mode" row to "Meditation / Relaxation".
- File 00 section 7.1 colour table → section 2 above. Section 7.3: bottom bar is the **pill bar** plus a round support button (opens Help). Section 7.4 Home: "Devices" hero when disconnected, `DeviceHeader` when connected, device hub rows. Section 7.4 Sound/Equalizer: vertical sliders, Mood/Genres/Custom groups.
- File 00 sections 8 and 10: add the new files (section 4) and set the unit-test total to **35** (30 + 5 from section 6).
- File 00 section 5.1 and 5.2: add "Mode names/order follow the official app (Noise Reduction · Off · Transparency)".
- File 05 `OFFICIAL_FEATURE_PARITY.md`: replace the Hearing Assist row and add rows listed in section 7 below. `DEVICE_CAPABILITY_MATRIX.md`: add rows for Clean Dust and Drain Moisture (Works now). `TEST_PLAN.md`: add the five test names and the physical checks in section 7. `SECURITY_REVIEW.md`: add the maintenance-tone paragraph in section 7. `KNOWN_LIMITATIONS.md`: add the bullets in section 7. `SOURCE_AND_ASSUMPTION_REGISTER.md`: add the screenshot rows in section 7.

## 4. New files

Project tree additions:
```
app/src/main/java/com/eclipsecompanion/app/audio/MaintenanceTones.kt
app/src/main/java/com/eclipsecompanion/app/ui/Hub.kt
app/src/main/java/com/eclipsecompanion/app/ui/EqPanel.kt
app/src/test/java/com/eclipsecompanion/app/ExtraTests2.kt
```

### `app/src/main/java/com/eclipsecompanion/app/audio/MaintenanceTones.kt`
Clean Dust and Drain Moisture. Honest scope: the phone plays a fixed tone or slow sweep through the connected earbuds. The earbuds are **not** controlled and no cleaning result is promised. Always short, fixed length, fade in/out, capped level, stops on Stop, call, focus loss, route loss, leaving the screen and backgrounding.
```kotlin
package com.eclipsecompanion.app.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Process
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

enum class MaintMode(val label: String, val seconds: Int, val amp: Float, val blurb: String) {
    DRAIN("Drain moisture", 30, 0.5f,
        "Plays a steady low tone (about 165 Hz) for 30 seconds. The vibration can help push water out of the speaker opening."),
    DUST("Clean dust", 60, 0.25f,
        "Plays a slow rising and falling tone (about 400 to 1600 Hz) for 60 seconds. The vibration can loosen dust from the speaker mesh.")
}

enum class MaintChannel(val label: String) { BOTH("Both"), LEFT("Left only"), RIGHT("Right only") }

class MaintenancePlayer(private val ctx: Context) {
    val playing = MutableStateFlow(false)
    val remainingSec = MutableStateFlow(0)
    val error = MutableStateFlow<String?>(null)
    @Volatile private var run = false
    private var thread: Thread? = null
    private val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private fun fail(m: String): String { error.value = m; return m }

    /** Returns an error message, or null when playback started. */
    fun start(mode: MaintMode, channel: MaintChannel): String? {
        stop()
        error.value = null
        val out = HearingAssistHub.findEarbudOutput(ctx) ?: return fail("Connect your earbuds through Bluetooth first. This feature only plays through them.")
        if (am.mode != AudioManager.MODE_NORMAL) return fail("A call is active. Finish the call first.")
        val rate = 44_100
        val minBuf = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT)
        if (minBuf <= 0) return fail("Audio output is unavailable on this phone.")
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).setAudioAttributes(attrs)
            .setOnAudioFocusChangeListener { c ->
                if (c == AudioManager.AUDIOFOCUS_LOSS || c == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) run = false
            }.build()
        if (am.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return fail("Could not get audio focus. Stop other audio and try again.")
        val track = AudioTrack.Builder().setAudioAttributes(attrs)
            .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build())
            .setBufferSizeInBytes(max(minBuf, 16_384)).setTransferMode(AudioTrack.MODE_STREAM).build()
        track.setPreferredDevice(out)
        run = true; playing.value = true; remainingSec.value = mode.seconds
        thread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            val frames = 1024
            val buf = ShortArray(frames * 2)
            val total = mode.seconds.toLong() * rate
            var n = 0L
            var phase = 0.0
            var chunks = 0
            try {
                track.play()
                while (run && n < total) {
                    for (i in 0 until frames) {
                        val pos = n + i
                        val t = pos.toDouble() / rate
                        val f = if (mode == MaintMode.DRAIN) 165.0 else 400.0 + 1200.0 * (0.5 + 0.5 * sin(2 * PI * t / 10.0))
                        phase += 2 * PI * f / rate
                        if (phase > 2 * PI) phase -= 2 * PI
                        val leftSamples = (total - pos).toDouble()
                        val env = min(1.0, min(pos / (rate * 3.0), leftSamples / (rate * 2.0))).coerceAtLeast(0.0)   // 3 s fade-in, 2 s fade-out
                        val v = (sin(phase) * mode.amp * env * 32767).toInt().toShort()
                        buf[2 * i] = if (channel != MaintChannel.RIGHT) v else 0.toShort()
                        buf[2 * i + 1] = if (channel != MaintChannel.LEFT) v else 0.toShort()
                    }
                    if (track.write(buf, 0, buf.size) < 0) { error.value = "Stopped: audio output error."; break }
                    n += frames
                    remainingSec.value = ((total - n) / rate).toInt().coerceAtLeast(0)
                    if (++chunks % 40 == 0 && (HearingAssistHub.findEarbudOutput(ctx) == null || am.mode != AudioManager.MODE_NORMAL)) {
                        error.value = "Stopped: the earbuds output was lost or a call started."; break
                    }
                }
            } catch (e: Exception) {
                error.value = "Stopped: audio error (${e.javaClass.simpleName})."
            } finally {
                run = false
                runCatching { track.stop() }; track.release()
                am.abandonAudioFocusRequest(focus)
                playing.value = false; remainingSec.value = 0
            }
        }, "Maintenance").also { it.start() }
        return null
    }

    fun stop() { run = false; thread?.join(1500); thread = null }
}

/** One shared player so the tone survives recomposition and can be stopped from MainActivity.onStop. */
object MaintenanceHub {
    @Volatile private var player: MaintenancePlayer? = null
    fun get(ctx: Context): MaintenancePlayer =
        player ?: synchronized(this) { player ?: MaintenancePlayer(ctx.applicationContext).also { player = it } }
    fun stopAll() { player?.stop() }
}
```

### `app/src/main/java/com/eclipsecompanion/app/ui/Hub.kt`
Pill navigation, Devices hero, device header, device hub, mode selector, side picker, Name screen, Clean Dust / Drain Moisture screen, offline Products and Tips.
```kotlin
package com.eclipsecompanion.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eclipsecompanion.app.AppViewModel
import com.eclipsecompanion.app.audio.HearingAssistHub
import com.eclipsecompanion.app.audio.MaintChannel
import com.eclipsecompanion.app.audio.MaintMode
import com.eclipsecompanion.app.audio.MaintenanceHub
import com.eclipsecompanion.app.domain.ListeningMode
import com.eclipsecompanion.app.domain.Reported
import com.eclipsecompanion.app.domain.Side

/* ------------------------------------------------------------------ Pill navigation (official style) */

@Composable
fun PillNavBar(tabs: List<Tab>, selected: Tab, onSelect: (Tab) -> Unit, onHelp: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(Modifier.weight(1f), shape = RoundedCornerShape(36.dp), color = E.c.surface, border = BorderStroke(1.dp, E.c.outline)) {
            Row(Modifier.padding(6.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                tabs.forEach { t ->
                    val sel = t == selected
                    Column(
                        Modifier.weight(1f).heightIn(min = 56.dp).clip(RoundedCornerShape(28.dp))
                            .background(if (sel) E.c.accentContainer else Color.Transparent)
                            .clickable(role = Role.Tab, onClick = { onSelect(t) })
                            .semantics { stateDescription = if (sel) "Selected" else "Not selected"; contentDescription = t.label }
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
                    ) {
                        Icon(t.icon, contentDescription = null, tint = if (sel) E.c.textPrimary else E.c.textSecondary)
                        Text(t.label, style = MaterialTheme.typography.labelSmall, color = if (sel) E.c.textPrimary else E.c.textSecondary, maxLines = 1)
                    }
                }
            }
        }
        Surface(Modifier.size(64.dp), shape = CircleShape, color = E.c.surface, border = BorderStroke(1.dp, E.c.outline)) {
            IconButton(onHelp, Modifier.fillMaxSize()) { Icon(Icons.Rounded.SupportAgent, contentDescription = "Help and support", tint = E.c.accent) }
        }
    }
}

/* ------------------------------------------------------------------ Devices hero and device header */

@Composable
fun DevicesHero(name: String, status: String, onConnect: () -> Unit, onAdd: () -> Unit) {
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EclipseIllustration(Modifier.padding(vertical = 8.dp))
        Text(name.uppercase(), style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp, fontWeight = FontWeight.Bold), color = E.c.textPrimary, textAlign = TextAlign.Center)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val dot = E.c.accent
            Canvas(Modifier.size(8.dp)) { drawCircle(dot) }
            Text(status.uppercase(), style = MaterialTheme.typography.labelLarge, color = E.c.textSecondary, letterSpacing = 3.sp)
        }
        Button(
            onClick = onConnect, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth(0.85f).heightIn(min = 56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = E.c.accentFill, contentColor = E.c.onAccent)
        ) {
            Text("CONNECT", style = MaterialTheme.typography.labelLarge, letterSpacing = 4.sp)
            Spacer(Modifier.width(12.dp))
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null)
        }
        TextButton(onAdd) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = E.c.accent)
            Spacer(Modifier.width(8.dp))
            Text("Add device", color = E.c.accent)
        }
    }
}

@Composable
private fun BatPill(label: String, value: Reported<Int>) {
    val text = (value as? Reported.Available)?.value?.let { "$it%" } ?: "—"
    val desc = (value as? Reported.Available)?.value?.let { "$label battery $it percent" } ?: "$label battery not available"
    Surface(shape = RoundedCornerShape(16.dp), color = E.c.surface, border = BorderStroke(1.dp, E.c.outline), modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = desc }) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = E.c.textSecondary)
            val ok = E.c.success; val frame = E.c.outline
            val frac = ((value as? Reported.Available)?.value ?: 0) / 100f
            Canvas(Modifier.size(width = 30.dp, height = 14.dp)) {
                drawRoundRect(frame, cornerRadius = CornerRadius(5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
                if (frac > 0f) drawRoundRect(ok, Offset(4f, 4f), Size((size.width - 8f) * frac, size.height - 8f), CornerRadius(3f))
            }
            Text(text, style = MaterialTheme.typography.labelLarge, color = E.c.textPrimary)
        }
    }
}

@Composable
fun DeviceHeader(name: String, left: Reported<Int>, right: Reported<Int>, onRefresh: () -> Unit, onDisconnect: () -> Unit) {
    ECard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(name.uppercase(), style = MaterialTheme.typography.headlineMedium, color = E.c.accent)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { BatPill("L", left); BatPill("R", right) }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onRefresh) { Text("Refresh") }
                    TextButton(onDisconnect) { Text("Disconnect") }
                }
            }
            EclipseIllustration(Modifier.weight(1f))
        }
    }
}

/* ------------------------------------------------------------------ Mode selector (official order and names) */

@Composable
fun ModeSelectorRow(vm: AppViewModel) {
    val c = vm.controller
    val phase by c.phase.collectAsStateWithLifecycle()
    val mode by c.confirmedMode.collectAsStateWithLifecycle()
    val pending by c.pendingMode.collectAsStateWithLifecycle()
    val profile by c.profile.collectAsStateWithLifecycle()
    val can = c.can("setMode")
    val reason = shortReason(phase, profile.isVerified("setMode"))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            Triple(ListeningMode.ANC, "Noise Reduction", Icons.Rounded.VolumeOff),
            Triple(ListeningMode.NORMAL, "Off", Icons.Rounded.PowerSettingsNew),
            Triple(ListeningMode.TRANSPARENCY, "Transparency", Icons.Rounded.Hearing)
        ).forEach { (m, label, icon) ->
            ModeCard(
                label, icon, selected = (mode as? Reported.Available)?.value == m, pending = pending == m,
                enabled = can, reason = if (can) null else reason, onClick = { vm.setMode(m) }, modifier = Modifier.weight(1f)
            )
        }
    }
}

/* ------------------------------------------------------------------ Device hub rows */

@Composable
private fun HubTintCard(title: String, subtitle: String, icon: ImageVector, tint: Color, iconTint: Color, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Surface(modifier, shape = shape, color = tint, border = BorderStroke(1.dp, iconTint.copy(alpha = 0.4f))) {
        Column(Modifier.clip(shape).clickable(role = Role.Button, onClick = onClick).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(14.dp), color = iconTint.copy(alpha = 0.25f)) { Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.padding(12.dp)) }
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = E.c.textSecondary)
            }
            Text(title, style = MaterialTheme.typography.titleMedium, color = E.c.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = E.c.textSecondary)
        }
    }
}

@Composable
private fun HubRow(title: String, subtitle: String?, icon: ImageVector, onClick: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Surface(Modifier.fillMaxWidth(), shape = shape, color = E.c.surface, border = BorderStroke(1.dp, E.c.outline)) {
        Row(
            Modifier.clip(shape).clickable(role = Role.Button, onClick = onClick).heightIn(min = 64.dp).padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(icon, contentDescription = null, tint = E.c.accent)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = E.c.textPrimary)
                if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = E.c.textSecondary)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = E.c.textSecondary)
        }
    }
}

/** The official device page list: Touch/Sound cards, then Name Change, Hearing Assist, Relaxation, Clean Dust, Drain Moisture (+ our extras). */
@Composable
fun DeviceHub(open: (String) -> Unit, goAssist: () -> Unit, simple: Boolean) {
    if (!simple) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        HubTintCard("Touch Settings", "Gestures & controls", Icons.Rounded.TouchApp, E.c.tintRose, E.c.tintRoseIcon, { open("tab:controls") }, Modifier.weight(1f))
        HubTintCard("Sound Settings", "EQ & personal sound", Icons.Rounded.Tune, E.c.tintIndigo, E.c.accent, { open("tab:sound") }, Modifier.weight(1f))
    }
    HubRow("Name Change", null, Icons.Rounded.Edit) { open("rename") }
    HubRow("Hearing Assist", "Not a medical device", Icons.Rounded.Hearing, goAssist)
    HubRow("Relaxation", null, Icons.Rounded.GraphicEq) { open("relax") }
    HubRow("Clean Dust", null, Icons.Rounded.CleaningServices) { open("dust") }
    HubRow("Drain Moisture", null, Icons.Rounded.WaterDrop) { open("drain") }
    HubRow("Product details", "Works offline", Icons.Rounded.Info) { open("products") }
    HubRow("Tips", "Works offline", Icons.Rounded.Lightbulb) { open("tips") }
}

/* ------------------------------------------------------------------ Touch side picker (image-style tiles) */

@Composable
private fun EarbudTile(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(24.dp)
    val stem = E.c.textSecondary; val head = E.c.textPrimary
    Surface(
        modifier.semantics { stateDescription = if (selected) "Selected" else "Not selected"; contentDescription = "$label earbud" },
        shape = shape, color = if (selected) E.c.tintIndigo else E.c.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) E.c.accentFill else E.c.outline)
    ) {
        Column(Modifier.clip(shape).clickable(role = Role.Tab, onClick = onClick).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Canvas(Modifier.size(width = 64.dp, height = 90.dp)) {
                val w = size.width; val h = size.height
                drawRoundRect(stem, Offset(w * 0.40f, h * 0.35f), Size(w * 0.20f, h * 0.62f), CornerRadius(w * 0.10f))
                drawCircle(head, w * 0.30f, Offset(w * 0.5f, h * 0.30f))
            }
            Text(label, style = MaterialTheme.typography.titleMedium, color = if (selected) E.c.accent else E.c.textSecondary)
        }
    }
}

@Composable
fun SidePicker(selected: Side, onSelect: (Side) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Side.values().forEach { s -> EarbudTile(s.label.substringBefore(' '), s == selected, { onSelect(s) }, Modifier.weight(1f)) }
    }
}

/* ------------------------------------------------------------------ Name Change */

@Composable
fun NameScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<String?>(null) }
    SubScreen("Name Change", onBack) {
        ECard {
            Section("Name on this phone")
            Body(s.alias)
            SecondaryButton("Change name on this phone", { dialog = "alias" }, Modifier.fillMaxWidth())
            Body("This name is stored only in this app.")
        }
        ECard {
            Section("Name broadcast by the earbuds")
            if (vm.controller.can("setName")) PrimaryButton("Rename earbuds", { dialog = "rename" }, Modifier.fillMaxWidth())
            else {
                Body(vm.controller.reasonFor("setName"))
                PrimaryButton("Rename earbuds", {}, Modifier.fillMaxWidth(), enabled = false)
            }
        }
    }
    when (dialog) {
        "alias" -> TextInputDialog("Name on this phone", s.alias, 30, { vm.setAlias(it) }, { dialog = null })
        "rename" -> TextInputDialog("Earbud name (1–20 plain characters)", "", 20, { vm.renameEarbuds(it) }, { dialog = null })
    }
}

/* ------------------------------------------------------------------ Clean Dust / Drain Moisture */

@Composable
fun MaintenanceScreen(mode: MaintMode, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val player = remember { MaintenanceHub.get(ctx) }
    val playing by player.playing.collectAsStateWithLifecycle()
    val remaining by player.remainingSec.collectAsStateWithLifecycle()
    val err by player.error.collectAsStateWithLifecycle()
    var channel by rememberSaveable { mutableStateOf(MaintChannel.BOTH.name) }
    var confirmed by rememberSaveable { mutableStateOf(false) }
    val route = remember(playing) { HearingAssistHub.routeName(ctx) }
    val hasRoute = route.startsWith("Output")
    DisposableEffect(Unit) { onDispose { player.stop() } }

    SubScreen(mode.label, onBack) {
        Banner("Take the earbuds OUT of your ears before you start. Never wear them while this tone plays, and lower the phone volume before you put them back in.", Kind.WARN)
        ECard {
            Section(mode.label)
            Body(mode.blurb)
            Body("This is a phone-made sound only. The earbuds are not controlled, and a result is not guaranteed. It does not replace gentle manual cleaning or drying.")
        }
        Section("Earbud")
        ChipRow(MaintChannel.values().map { it.label }, MaintChannel.valueOf(channel).label) { l -> channel = MaintChannel.values().first { it.label == l }.name }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Checkbox(confirmed, { confirmed = it })
            Text("My earbuds are out of my ears.", style = MaterialTheme.typography.bodyLarge, color = E.c.textPrimary, modifier = Modifier.weight(1f))
        }
        Body(route)
        err?.let { Banner(it, Kind.ERROR) }
        if (playing) {
            Body("Time left %02d:%02d".format(remaining / 60, remaining % 60))
            PrimaryButton("Stop", { player.stop() }, Modifier.fillMaxWidth(), danger = true)
        } else {
            PrimaryButton("Start", { player.start(mode, MaintChannel.valueOf(channel)) }, Modifier.fillMaxWidth(), enabled = confirmed && hasRoute)
            if (!hasRoute) Body("Connect your earbuds in Android Bluetooth settings first.")
        }
    }
}

/* ------------------------------------------------------------------ Products and Tips (offline, replaces the official News and Products tabs) */

@Composable
fun ProductsScreen(onBack: () -> Unit) {
    SubScreen("Product details", onBack) {
        ECard {
            Section("Ronin Eclipse R-7065")
            Body("Bluetooth 5.3, active noise cancelling with transparency mode, quad-microphone call noise reduction (ENC), 10 mm drivers, charging case.")
            Body("Battery capacity listed by retailers: about 60 mAh per earbud and 700 mAh for the case. No multipoint (two-device) connection and no water-resistance rating are listed.")
        }
        Banner("These details come from public product listings, which are secondary sources. Check your box and printed manual. This app makes no promise about runtime, noise reduction in dB or water resistance.", Kind.INFO)
        ECard {
            Section("ANC, transparency and ENC")
            Body("Noise Reduction (ANC) lowers outside sound you hear. Transparency lets outside sound in. Off applies neither. Call noise reduction (ENC) cleans your voice for the other person and is a separate feature.")
        }
        ECard {
            Section("Manual")
            Body("No manual is bundled. Use the printed manual that came with the earbuds.")
        }
    }
}

@Composable
fun TipsScreen(onBack: () -> Unit) {
    SubScreen("Tips", onBack) {
        listOf(
            "Fit" to "Try each ear-tip size. A good seal improves bass and noise cancelling.",
            "Listening safely" to "Keep volume comfortable and take breaks. If sounds feel sharp or your ears ring, stop and lower the volume.",
            "Transparency is not a safety device" to "Stay alert near traffic. Transparency does not guarantee you will hear every hazard.",
            "Cleaning" to "Wipe with a dry soft cloth. Do not push tools into the speaker opening. Clean Dust and Drain Moisture in this app only play a sound; they do not replace careful cleaning.",
            "Charging care" to "General advice: keep the case and earbuds away from heat, and avoid leaving them fully drained for long periods.",
            "Pairing" to "Open the case, keep the earbuds close to the phone, and choose them in Android Bluetooth settings. Then open this app and tap Connect."
        ).forEach { (t, b) -> ECard { Section(t); Body(b) } }
    }
}
```

### `app/src/main/java/com/eclipsecompanion/app/ui/EqPanel.kt`
Vertical sliders, mini-bar preset tiles, Mood / Genres / Custom groups, four Custom slots. Each band is a separate composable with its own value, so one slider can never move another.
```kotlin
package com.eclipsecompanion.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.eclipsecompanion.app.AppViewModel
import com.eclipsecompanion.app.EqState
import com.eclipsecompanion.app.domain.EqDefaults
import com.eclipsecompanion.app.domain.EqProfile
import kotlinx.coroutines.launch

private fun yToDb(y: Float, h: Float): Float {
    val frac = (1f - y / h.coerceAtLeast(1f)).coerceIn(0f, 1f)
    val raw = EqDefaults.MIN_DB + frac * (EqDefaults.MAX_DB - EqDefaults.MIN_DB)
    return Math.round(raw * 2f) / 2f
}

@Composable
private fun EqBandColumn(index: Int, hz: Int, gain: Float, onBand: (Int, Float) -> Unit, modifier: Modifier) {
    val c = E.c
    val label = if (hz >= 1000) "${hz / 1000}k" else "$hz"
    val speak = if (hz >= 1000) "${hz / 1000} kilohertz" else "$hz hertz"
    val cb by rememberUpdatedState(onBand)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("%+.0f".format(gain), style = MaterialTheme.typography.labelSmall, color = c.textSecondary, maxLines = 1)
        Box(
            Modifier.weight(1f).width(40.dp)
                .pointerInput(index) { detectTapGestures { o -> cb(index, yToDb(o.y, size.height.toFloat())) } }
                .pointerInput(index) {
                    detectVerticalDragGestures(onVerticalDrag = { ch, _ -> ch.consume(); cb(index, yToDb(ch.position.y, size.height.toFloat())) })
                }
                .semantics {
                    contentDescription = "$speak band"
                    stateDescription = "%+.1f decibels".format(gain)
                    customActions = listOf(
                        CustomAccessibilityAction("Raise by half a decibel") { cb(index, gain + 0.5f); true },
                        CustomAccessibilityAction("Lower by half a decibel") { cb(index, gain - 0.5f); true }
                    )
                }
        ) {
            Canvas(Modifier.matchParentSize()) {
                val w = size.width; val h = size.height; val cx = w / 2f
                val r = 11.dp.toPx(); val track = 6.dp.toPx()
                val frac = ((gain - EqDefaults.MIN_DB) / (EqDefaults.MAX_DB - EqDefaults.MIN_DB)).coerceIn(0f, 1f)
                val ty = (h * (1f - frac)).coerceIn(r, h - r)
                drawRoundRect(c.outline, Offset(cx - track / 2f, r), Size(track, h - 2 * r), CornerRadius(track / 2f))
                drawRoundRect(c.accentFill, Offset(cx - track / 2f, ty), Size(track, h - r - ty), CornerRadius(track / 2f))
                drawLine(c.textSecondary.copy(alpha = 0.5f), Offset(cx - 10.dp.toPx(), h / 2f), Offset(cx + 10.dp.toPx(), h / 2f), strokeWidth = 1.dp.toPx())
                drawCircle(c.surface, r, Offset(cx, ty))
                drawCircle(c.accent, r, Offset(cx, ty), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = c.textSecondary, maxLines = 1)
    }
}

/** Ten vertical sliders (32 ... 16k). Place it inside an ECard. */
@Composable
fun VerticalEqPanel(gains: List<Float>, onBand: (Int, Float) -> Unit) {
    Row(Modifier.fillMaxWidth().height(240.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        EqDefaults.FREQS.forEachIndexed { i, hz -> EqBandColumn(i, hz, gains.getOrElse(i) { 0f }, onBand, Modifier.weight(1f)) }
    }
}

@Composable
fun PresetTile(name: String, gains: List<Float>, selected: Boolean, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = E.c
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier.semantics { stateDescription = if (selected) "Selected" else "Not selected" },
        shape = shape, color = if (selected) c.accentFill else c.surface, border = BorderStroke(1.dp, if (selected) c.accentFill else c.outline)
    ) {
        Column(Modifier.clip(shape).clickable(enabled = enabled, role = Role.Button, onClick = onClick).heightIn(min = 72.dp).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val bar = if (selected) Color.White else c.accent.copy(alpha = if (enabled) 0.85f else 0.35f)
            Canvas(Modifier.fillMaxWidth().height(22.dp)) {
                val n = 10; val gap = size.width / n; val bw = gap * 0.45f
                for (i in 0 until n) {
                    val g = gains.getOrElse(i) { 0f }
                    val bh = size.height * (0.4f + 0.6f * ((g + 12f) / 24f))
                    drawRoundRect(bar, Offset(i * gap + (gap - bw) / 2f, (size.height - bh) / 2f), Size(bw, bh), CornerRadius(bw / 2f))
                }
            }
            Text(name, style = MaterialTheme.typography.labelLarge, color = if (selected) Color.White else if (enabled) c.textPrimary else c.textSecondary, maxLines = 2)
        }
    }
}

@Composable
private fun PresetGrid(title: String, items: List<Triple<String, List<Float>, Boolean>>, selectedName: String?, onPick: (Int) -> Unit) {
    Section(title)
    items.withIndex().toList().chunked(3).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { (idx, it) -> PresetTile(it.first, it.second, it.first == selectedName, it.third, { onPick(idx) }, Modifier.weight(1f)) }
            repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

/** Saves the current gains into Custom slot [n] (1..4). Replaces whatever was in that slot. */
fun AppViewModel.saveEqSlot(n: Int) {
    val id = "custom_slot_$n"
    val p = EqProfile(id, "Custom $n", eq.value.gains)
    val list = (settings.value.customEq.filterNot { it.id == id } + p).takeLast(50)
    eq.value = EqState(id, p.gains)
    viewModelScope.launch { store.setCustomEq(list); store.setCurrentEq(id, p.gains) }
}

/** Mood, Genres and Custom groups like the official Audio Presets page (our own values, no artist names). */
@Composable
fun EqPresetGroups(vm: AppViewModel) {
    val eq by vm.eq.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    val flat = EqDefaults.PRESETS.first()
    val currentName = (EqDefaults.ALL_BUILTIN + s.customEq).firstOrNull { it.id == eq.id }?.name

    val mood = listOf(flat to "Default") + EqDefaults.MOODS.map { it to it.name }
    PresetGrid("Mood", mood.map { (p, n) -> Triple(n, p.gains, true) }, currentName?.let { if (it == "Flat") "Default" else it }) { i -> vm.selectEq(mood[i].first) }

    val genres = EqDefaults.PRESETS.drop(1)
    PresetGrid("Genres", genres.map { Triple(it.name, it.gains, true) }, currentName) { i -> vm.selectEq(genres[i]) }

    val slots = (1..4).map { n -> s.customEq.firstOrNull { it.id == "custom_slot_$n" } }
    PresetGrid("Custom", slots.mapIndexed { i, p -> Triple("Custom ${i + 1}", p?.gains ?: EqDefaults.flat(), p != null) }, currentName) { i ->
        slots[i]?.let { vm.selectEq(it) }
    }
    Body("Save the current sound into a slot:")
    ChipRow(listOf("Custom 1", "Custom 2", "Custom 3", "Custom 4"), null) { vm.saveEqSlot(it.removePrefix("Custom ").toInt()) }
}
```

### `app/src/test/java/com/eclipsecompanion/app/ExtraTests2.kt`
```kotlin
package com.eclipsecompanion.app

import com.eclipsecompanion.app.audio.MaintMode
import com.eclipsecompanion.app.domain.EqDefaults
import com.eclipsecompanion.app.domain.Gesture
import com.eclipsecompanion.app.domain.Side
import com.eclipsecompanion.app.domain.duplicateActions
import com.eclipsecompanion.app.domain.touchKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtraTests2 {
    @Test fun bandFrequenciesMatchOfficialLayout() {
        assertEquals(listOf(32, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000), EqDefaults.FREQS)
    }

    @Test fun moodPresetsAreWellFormedAndIdsAreUniqueAcrossAllBuiltIns() {
        assertEquals(5, EqDefaults.MOODS.size)
        EqDefaults.MOODS.forEach { assertEquals(10, it.gains.size); assertTrue(it.gains.all { g -> g in -12f..12f }); assertTrue(it.builtIn) }
        assertEquals(EqDefaults.ALL_BUILTIN.size, EqDefaults.ALL_BUILTIN.map { it.id }.toSet().size)
        assertEquals(31, EqDefaults.ALL_BUILTIN.size)
    }

    @Test fun noActionIsIgnoredByDuplicateWarning() {
        val m = mapOf(
            touchKey(Side.LEFT, Gesture.TAP) to "No Action",
            touchKey(Side.LEFT, Gesture.DOUBLE) to "No Action",
            touchKey(Side.LEFT, Gesture.TRIPLE) to "ANC Switching"
        )
        assertTrue(duplicateActions(m, Side.LEFT).isEmpty())
    }

    @Test fun gestureLabelsMatchOfficialNames() {
        assertEquals("One Tap", Gesture.TAP.label); assertEquals("Two Tap", Gesture.DOUBLE.label)
        assertEquals("Three Tap", Gesture.TRIPLE.label); assertEquals("Tap & Hold", Gesture.LONG.label)
    }

    @Test fun maintenanceTonesStayShortAndCapped() {
        MaintMode.values().forEach { assertTrue(it.seconds in 1..60); assertTrue(it.amp in 0.01f..0.5f) }
        assertTrue(MaintMode.DUST.amp <= 0.25f)
    }
}
```
Total unit tests after v4.0: **35** (30 + 5).

## 5. More features added in v4.0 (all local, no new permissions)
1. **Clean Dust** and **Drain Moisture** (missing in v3.0): fixed 60 s / 30 s phone-made tone, 3 s fade-in, 2 s fade-out, level capped, Left/Right/Both, requires "earbuds out of ears" tick, earbuds route, no call; stops on Stop, call, focus loss, route loss, leaving the screen and app backgrounding.
2. **Official-style Devices screen** with CONNECT and Add Device, device header with L/R pills, tinted Touch/Sound cards and device hub rows.
3. **Pill navigation + round support button** (opens Help).
4. **Vertical EQ**, 63 Hz fix, **Mood / Genres / Custom** groups with mini-bar thumbnails, **4 Custom slots** (official has 2), fine-tune ± panel kept for accuracy and TalkBack (custom accessibility actions on every slider).
5. **Name Change** page that separates the phone alias from the firmware name.
6. **Offline Product details and Tips** pages replacing the official News/Products tabs (no network).
7. **Official mode names and order**, **official touch gesture and action names**, **image-style side picker**.
8. **Dark-first theme** with correct status/navigation bar icon colours in both themes.

Ideas intentionally left out (and why): copying real artist presets (names are real people, values unknown); guessing how the official Clean Dust/Drain Moisture tones work (this version is original and described honestly); News feed (would need internet).

## 6. Hearing Assist for the user's mother (kept, with improved guidance)
Unchanged engine. Additional wording rules: the Assist screen and Help say "assistive listening, not a medical hearing aid"; presets stay Conversation / TV / Quiet room / Restaurant / Outdoors; Simple mode now hides the Touch and Sound cards but keeps Hearing Assist on Home. Do not add a persistent "increase gain" nudge.

## 7. Doc text to add (file 05 files)

**HelpScreen + `docs/TROUBLESHOOTING.md` — add these three Q&A (identical text in both):**
- **What do Clean Dust and Drain Moisture do?** — "They play a fixed tone from your phone through the earbuds for 30 to 60 seconds. The vibration may help move water or dust out of the speaker opening. Take the earbuds out of your ears first. It is not guaranteed and does not replace gentle cleaning."
- **Why does the mode say Off instead of Normal?** — "The official app calls the no-processing mode Off. It is the same state: no noise cancelling and no transparency."
- **Why is there no case battery on Home?** — "The official app shows only left and right battery. The case value appears only if a verified profile reports it."

**`docs/OFFICIAL_FEATURE_PARITY.md` — replace the Hearing Assist row and add:**
| Hearing Assist | **Yes** (visible on the official device page, screenshot 30 Oct 2026) | Phone-mic path unverified | Local audio | Full engine with safeguards | Works |
| Relaxation | Yes (row on device page) | Unknown if phone or earbud audio | Local audio | 8 procedural sounds | Works (phone-side) |
| Clean Dust | Yes (row on device page) | How the official one works is unknown | Local audio | Fixed phone tone, capped | Works (original implementation) |
| Drain Moisture | Yes (row on device page) | Same | Local audio | Fixed phone tone, capped | Works (original implementation) |
| Name Change | Yes | Storage target unknown | Local + protocol | Alias now, firmware rename if verified | Alias works |
| Touch Settings | Yes: One Tap, Two Tap, Three Tap, Tap & Hold; actions include No Action and ANC Switching | Full action list unknown | Device protocol | Official names, drafts, review, apply | Drafts work; apply blocked |
| Audio Presets | Yes: 10 bands, Mood / Artists / Custom (2 slots) | Official preset values unknown | Protocol + local | Mood, Genres, 4 Custom slots | Local works; earbud write blocked |
| Case battery | Not shown (L and R only) | n/a | Device protocol | Case card shows Not available unless reported | Blocked: profile |
| Support button, News, Products | Yes | n/a | Online content | Support = offline Help; Products and Tips offline; News not reproduced | Works (offline substitutes) |
Also change the sentence about improvements: mode names follow the official app.

**`docs/SECURITY_REVIEW.md` — add under "Threat model":** "**Maintenance tones (Clean Dust, Drain Moisture)**: only play through a Bluetooth earbud route, never the phone speaker; fixed duration (30 s / 60 s), amplitude caps 0.5 and 0.25, 3 s fade-in, user must confirm earbuds are out of the ears, stop on call, focus loss, route loss, screen exit and app stop. No microphone is used."

**`docs/KNOWN_LIMITATIONS.md` — add:** "Clean Dust and Drain Moisture are original phone-side tones; their effectiveness is not proven and they are not the official app's implementation." / "Official artist presets are not reproduced." / "Pill navigation labels may be shortened with very large font scale; each tab still has an accessible name." / "Tone loudness depends on the phone's media volume, which the app cannot set."

**`docs/TEST_PLAN.md` — add the five test names from ExtraTests2 and these physical checks:** Clean Dust and Drain Moisture start only with the tick and an earbuds route, stop on Stop / call / leaving screen / home button, never play through the phone speaker; vertical EQ drag changes only its own band (also with TalkBack custom actions); Custom slot save, reload after app restart; pill bar at font scale 2.0; status-bar icons readable in dark and light theme; mode order Noise Reduction · Off · Transparency.

**`docs/SOURCE_AND_ASSUMPTION_REGISTER.md` — add:** "Official device page lists Touch Settings, Sound Settings, Name Change, Hearing Assist, Relaxation, Clean Dust, Drain Moisture | User screenshots of installed official app | Observed | 30 Oct 2026 | High | Features and layout | Yes" and "Official EQ is 10 bands 32 to 16k with Mood/Artists/Custom groups | Same screenshots | Observed | 30 Oct 2026 | High | EQ layout and groups | Yes" and "Official app shows only L and R battery | Same screenshots | Observed | 30 Oct 2026 | High | Case stays profile-gated | Yes" and "How official Clean Dust / Drain Moisture work | none | Assumption | n/a | Unknown | Own tones, honest wording | n/a".

## 8. Final self-check additions (answer yes/no in the final report)
- Dark theme is the default and `FREQS` contains 63, not 64.
- `ModeSelectorRow` is used in both Home and Sound; labels are Noise Reduction · Off · Transparency.
- `MaintenanceHub.stopAll()` is called in `onStop`; both maintenance modes refuse to start without an earbuds route and the tick.
- `VerticalEqPanel`, `EqPresetGroups`, `PillNavBar`, `DevicesHero`, `DeviceHeader`, `DeviceHub`, `SidePicker`, `NameScreen`, `MaintenanceScreen`, `ProductsScreen`, `TipsScreen` all exist and are referenced.
- 35 unit tests exist; docs updated per section 7; no `INTERNET`.
