# File 04 — v3.0 extras: system monitor, EQ curve, welcome tour, About screen, extra tests

New files only. Package `com.eclipsecompanion.app`. They are referenced by the patched files 01–02D (`SystemMonitor` from `AppViewModel`; `EqCurve`, `OnboardingScreen`, `AboutScreen` from `Screens.kt`). Create every file below **verbatim**; fix only real compile errors and list them in `docs/CHANGES_FROM_PROMPT.md`.

---

## 1. Bluetooth + audio-route monitor

Purpose: show whether Bluetooth is on, and which Android **audio** route is active, separately from the app's **control** link. (Earbuds can play music while the proprietary control channel is unavailable; the UI must say so.)

### `app/src/main/java/com/eclipsecompanion/app/bluetooth/SystemMonitor.kt`
```kotlin
package com.eclipsecompanion.app.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.eclipsecompanion.app.audio.HearingAssistHub
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Observes (read-only) the phone's Bluetooth on/off state and the active Bluetooth audio output.
 * It never connects, scans or writes anything. Started/stopped by the ViewModel.
 */
class SystemMonitor(private val ctx: Context) {
    private val app = ctx.applicationContext
    private val am = app.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    val btOn = MutableStateFlow(readBluetooth())
    val audioRoute = MutableStateFlow(readRoute())

    private var started = false

    private val stateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) { btOn.value = readBluetooth(); audioRoute.value = readRoute() }
    }

    private val deviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>) { audioRoute.value = readRoute() }
        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>) { audioRoute.value = readRoute() }
    }

    private fun readBluetooth(): Boolean =
        app.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true

    private fun readRoute(): String = HearingAssistHub.routeName(app)

    @Synchronized
    fun start() {
        if (started) return
        started = true
        btOn.value = readBluetooth(); audioRoute.value = readRoute()
        ContextCompat.registerReceiver(app, stateReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED), ContextCompat.RECEIVER_NOT_EXPORTED)
        am.registerAudioDeviceCallback(deviceCallback, Handler(Looper.getMainLooper()))
    }

    @Synchronized
    fun stop() {
        if (!started) return
        started = false
        runCatching { app.unregisterReceiver(stateReceiver) }
        runCatching { am.unregisterAudioDeviceCallback(deviceCallback) }
    }
}
```

---

## 2. Extras UI: EQ curve, welcome tour, About screen

### `app/src/main/java/com/eclipsecompanion/app/ui/Extras.kt`
```kotlin
package com.eclipsecompanion.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.eclipsecompanion.app.APP_VERSION
import com.eclipsecompanion.app.AppViewModel
import com.eclipsecompanion.app.Host
import com.eclipsecompanion.app.audio.interpolateGainDb
import com.eclipsecompanion.app.domain.EqDefaults
import kotlin.math.ln
import kotlin.math.pow

/**
 * Frequency-response preview of the 10 band gains (20 Hz – 20 kHz, ±12 dB). Purely visual: it draws the
 * curve the sliders describe, it does not claim to measure anything.
 */
@Composable
fun EqCurve(gains: List<Float>, modifier: Modifier = Modifier) {
    val c = E.c
    val desc = "Equalizer curve. " + EqDefaults.FREQS.mapIndexed { i, f ->
        "$f hertz ${"%+.1f".format(gains.getOrElse(i) { 0f })} decibels"
    }.joinToString(", ")
    Canvas(modifier.fillMaxWidth().height(120.dp).semantics { contentDescription = desc }) {
        val w = size.width
        val h = size.height
        fun yOf(db: Float) = h / 2f - (db.coerceIn(-12f, 12f) / 12f) * (h / 2f)
        val fMin = 20f
        val fMax = 20000f
        fun xOf(hz: Float) = (ln(hz / fMin) / ln(fMax / fMin)) * w

        for (db in listOf(-12f, -6f, 0f, 6f, 12f)) {
            drawLine(c.outline, Offset(0f, yOf(db)), Offset(w, yOf(db)), strokeWidth = if (db == 0f) 3f else 1f)
        }
        val path = Path()
        val steps = 80
        for (i in 0..steps) {
            val hz = fMin * (fMax / fMin).pow(i.toFloat() / steps)
            val px = xOf(hz)
            val py = yOf(interpolateGainDb(gains, hz))
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
        }
        drawPath(path, c.accent, style = Stroke(width = 5f))
        EqDefaults.FREQS.forEachIndexed { i, f ->
            drawCircle(c.accent, 6f, Offset(xOf(f.toFloat()), yOf(gains.getOrElse(i) { 0f })))
        }
    }
}

/* ------------------------------------------------------------------ Welcome tour (first run) */

/**
 * Four short pages: what the app is, what it needs, what works now vs what needs a verified profile, finish.
 * No scan, no connection and no permission prompt happens unless the person taps the matching button.
 */
@Composable
fun OnboardingScreen(vm: AppViewModel, host: Host) {
    var page by rememberSaveable { mutableStateOf(0) }
    val last = 3
    ScreenColumn {
        EclipseIllustration()
        Title("Eclipse Companion")
        when (page) {
            0 -> {
                Section("Made for your Ronin Eclipse")
                Body("An independent, offline companion for your earbuds. No account, no sign-in, no internet permission, no ads, no analytics.")
                Body("It is not an official Ronin product and is not affiliated with or endorsed by Ronin.")
            }
            1 -> {
                Section("Nearby devices permission")
                Body("Android calls the permission needed to find and talk to your earbuds “Nearby devices”. The app does not use your location.")
                Body("Hearing Assist asks for the microphone only when you tap Start, and never records or stores sound.")
                SecondaryButton("Allow Nearby devices now", { host.requestBluetooth { host.toast("Nearby devices allowed.") } }, Modifier.fillMaxWidth())
            }
            2 -> {
                Section("What works today")
                Body("Hearing Assist, the system equalizer for supporting music players, 26 EQ presets and your own profiles, relaxation sounds, channel test, touch-control drafts, help and diagnostics.")
                Section("What needs a verified profile")
                Body("Switching ANC / Transparency / Normal, per-earbud and case battery, earbud EQ, touch gestures on the earbuds, Gaming Mode, rename and reset. The control commands of the earbuds are not public, so the app never guesses them. Until you import a verified profile these controls stay switched off and say why.")
            }
            else -> {
                Section("Ready")
                Body("Open the earbuds’ case, then tap Connect earbuds on the Home screen. Family members can switch on Simple mode in Settings.")
                Body("Hearing Assist is an assistive listening tool, not a medical hearing aid.")
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (page > 0) SecondaryButton("Back", { page -= 1 }, Modifier.weight(1f))
            if (page < last) PrimaryButton("Next", { page += 1 }, Modifier.weight(1f))
            else PrimaryButton("Get started", { vm.setFirstRunDone(true) }, Modifier.weight(1f))
        }
        if (page < last) SecondaryButton("Skip", { vm.setFirstRunDone(true) }, Modifier.fillMaxWidth())
        Body("Step ${page + 1} of ${last + 1}")
    }
}

/* ------------------------------------------------------------------ About + licences */

@Composable
fun AboutScreen(onBack: () -> Unit) {
    SubScreen("About and licences", onBack) {
        ECard {
            Section("Eclipse Companion $APP_VERSION")
            Body("Independent project for the Ronin Eclipse R-7065. Not affiliated with or endorsed by Ronin. “Ronin” and “Eclipse” belong to their owners and are used only to say which earbuds this app is made for.")
            Body("Hearing Assist is not a medical device and does not replace a hearing assessment.")
        }
        ECard {
            Section("Open-source components")
            Body("Kotlin and kotlinx.coroutines: Apache License 2.0.")
            Body("AndroidX (Core, Activity, Lifecycle, DataStore) and Jetpack Compose with Material 3 and Material Icons: Apache License 2.0.")
            Body("Test only, not shipped in the app: JUnit 4 (Eclipse Public License 1.0) and org.json (JSON license).")
            Body("The launcher icon, favicon and illustration are original artwork made for this project.")
        }
        ECard {
            Section("Network and data")
            Body("This app has no internet permission, no account, no analytics, no ads, no remote fonts or images, and no dynamic code loading. See Privacy for exactly what is stored on the phone.")
        }
    }
}
```

---

## 3. Extra unit tests

### `app/src/test/java/com/eclipsecompanion/app/ExtraTests.kt`
```kotlin
package com.eclipsecompanion.app

import com.eclipsecompanion.app.audio.MildCompressor
import com.eclipsecompanion.app.domain.EqDefaults
import com.eclipsecompanion.app.domain.Gesture
import com.eclipsecompanion.app.domain.Side
import com.eclipsecompanion.app.domain.compatibilityLabel
import com.eclipsecompanion.app.domain.duplicateActions
import com.eclipsecompanion.app.domain.touchKey
import com.eclipsecompanion.app.protocol.FrameCodec
import com.eclipsecompanion.app.protocol.Framing
import com.eclipsecompanion.app.protocol.Hex
import com.eclipsecompanion.app.protocol.ProtocolProfile
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtraTests {

    /* ---------- EQ regression (official-app bug: sliders moved neighbours) ---------- */

    @Test fun movingOneBandRepeatedlyNeverTouchesOthers() {
        var g = EqDefaults.PRESETS.first { it.id == "builtin_rock" }.gains
        val before = g
        g = EqDefaults.withBand(g, 3, 7.5f)
        g = EqDefaults.withBand(g, 3, -2f)
        for (i in g.indices) if (i != 3) assertEquals(before[i], g[i], 0f)
        assertEquals(-2f, g[3], 0f)
    }

    @Test fun sanitizeRejectsWrongSizeAndNaN() {
        assertNull(EqDefaults.sanitize(listOf(0f, 1f)))
        assertNull(EqDefaults.sanitize(List(10) { if (it == 2) Float.NaN else 0f }))
        assertNotNull(EqDefaults.sanitize(List(10) { 20f }))
        assertEquals(12f, EqDefaults.sanitize(List(10) { 20f })!![0], 0f)
    }

    @Test fun headroomEqualsMinusLargestBoost() {
        assertEquals(0f, EqDefaults.headroomDb(EqDefaults.flat()), 0f)
        assertEquals(-6f, EqDefaults.headroomDb(listOf(6f, 5f, 4f, 2f, 0f, 0f, 0f, 0f, 0f, 0f)), 0f)
        assertEquals(0f, EqDefaults.headroomDb(List(10) { -3f }), 0f)       // cuts only: no extra attenuation needed
    }

    @Test fun presetIdsAndNamesAreUniqueAndFlatIsFirst() {
        assertEquals(26, EqDefaults.PRESETS.map { it.name }.toSet().size)
        assertTrue(EqDefaults.isFlat(EqDefaults.PRESETS.first().gains))
        assertTrue(EqDefaults.PRESETS.all { it.builtIn })
    }

    /* ---------- Touch conflict warning ---------- */

    @Test fun duplicateActionsIgnoreDisabledAndOtherSide() {
        val m = mapOf(
            touchKey(Side.LEFT, Gesture.TAP) to "Play/Pause",
            touchKey(Side.LEFT, Gesture.DOUBLE) to "Play/Pause",
            touchKey(Side.LEFT, Gesture.TRIPLE) to "Disabled",
            touchKey(Side.LEFT, Gesture.LONG) to "Disabled",
            touchKey(Side.RIGHT, Gesture.TAP) to "Next track"
        )
        assertEquals(listOf("Play/Pause"), duplicateActions(m, Side.LEFT))
        assertTrue(duplicateActions(m, Side.RIGHT).isEmpty())
    }

    /* ---------- Compatibility wording never over-claims ---------- */

    @Test fun compatibilityLabelIsConservative() {
        assertTrue(compatibilityLabel(false, true, setOf("setMode")).startsWith("No compatible"))
        assertTrue(compatibilityLabel(true, false, setOf("setMode")).contains("unverified"))
        assertTrue(compatibilityLabel(true, true, emptySet()).contains("unverified"))
        assertTrue(compatibilityLabel(true, true, setOf("readBattery")).startsWith("Partially"))
        assertTrue(compatibilityLabel(true, true, setOf("readBattery", "setMode")).startsWith("Compatible"))
    }

    /* ---------- Frame codec edge cases ---------- */

    private val noChecksum = Framing(byteArrayOf(0x55), false, "none", 0)
    private val crcFraming = Framing(byteArrayOf(0xAA.toByte(), 0x55), true, "crc16modbus", 2)

    @Test fun asciiTokenInsertsTextAndRejectsNonAscii() {
        val ok = FrameCodec.build(noChecksum, "10 {name*}", emptyMap(), mapOf("name" to "AB"))
        assertArrayEquals(Hex.parse("55 10 41 42"), ok)
        assertNull(FrameCodec.build(noChecksum, "10 {name*}", emptyMap(), mapOf("name" to "Aé")))
        assertNull(FrameCodec.build(noChecksum, "10 {name*}", emptyMap(), emptyMap()))
    }

    @Test fun crcFrameRoundTrips() {
        val frame = FrameCodec.build(crcFraming, "01 {mode}", mapOf("mode" to 2), emptyMap())!!
        assertEquals(2 + 1 + 2 + 2, frame.size)                              // header + length + body + crc16
        assertArrayEquals(Hex.parse("01 02"), FrameCodec.parse(crcFraming, frame))
        val bad = frame.copyOf(); bad[bad.size - 1] = (bad[bad.size - 1].toInt() xor 0x01).toByte()
        assertNull(FrameCodec.parse(crcFraming, bad))
    }

    @Test fun lengthByteFramesOver255BytesAreRefused() {
        val f = Framing(byteArrayOf(0xAA.toByte()), true, "none", 0)
        assertNull(FrameCodec.build(f, "{name*}", emptyMap(), mapOf("name" to "x".repeat(256))))
    }

    @Test fun hexParserHandlesSeparatorsAndRejectsOddLength() {
        assertArrayEquals(byteArrayOf(0x0A, 0x1B, 0x2C), Hex.parse("0a:1B-2c"))
        assertNull(Hex.parse("ABC"))
        assertNull(Hex.parse("ZZ"))
    }

    /* ---------- Protocol profile validation ---------- */

    @Test fun profileAcceptsNewOptionalCommandsAndRanges() {
        val p = ProtocolProfile.fromJson(
            """{"schema":1,"name":"t","ancLevel":{"min":0,"max":3},
               "commands":{"setAncLevel":{"request":"20 {level}","verified":true},
                           "setWearDetection":{"request":"21 {on}","verified":false},
                           "setVoicePrompts":{"request":"22 {on}","verified":false}}}"""
        ).getOrThrow()
        assertTrue(p.isVerified("setAncLevel"))
        assertFalse(p.isVerified("setWearDetection"))
        assertEquals(3, p.ancLevel!!.max)
    }

    @Test fun profileRejectsBadRangesUuidsAndHex() {
        assertTrue(ProtocolProfile.fromJson("""{"schema":1,"ancLevel":{"min":5,"max":1}}""").isFailure)
        assertTrue(ProtocolProfile.fromJson("""{"schema":1,"transparencyLevel":{"min":0,"max":999}}""").isFailure)
        assertTrue(ProtocolProfile.fromJson("""{"schema":1,"transport":{"type":"gatt","write":"not-a-uuid"}}""").isFailure)
        assertTrue(ProtocolProfile.fromJson("""{"schema":1,"framing":{"header":"ZZ"}}""").isFailure)
        assertTrue(ProtocolProfile.fromJson("""{"schema":1,"transport":{"type":"bluetooth"}}""").isFailure)
    }

    @Test fun oversizedProfileIsRejected() {
        val big = "{\"schema\":1,\"notes\":\"" + "x".repeat(ProtocolProfile.MAX_JSON_CHARS) + "\"}"
        assertTrue(ProtocolProfile.fromJson(big).isFailure)
    }

    @Test fun bundledTemplateShapeHasNothingVerified() {
        val template = """{"schema":1,"name":"t","verified":false,
            "transport":{"type":"gatt","service":"","write":"","notify":"","writeNoResponse":false,"sppUuid":"00001101-0000-1000-8000-00805F9B34FB"},
            "framing":{"header":"","lengthByte":false,"checksum":"none","checksumFrom":0},
            "modes":{},"commands":{"readBattery":{"request":"","ack":null,"verified":false},"setMode":{"request":"","ack":null,"verified":false}}}"""
        val p = ProtocolProfile.fromJson(template).getOrThrow()
        assertFalse(p.transportConfigured)
        assertTrue(p.commands.values.none { it.verified })
    }

    /* ---------- Hearing Assist comfort compressor ---------- */

    @Test fun compressorLeavesQuietSignalAndTamesLoudOne() {
        val quiet = MildCompressor(48_000)
        var q = 0f
        repeat(4_000) { q = quiet.process(0.1f) }
        assertEquals(0.1f, q, 0.001f)

        val loud = MildCompressor(48_000)
        var l = 0f
        repeat(4_000) { l = loud.process(0.9f) }
        assertTrue("loud signal should be reduced, was $l", l in 0.4f..0.6f)
    }
}
```
