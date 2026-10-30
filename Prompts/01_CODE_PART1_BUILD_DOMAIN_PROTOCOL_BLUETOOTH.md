# File 01 — Build files, resources, domain, protocol engine, Bluetooth (v3.0)

Create each file at the exact path shown, with the exact content. Package: `com.eclipsecompanion.app`. Assets (icons, JSON profile) are in file 03. UI/audio/data code is in files 02/02B/02C/02D. New v3.0 extras (system monitor, onboarding, EQ curve, About screen, extra tests) are in file 04.

> Dependency versions below were stable at writing time. If Gradle sync fails, upgrade to the nearest compatible stable versions (AGP ↔ Kotlin ↔ Compose BOM) and record it in `docs/CHANGES_FROM_PROMPT.md`.

---

## 1. Gradle

### `settings.gradle.kts`
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "EclipseCompanion"
include(":app")
```

### `build.gradle.kts` (root)
```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
```

### `gradle.properties`
```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.nonTransitiveRClass=true
kotlin.code.style=official
```

### `app/build.gradle.kts`
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.eclipsecompanion.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.eclipsecompanion.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    testOptions { unitTests.isReturnDefaultValues = true }
    packaging { resources.excludes += "/META-INF/{AL2.0,LGPL2.1}" }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}
```

### `app/proguard-rules.pro`
```
# Keep audio effect receiver and service entry points
-keep class com.eclipsecompanion.app.audio.EffectSessionReceiver { *; }
-keep class com.eclipsecompanion.app.audio.HearingAssistService { *; }
```

---

## 2. Manifest and resources

### `app/src/main/AndroidManifest.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-feature android:name="android.hardware.bluetooth_le" android:required="false" />
    <uses-feature android:name="android.hardware.bluetooth" android:required="false" />
    <uses-feature android:name="android.hardware.microphone" android:required="false" />

    <!-- Bluetooth (API 31+) -->
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN"
        android:usesPermissionFlags="neverForLocation" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <!-- Legacy Bluetooth (API 30 and below) -->
    <uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="30" />

    <!-- Hearing Assist: requested only when the user taps Start -->
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- System equalizer / audio effects -->
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

    <!-- NO INTERNET PERMISSION BY DESIGN -->

    <application
        android:allowBackup="false"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.EclipseCompanion"
        android:usesCleartextTraffic="false">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize"
            android:launchMode="singleTop"
            android:configChanges="orientation|screenSize|smallestScreenSize|screenLayout|keyboardHidden|uiMode|fontScale|density">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            <!-- Lets music players open this app from their "Equalizer" button -->
            <intent-filter>
                <action android:name="android.media.action.DISPLAY_AUDIO_EFFECT_CONTROL_PANEL" />
                <category android:name="android.intent.category.DEFAULT" />
            </intent-filter>
            <!-- Long-press launcher shortcuts: Hearing Assist, Equalizer, Relaxation (res/xml/shortcuts.xml, file 03) -->
            <meta-data android:name="android.app.shortcuts" android:resource="@xml/shortcuts" />
        </activity>

        <service
            android:name=".audio.HearingAssistService"
            android:exported="false"
            android:foregroundServiceType="microphone" />

        <receiver
            android:name=".audio.EffectSessionReceiver"
            android:exported="true">
            <intent-filter>
                <action android:name="android.media.action.OPEN_AUDIO_EFFECT_CONTROL_SESSION" />
                <action android:name="android.media.action.CLOSE_AUDIO_EFFECT_CONTROL_SESSION" />
            </intent-filter>
        </receiver>
    </application>
</manifest>
```

### `app/src/main/res/values/strings.xml`
```xml
<resources>
    <string name="app_name">Eclipse Companion</string>
    <string name="notif_channel_assist">Hearing Assist session</string>
    <string name="shortcut_assist_short">Hearing Assist</string>
    <string name="shortcut_assist_long">Open Hearing Assist</string>
    <string name="shortcut_eq_short">Equalizer</string>
    <string name="shortcut_eq_long">Open the equalizer</string>
    <string name="shortcut_relax_short">Relax</string>
    <string name="shortcut_relax_long">Open relaxation sounds</string>
</resources>
```

### `app/src/main/res/values/themes.xml`
```xml
<resources>
    <style name="Theme.EclipseCompanion" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:windowBackground">#F6F6F4</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLayoutInDisplayCutoutMode">shortEdges</item>
    </style>
</resources>
```

### `app/src/main/res/values-night/themes.xml`
```xml
<resources>
    <style name="Theme.EclipseCompanion" parent="android:Theme.Material.NoActionBar">
        <item name="android:windowBackground">#121314</item>
        <item name="android:statusBarColor">@android:color/transparent</item>
        <item name="android:navigationBarColor">@android:color/transparent</item>
        <item name="android:windowLayoutInDisplayCutoutMode">shortEdges</item>
    </style>
</resources>
```

---

## 3. Domain

### `app/src/main/java/com/eclipsecompanion/app/domain/Models.kt`
```kotlin
package com.eclipsecompanion.app.domain

import kotlin.math.abs

/** A value reported by the device (or unavailable). Unknown is NEVER represented as 0. */
sealed interface Reported<out T> {
    data class Available<T>(val value: T, val atMillis: Long) : Reported<T>
    data class Unavailable(val reason: String) : Reported<Nothing>
}

fun <T> Reported<T>.valueOrNull(): T? = (this as? Reported.Available<T>)?.value

enum class ListeningMode(val label: String, val key: String) {
    ANC("ANC", "ANC"),
    TRANSPARENCY("Transparency", "TRANSPARENCY"),
    NORMAL("Normal", "NORMAL")
}

enum class Phase { IDLE, SCANNING, CONNECTING, DISCOVERING, READY, DISCONNECTED, ERROR }

data class BatteryState(
    val left: Reported<Int>,
    val right: Reported<Int>,
    val case: Reported<Int>,
    /** Single value from the standard BLE Battery Service (not per-earbud). */
    val combined: Reported<Int>
) {
    companion object {
        fun unavailable(reason: String): BatteryState {
            val u = Reported.Unavailable(reason)
            return BatteryState(u, u, u, u)
        }
    }
}

/** Merge a fresh report into the old state: unavailable fields never erase a known (now stale) value. */
fun mergeBattery(old: BatteryState, new: BatteryState): BatteryState {
    fun m(o: Reported<Int>, n: Reported<Int>) = if (n is Reported.Available) n else if (o is Reported.Available) o else n
    return BatteryState(m(old.left, new.left), m(old.right, new.right), m(old.case, new.case), m(old.combined, new.combined))
}

fun validBattery(v: Int?): Boolean = v != null && v in 0..100

sealed interface CommandResult {
    data class Confirmed(val text: String) : CommandResult
    data class SentUnconfirmed(val text: String) : CommandResult
    data class Rejected(val reason: String) : CommandResult
    data class Unsupported(val reason: String) : CommandResult
}

/* ---------------------------------------------------------------- EQ */

data class EqProfile(
    val id: String,
    val name: String,
    val gains: List<Float>,
    val builtIn: Boolean = false
)

object EqDefaults {
    val FREQS = listOf(32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
    const val MIN_DB = -12f
    const val MAX_DB = 12f
    const val STEP_DB = 0.5f
    val BAND_COUNT get() = FREQS.size

    fun flat(): List<Float> = List(FREQS.size) { 0f }

    /** Returns a NEW list where only [index] differs. Never touches neighbours. */
    fun withBand(gains: List<Float>, index: Int, value: Float): List<Float> {
        require(index in gains.indices) { "Band index out of range" }
        require(value.isFinite()) { "Gain must be finite" }
        val out = gains.toMutableList()
        out[index] = value.coerceIn(MIN_DB, MAX_DB)
        return out
    }

    fun sanitize(gains: List<Float>): List<Float>? =
        if (gains.size == FREQS.size && gains.all { it.isFinite() }) gains.map { it.coerceIn(MIN_DB, MAX_DB) } else null

    /** Largest positive boost; used for a clipping warning (>= 6 dB is risky without headroom). */
    fun peakBoostDb(gains: List<Float>): Float = (gains.maxOrNull() ?: 0f).coerceAtLeast(0f)

    /** Cut (in dB, <= 0) applied to the whole curve so no band ends up boosted above 0 dB. Prevents digital clipping. */
    fun headroomDb(gains: List<Float>): Float = 0f - peakBoostDb(gains)

    fun isFlat(gains: List<Float>) = gains.all { abs(it) < 0.01f }

    private fun p(id: String, name: String, vararg g: Float) =
        EqProfile("builtin_$id", name, g.toList(), builtIn = true)

    //                                       32  64 125 250 500  1k  2k  4k  8k 16k
    val PRESETS: List<EqProfile> = listOf(
        p("flat", "Flat", 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
        p("bass_boost", "Bass Boost", 6f, 5f, 4f, 2f, 0f, 0f, 0f, 0f, 0f, 0f),
        p("bass_light", "Bass Light", 3f, 3f, 2f, 1f, 0f, 0f, 0f, 0f, 0f, 0f),
        p("treble", "Treble Boost", 0f, 0f, 0f, 0f, 0f, 1f, 2f, 4f, 5f, 6f),
        p("vocal", "Vocal Clarity", -2f, -2f, -1f, 0f, 2f, 3f, 3f, 2f, 0f, -1f),
        p("podcast", "Podcast", -4f, -3f, -1f, 1f, 3f, 4f, 3f, 1f, -1f, -3f),
        p("acoustic", "Acoustic", 3f, 3f, 2f, 1f, 1f, 1f, 2f, 3f, 3f, 2f),
        p("classical", "Classical", 3f, 2f, 1f, 0f, 0f, 0f, -1f, -1f, 2f, 3f),
        p("jazz", "Jazz", 2f, 1f, 0f, 1f, -1f, -1f, 0f, 1f, 2f, 3f),
        p("rock", "Rock", 4f, 3f, 2f, 0f, -1f, -1f, 1f, 3f, 4f, 4f),
        p("pop", "Pop", -1f, 1f, 3f, 3f, 1f, 0f, -1f, -1f, 1f, 2f),
        p("electronic", "Electronic", 4f, 3f, 1f, 0f, -2f, 1f, 0f, 2f, 4f, 4f),
        p("hiphop", "Hip-Hop", 5f, 4f, 2f, 2f, -1f, -1f, 1f, 0f, 2f, 3f),
        p("rnb", "R&B", 3f, 6f, 5f, 2f, -2f, -1f, 2f, 2f, 3f, 3f),
        p("dance", "Dance", 5f, 4f, 2f, 0f, 0f, -2f, -1f, 0f, 3f, 2f),
        p("late_night", "Late Night", 2f, 2f, 1f, 0f, 0f, -1f, -2f, -3f, -4f, -4f),
        p("speech", "Speech", -6f, -4f, -2f, 1f, 3f, 4f, 4f, 2f, 0f, -2f),
        p("lounge", "Lounge", -2f, -1f, 0f, 1f, 2f, 2f, 1f, 0f, 1f, 2f),
        p("bass_reducer", "Bass Reducer", -6f, -5f, -4f, -2f, 0f, 0f, 0f, 0f, 0f, 0f),
        p("warm", "Warm", 2f, 2f, 2f, 1f, 0f, -1f, -1f, -2f, -2f, -3f),
        p("bright", "Bright", -2f, -2f, -1f, 0f, 0f, 1f, 2f, 3f, 3f, 4f),
        p("soften_treble", "Soften Treble", 0f, 0f, 0f, 0f, 0f, 0f, -1f, -2f, -3f, -4f),
        p("movie", "Movie", 4f, 4f, 2f, 0f, -1f, 0f, 2f, 3f, 3f, 2f),
        p("gaming", "Gaming (footsteps)", -2f, -2f, -1f, 0f, 1f, 2f, 3f, 4f, 3f, 1f),
        p("workout", "Workout", 5f, 4f, 2f, 0f, 0f, 1f, 2f, 3f, 3f, 2f),
        p("loudness", "Low-volume Loudness", 5f, 4f, 2f, 0f, -1f, -1f, 0f, 2f, 4f, 3f)
    )
}

/* ---------------------------------------------------------------- Touch */

enum class Side(val label: String) { LEFT("Left earbud"), RIGHT("Right earbud") }
enum class Gesture(val label: String) {
    TAP("Single tap"), DOUBLE("Double tap"), TRIPLE("Triple tap"), LONG("Long press")
}

/** Key format "LEFT:TAP" -> action name (must be one of the profile's touch.actions keys). */
typealias TouchMap = Map<String, String>

fun touchKey(side: Side, gesture: Gesture) = "${side.name}:${gesture.name}"

/** Actions assigned to more than one gesture of one earbud (ignores "Disabled"). Used for the conflict warning. */
fun duplicateActions(map: Map<String, String>, side: Side): List<String> =
    Gesture.values().mapNotNull { map[touchKey(side, it)] }
        .filter { it != "Disabled" }
        .groupingBy { it }.eachCount()
        .filter { it.value > 1 }.keys.toList()

/** Plain-language descriptions of the generic actions (shown in the Controls screen). */
val TOUCH_ACTION_HELP: Map<String, String> = mapOf(
    "Play/Pause" to "Starts or pauses the current audio.",
    "Next track" to "Skips to the next track.",
    "Previous track" to "Goes back to the previous track.",
    "Volume up" to "Raises the media volume one step.",
    "Volume down" to "Lowers the media volume one step.",
    "Answer/End call" to "Answers an incoming call, or ends the current call.",
    "Reject call" to "Declines an incoming call.",
    "Cycle ANC/Transparency/Normal" to "Steps through the three listening modes.",
    "Voice assistant" to "Starts your phone's voice assistant.",
    "Gaming mode" to "Toggles low-latency mode (if the earbuds support it).",
    "Disabled" to "Does nothing."
)

/** Human-readable result of the first-run / diagnostics compatibility check (never claims more than the profile proves). */
fun compatibilityLabel(connected: Boolean, transportConfigured: Boolean, verified: Set<String>): String = when {
    !connected -> "No compatible device connected."
    !transportConfigured -> "Device found but protocol unverified (no control channel configured)."
    verified.isEmpty() -> "Device found but protocol unverified (no command marked verified)."
    "setMode" in verified && "readBattery" in verified -> "Compatible: core controls verified (${verified.sorted().joinToString()})."
    else -> "Partially compatible: ${verified.sorted().joinToString()}."
}

/** Generic action names shown when the profile has no verified action list (local drafts only). */
val GENERIC_TOUCH_ACTIONS = listOf(
    "Play/Pause", "Next track", "Previous track", "Volume up", "Volume down",
    "Answer/End call", "Reject call", "Cycle ANC/Transparency/Normal", "Voice assistant", "Gaming mode", "Disabled"
)

/* ---------------------------------------------------------------- Hearing Assist settings */

enum class SpeechClarity(val label: String) { OFF("Off"), MILD("Mild"), MODERATE("Moderate") }

data class HearingSettings(
    val inputGain: Float = 1.0f,          // 0.5 .. 4.0 (linear)
    val outputGain: Float = 0.15f,        // 0.0 .. 0.6 (linear) — starts LOW every session
    val clarity: SpeechClarity = SpeechClarity.OFF,
    val rumbleCut: Boolean = true
) {
    fun clamped() = copy(
        inputGain = inputGain.coerceIn(INPUT_MIN, INPUT_MAX),
        outputGain = outputGain.coerceIn(0f, OUTPUT_MAX)
    )
    companion object {
        const val INPUT_MIN = 0.5f
        const val INPUT_MAX = 4.0f
        const val OUTPUT_MAX = 0.6f
        const val SAFE_OUTPUT_START = 0.15f
    }
}
```

---

## 4. Protocol engine

### `app/src/main/java/com/eclipsecompanion/app/protocol/ProtocolProfile.kt`
```kotlin
package com.eclipsecompanion.app.protocol

import org.json.JSONObject

data class Identify(val namePrefixes: List<String>, val serviceUuids: List<String>)

data class TransportCfg(
    val type: String,               // "gatt" or "spp"
    val service: String,
    val write: String,
    val notify: String,
    val writeNoResponse: Boolean,
    val sppUuid: String
)

class Framing(val header: ByteArray, val lengthByte: Boolean, val checksum: String, val checksumFrom: Int)

/** [request] is a template: hex bytes and tokens {name} (1 byte) or {name*} (ASCII string). */
data class Command(val request: String, val ack: String?, val verified: Boolean)

data class BatteryParse(val prefix: String, val left: Int?, val right: Int?, val case: Int?, val invalid: List<Int>)
data class ModeParse(val prefix: String, val index: Int, val map: Map<String, Int>)
data class FirmwareParse(val prefix: String, val index: Int)
data class EqCfg(val bands: Int, val offset: Int, val stepDb: Float)
data class TouchCfg(val sides: Map<String, Int>, val gestures: Map<String, Int>, val actions: Map<String, Int>)
data class LevelCfg(val min: Int, val max: Int)

data class ProtocolProfile(
    val name: String,
    val verified: Boolean,
    val notes: String,
    val identify: Identify,
    val transport: TransportCfg,
    val framing: Framing,
    val modes: Map<String, Int>,
    val commands: Map<String, Command>,
    val battery: BatteryParse?,
    val mode: ModeParse?,
    val firmware: FirmwareParse?,
    val eq: EqCfg?,
    val touch: TouchCfg?,
    val transparency: LevelCfg?,
    val ancLevel: LevelCfg? = null
) {
    val transportConfigured: Boolean
        get() = when (transport.type) {
            "spp" -> transport.sppUuid.isNotEmpty()
            else -> transport.write.isNotEmpty()
        }

    fun isVerified(command: String) = commands[command]?.verified == true

    companion object {
        val ALLOWED_COMMANDS = setOf(
            "readBattery", "readMode", "setMode", "readFirmware", "setEq", "setTouch",
            "setGaming", "setName", "setTransparencyLevel", "factoryReset",
            "setAncLevel", "setWearDetection", "setVoicePrompts"
        )
        private val UUID_RE = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        const val MAX_JSON_CHARS = 100_000

        val EMPTY = ProtocolProfile(
            name = "No profile", verified = false, notes = "",
            identify = Identify(listOf("Eclipse", "Ronin"), emptyList()),
            transport = TransportCfg("gatt", "", "", "", false, "00001101-0000-1000-8000-00805F9B34FB"),
            framing = Framing(ByteArray(0), false, "none", 0),
            modes = emptyMap(), commands = emptyMap(),
            battery = null, mode = null, firmware = null, eq = null, touch = null, transparency = null
        )

        fun fromJson(text: String): Result<ProtocolProfile> = try {
            require(text.length <= MAX_JSON_CHARS) { "Profile too large" }
            Result.success(parse(JSONObject(text)))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Invalid profile: ${e.message}"))
        }

        private fun optInt(o: JSONObject, k: String): Int? =
            if (o.has(k) && !o.isNull(k)) o.getInt(k).also { require(it in 0..4096) { "$k out of range" } } else null

        private fun uuidOrEmpty(s: String): String {
            if (s.isEmpty()) return s
            require(UUID_RE.matches(s)) { "Bad UUID: $s" }
            return s
        }

        private fun hexOrEmpty(s: String): String {
            if (s.isNotBlank()) requireNotNull(Hex.parse(s)) { "Bad hex: $s" }
            return s
        }

        private fun intMap(o: JSONObject?): Map<String, Int> {
            if (o == null) return emptyMap()
            val m = LinkedHashMap<String, Int>()
            for (k in o.keys()) {
                val v = o.getInt(k)
                require(v in 0..255) { "Value for $k must be 0..255" }
                m[k] = v
            }
            return m
        }

        private fun parse(o: JSONObject): ProtocolProfile {
            require(o.optInt("schema") == 1) { "Unsupported schema (expected 1)" }

            val idn = o.optJSONObject("identify")
            val prefixes = buildList { idn?.optJSONArray("namePrefixes")?.let { a -> for (i in 0 until a.length()) add(a.getString(i)) } }
            val svc = buildList { idn?.optJSONArray("serviceUuids")?.let { a -> for (i in 0 until a.length()) add(uuidOrEmpty(a.getString(i))) } }

            val t = o.optJSONObject("transport")
            val type = t?.optString("type", "gatt") ?: "gatt"
            require(type == "gatt" || type == "spp") { "transport.type must be gatt or spp" }
            val transport = TransportCfg(
                type = type,
                service = uuidOrEmpty(t?.optString("service", "") ?: ""),
                write = uuidOrEmpty(t?.optString("write", "") ?: ""),
                notify = uuidOrEmpty(t?.optString("notify", "") ?: ""),
                writeNoResponse = t?.optBoolean("writeNoResponse", false) ?: false,
                sppUuid = uuidOrEmpty(t?.optString("sppUuid", "00001101-0000-1000-8000-00805F9B34FB") ?: "")
            )

            val f = o.optJSONObject("framing")
            val checksum = f?.optString("checksum", "none") ?: "none"
            require(checksum in setOf("none", "sum8", "xor8", "crc16modbus")) { "Unknown checksum $checksum" }
            val headerHex = hexOrEmpty(f?.optString("header", "") ?: "")
            val framing = Framing(
                header = Hex.parse(headerHex) ?: ByteArray(0),
                lengthByte = f?.optBoolean("lengthByte", false) ?: false,
                checksum = checksum,
                checksumFrom = (f?.optInt("checksumFrom", 0) ?: 0).also { require(it in 0..16) { "checksumFrom out of range" } }
            )

            val cmds = LinkedHashMap<String, Command>()
            o.optJSONObject("commands")?.let { c ->
                for (name in c.keys()) {
                    require(name in ALLOWED_COMMANDS) { "Unknown command '$name'" }
                    val cj = c.getJSONObject(name)
                    val req = cj.optString("request", "")
                    val ack = if (cj.isNull("ack") || !cj.has("ack")) null else cj.getString("ack").ifBlank { null }
                    ack?.let { requireNotNull(Hex.parse(it)) { "Bad ack hex for $name" } }
                    val verified = cj.optBoolean("verified", false)
                    if (verified) require(req.isNotBlank()) { "Verified command '$name' needs a request template" }
                    cmds[name] = Command(req, ack, verified)
                }
            }

            val parse = o.optJSONObject("parse")
            val battery = parse?.optJSONObject("battery")?.let {
                BatteryParse(
                    prefix = hexOrEmpty(it.optString("prefix", "")),
                    left = optInt(it, "left"), right = optInt(it, "right"), case = optInt(it, "case"),
                    invalid = buildList { it.optJSONArray("invalid")?.let { a -> for (i in 0 until a.length()) add(a.getInt(i)) } }
                )
            }
            val mode = parse?.optJSONObject("mode")?.let {
                ModeParse(hexOrEmpty(it.optString("prefix", "")), optInt(it, "index") ?: 0, intMap(it.optJSONObject("map")))
            }
            val fw = parse?.optJSONObject("firmware")?.let {
                FirmwareParse(hexOrEmpty(it.optString("prefix", "")), optInt(it, "index") ?: 0)
            }
            val eq = o.optJSONObject("eq")?.let {
                EqCfg(it.optInt("bands", 10), it.optInt("offset", 0), it.optDouble("stepDb", 1.0).toFloat())
            }
            val touch = o.optJSONObject("touch")?.let {
                TouchCfg(intMap(it.optJSONObject("sides")), intMap(it.optJSONObject("gestures")), intMap(it.optJSONObject("actions")))
            }
            fun level(key: String, defMax: Int): LevelCfg? = o.optJSONObject(key)?.let {
                val lo = it.optInt("min", 0); val hi = it.optInt("max", defMax)
                require(lo in 0..255 && hi in 0..255 && lo <= hi) { "$key range must satisfy 0 <= min <= max <= 255" }
                LevelCfg(lo, hi)
            }
            val tr = level("transparencyLevel", 10)
            val anc = level("ancLevel", 3)

            return ProtocolProfile(
                name = o.optString("name", "Unnamed profile").take(80),
                verified = o.optBoolean("verified", false),
                notes = o.optString("notes", "").take(2000),
                identify = Identify(prefixes, svc),
                transport = transport, framing = framing,
                modes = intMap(o.optJSONObject("modes")),
                commands = cmds, battery = battery, mode = mode, firmware = fw,
                eq = eq, touch = touch, transparency = tr, ancLevel = anc
            )
        }
    }
}
```

### `app/src/main/java/com/eclipsecompanion/app/protocol/FrameCodec.kt`
```kotlin
package com.eclipsecompanion.app.protocol

import com.eclipsecompanion.app.domain.BatteryState
import com.eclipsecompanion.app.domain.ListeningMode
import com.eclipsecompanion.app.domain.Reported

object Hex {
    fun parse(s: String): ByteArray? {
        val c = s.filter { !it.isWhitespace() && it != ',' && it != ':' && it != '-' }
        if (c.length % 2 != 0) return null
        val out = ByteArray(c.length / 2)
        for (i in out.indices) {
            val v = c.substring(i * 2, i * 2 + 2).toIntOrNull(16) ?: return null
            out[i] = v.toByte()
        }
        return out
    }

    fun format(b: ByteArray): String = b.joinToString(" ") { "%02X".format(it.toInt() and 0xFF) }
}

fun startsWith(data: ByteArray, prefix: ByteArray): Boolean {
    if (prefix.size > data.size) return false
    for (i in prefix.indices) if (data[i] != prefix[i]) return false
    return true
}

/**
 * Frame layout: header | [length byte = body size] | body | [checksum]
 * Checksum is computed over frame bytes [checksumFrom, end of body).
 * crc16modbus is appended low byte first.
 */
object FrameCodec {

    fun checksum(kind: String, data: ByteArray): ByteArray = when (kind) {
        "sum8" -> byteArrayOf((data.sumOf { it.toInt() and 0xFF } and 0xFF).toByte())
        "xor8" -> byteArrayOf(data.fold(0) { a, b -> a xor (b.toInt() and 0xFF) }.toByte())
        "crc16modbus" -> {
            var crc = 0xFFFF
            for (b in data) {
                crc = crc xor (b.toInt() and 0xFF)
                repeat(8) { crc = if (crc and 1 != 0) (crc ushr 1) xor 0xA001 else crc ushr 1 }
            }
            byteArrayOf((crc and 0xFF).toByte(), ((crc ushr 8) and 0xFF).toByte())
        }
        else -> ByteArray(0)
    }

    private fun checksumSize(kind: String) = when (kind) { "sum8", "xor8" -> 1; "crc16modbus" -> 2; else -> 0 }

    /** Returns null if a token is missing/invalid (no frame is ever sent in that case). */
    fun build(f: Framing, template: String, ints: Map<String, Int>, strs: Map<String, String>): ByteArray? {
        val body = java.io.ByteArrayOutputStream()
        for (tok in template.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }) {
            if (tok.startsWith("{") && tok.endsWith("}")) {
                val key = tok.substring(1, tok.length - 1)
                if (key.endsWith("*")) {
                    val s = strs[key.dropLast(1)] ?: return null
                    if (s.any { it.code !in 32..126 }) return null
                    body.write(s.toByteArray(Charsets.US_ASCII))
                } else {
                    val v = ints[key] ?: return null
                    if (v !in 0..255) return null
                    body.write(v)
                }
            } else {
                body.write(Hex.parse(tok) ?: return null)
            }
        }
        val bodyBytes = body.toByteArray()
        if (f.lengthByte && bodyBytes.size > 255) return null
        val frame = java.io.ByteArrayOutputStream()
        frame.write(f.header)
        if (f.lengthByte) frame.write(bodyBytes.size)
        frame.write(bodyBytes)
        val pre = frame.toByteArray()
        val from = f.checksumFrom.coerceAtMost(pre.size)
        frame.write(checksum(f.checksum, pre.copyOfRange(from, pre.size)))
        return frame.toByteArray()
    }

    /** Returns the body or null if header/length/checksum do not match (strict). */
    fun parse(f: Framing, bytes: ByteArray): ByteArray? {
        if (!startsWith(bytes, f.header)) return null
        val csSize = checksumSize(f.checksum)
        val h = f.header.size
        val bodyStart: Int
        val bodyEnd: Int
        if (f.lengthByte) {
            if (bytes.size < h + 1) return null
            val len = bytes[h].toInt() and 0xFF
            bodyStart = h + 1
            bodyEnd = bodyStart + len
            if (bytes.size != bodyEnd + csSize) return null
        } else {
            bodyStart = h
            bodyEnd = bytes.size - csSize
            if (bodyEnd < bodyStart) return null
        }
        if (csSize > 0) {
            val from = f.checksumFrom.coerceAtMost(bodyEnd)
            val expect = checksum(f.checksum, bytes.copyOfRange(from, bodyEnd))
            val got = bytes.copyOfRange(bodyEnd, bodyEnd + csSize)
            if (!expect.contentEquals(got)) return null
        }
        return bytes.copyOfRange(bodyStart, bodyEnd)
    }
}

object ResponseParsers {

    fun battery(body: ByteArray, bp: BatteryParse?, now: Long): BatteryState? {
        if (bp == null) return null
        val prefix = Hex.parse(bp.prefix) ?: return null
        if (!startsWith(body, prefix)) return null
        fun v(i: Int?): Reported<Int> {
            if (i == null) return Reported.Unavailable("Not reported by this device")
            if (i !in body.indices) return Reported.Unavailable("Truncated report")
            val x = body[i].toInt() and 0xFF
            if (x in bp.invalid || x !in 0..100) return Reported.Unavailable("Device reported no valid value")
            return Reported.Available(x, now)
        }
        val u = Reported.Unavailable("Not reported")
        return BatteryState(v(bp.left), v(bp.right), v(bp.case), u)
    }

    fun mode(body: ByteArray, mp: ModeParse?): ListeningMode? {
        if (mp == null) return null
        val prefix = Hex.parse(mp.prefix) ?: return null
        if (!startsWith(body, prefix) || mp.index !in body.indices) return null
        val v = body[mp.index].toInt() and 0xFF
        val key = mp.map.entries.firstOrNull { it.value == v }?.key ?: return null
        return ListeningMode.values().firstOrNull { it.key == key }
    }

    fun firmware(body: ByteArray, fp: FirmwareParse?): String? {
        if (fp == null) return null
        val prefix = Hex.parse(fp.prefix) ?: return null
        if (!startsWith(body, prefix) || fp.index >= body.size) return null
        val s = String(body, fp.index, body.size - fp.index, Charsets.US_ASCII).trim { it.code < 32 || it.code > 126 }
        return s.takeIf { it.isNotEmpty() && it.length <= 40 && it.all { c -> c.code in 32..126 } }
    }
}
```

---

## 5. Bluetooth

### `app/src/main/java/com/eclipsecompanion/app/bluetooth/Transports.kt`
```kotlin
package com.eclipsecompanion.app.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.os.Build
import com.eclipsecompanion.app.protocol.TransportCfg
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.UUID

interface LinkTransport {
    /** Raw frames from the earbuds (notifications or serial data). */
    val incoming: SharedFlow<ByteArray>
    val disconnected: SharedFlow<Unit>
    /** Returns null on success or a human-readable error. */
    suspend fun open(): String?
    suspend fun write(data: ByteArray): Boolean
    fun close()
}

data class CharInfo(val uuid: String, val props: String, val readable: Boolean, val notifiable: Boolean)
data class ServiceInfo(val uuid: String, val characteristics: List<CharInfo>)

fun uuidOrNull(s: String): UUID? = try { if (s.isEmpty()) null else UUID.fromString(s) } catch (e: IllegalArgumentException) { null }

private val CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
val STD_BATTERY_SERVICE: UUID = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
val STD_BATTERY_LEVEL: UUID = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")

fun propString(p: Int): String = buildList {
    if ((p and BluetoothGattCharacteristic.PROPERTY_READ) != 0) add("READ")
    if ((p and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0) add("WRITE")
    if ((p and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0) add("WRITE_NO_RESP")
    if ((p and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) add("NOTIFY")
    if ((p and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) add("INDICATE")
}.joinToString(",")

/* ======================================================= BLE GATT */

@SuppressLint("MissingPermission")
class GattLink(
    private val context: Context,
    private val device: BluetoothDevice,
    private val cfg: TransportCfg
) : LinkTransport {

    private val _incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    override val incoming: SharedFlow<ByteArray> = _incoming
    private val _disconnected = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val disconnected: SharedFlow<Unit> = _disconnected
    /** Every notification from every characteristic (for the read-only Explorer). */
    val allNotifications = MutableSharedFlow<Pair<UUID, ByteArray>>(extraBufferCapacity = 64)

    private var gatt: BluetoothGatt? = null
    @Volatile private var generation = 0
    private val ops = Mutex()
    @Volatile private var connectD: CompletableDeferred<Boolean>? = null
    @Volatile private var discoverD: CompletableDeferred<Boolean>? = null
    @Volatile private var readD: CompletableDeferred<ByteArray?>? = null
    @Volatile private var writeD: CompletableDeferred<Boolean>? = null
    @Volatile private var descD: CompletableDeferred<Boolean>? = null
    @Volatile private var mtuD: CompletableDeferred<Boolean>? = null

    private fun callback(gen: Int) = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            if (gen != generation) { runCatching { g.close() }; return }   // stale callback
            if (newState == BluetoothProfile.STATE_CONNECTED && status == BluetoothGatt.GATT_SUCCESS) {
                connectD?.complete(true)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED || status != BluetoothGatt.GATT_SUCCESS) {
                connectD?.complete(false); discoverD?.complete(false)
                readD?.complete(null); writeD?.complete(false); descD?.complete(false)
                _disconnected.tryEmit(Unit)
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (gen == generation) discoverD?.complete(status == BluetoothGatt.GATT_SUCCESS)
        }

        @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
        override fun onCharacteristicRead(g: BluetoothGatt, c: BluetoothGattCharacteristic, status: Int) {
            if (gen != generation) return
            readD?.complete(if (status == BluetoothGatt.GATT_SUCCESS) c.value else null)
        }

        override fun onCharacteristicRead(g: BluetoothGatt, c: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
            if (gen == generation) readD?.complete(if (status == BluetoothGatt.GATT_SUCCESS) value else null)
        }

        override fun onCharacteristicWrite(g: BluetoothGatt, c: BluetoothGattCharacteristic, status: Int) {
            if (gen == generation) writeD?.complete(status == BluetoothGatt.GATT_SUCCESS)
        }

        override fun onDescriptorWrite(g: BluetoothGatt, d: BluetoothGattDescriptor, status: Int) {
            if (gen == generation) descD?.complete(status == BluetoothGatt.GATT_SUCCESS)
        }

        override fun onMtuChanged(g: BluetoothGatt, mtu: Int, status: Int) {
            if (gen == generation) mtuD?.complete(status == BluetoothGatt.GATT_SUCCESS)
        }

        @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
        override fun onCharacteristicChanged(g: BluetoothGatt, c: BluetoothGattCharacteristic) {
            if (gen != generation) return
            val v = c.value ?: return
            handle(c.uuid, v)
        }

        override fun onCharacteristicChanged(g: BluetoothGatt, c: BluetoothGattCharacteristic, value: ByteArray) {
            if (gen == generation) handle(c.uuid, value)
        }
    }

    private fun handle(uuid: UUID, value: ByteArray) {
        allNotifications.tryEmit(uuid to value)
        val n = uuidOrNull(cfg.notify)
        if (n == null || n == uuid) _incoming.tryEmit(value)
    }

    override suspend fun open(): String? {
        close()
        val gen = ++generation
        connectD = CompletableDeferred()
        gatt = device.connectGatt(context, false, callback(gen), BluetoothDevice.TRANSPORT_LE)
        if (gatt == null) return "Could not start a GATT connection"
        if (withTimeoutOrNull(15_000) { connectD!!.await() } != true) { close(); return "Connection timed out or was refused. Put the earbuds in pairing mode and try again." }
        discoverD = CompletableDeferred()
        if (gatt?.discoverServices() != true) { close(); return "Service discovery could not start" }
        if (withTimeoutOrNull(15_000) { discoverD!!.await() } != true) { close(); return "Service discovery failed" }
        // Best effort: a larger MTU lets longer frames (EQ vectors, names) go out in one write. Failure is harmless.
        mtuD = CompletableDeferred()
        if (gatt?.requestMtu(247) == true) withTimeoutOrNull(3_000) { mtuD!!.await() }
        val n = uuidOrNull(cfg.notify)
        if (n != null && !setNotify(n, true)) { close(); return "Could not subscribe to the control notifications" }
        return null
    }

    private fun findChar(uuid: UUID, service: UUID? = null): BluetoothGattCharacteristic? {
        val g = gatt ?: return null
        if (service != null) g.getService(service)?.getCharacteristic(uuid)?.let { return it }
        return g.services.firstNotNullOfOrNull { it.getCharacteristic(uuid) }
    }

    fun services(): List<ServiceInfo> = gatt?.services.orEmpty().map { s ->
        ServiceInfo(s.uuid.toString(), s.characteristics.map { c ->
            CharInfo(
                c.uuid.toString(), propString(c.properties),
                readable = (c.properties and BluetoothGattCharacteristic.PROPERTY_READ) != 0,
                notifiable = (c.properties and (BluetoothGattCharacteristic.PROPERTY_NOTIFY or BluetoothGattCharacteristic.PROPERTY_INDICATE)) != 0
            )
        })
    }

    fun hasService(uuid: UUID) = gatt?.getService(uuid) != null

    suspend fun readChar(uuid: UUID): ByteArray? = ops.withLock {
        val g = gatt ?: return@withLock null
        val ch = findChar(uuid) ?: return@withLock null
        if ((ch.properties and BluetoothGattCharacteristic.PROPERTY_READ) == 0) return@withLock null
        readD = CompletableDeferred()
        if (!g.readCharacteristic(ch)) return@withLock null
        withTimeoutOrNull(5_000) { readD!!.await() }
    }

    @Suppress("DEPRECATION")
    suspend fun setNotify(uuid: UUID, enable: Boolean): Boolean = ops.withLock {
        val g = gatt ?: return@withLock false
        val ch = findChar(uuid, uuidOrNull(cfg.service)) ?: return@withLock false
        val p = ch.properties
        val canNotify = (p and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0
        val canIndicate = (p and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
        if (!canNotify && !canIndicate) return@withLock false
        if (!g.setCharacteristicNotification(ch, enable)) return@withLock false
        val d = ch.getDescriptor(CCCD) ?: return@withLock false
        val v = when {
            !enable -> BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            canNotify -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            else -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
        }
        descD = CompletableDeferred()
        val started = if (Build.VERSION.SDK_INT >= 33) {
            g.writeDescriptor(d, v) == BluetoothStatusCodes.SUCCESS
        } else {
            d.value = v
            g.writeDescriptor(d)
        }
        if (!started) return@withLock false
        withTimeoutOrNull(5_000) { descD!!.await() } == true
    }

    @Suppress("DEPRECATION")
    override suspend fun write(data: ByteArray): Boolean = ops.withLock {
        val g = gatt ?: return@withLock false
        val u = uuidOrNull(cfg.write) ?: return@withLock false
        val ch = findChar(u, uuidOrNull(cfg.service)) ?: return@withLock false
        val needed = if (cfg.writeNoResponse) BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE else BluetoothGattCharacteristic.PROPERTY_WRITE
        if ((ch.properties and needed) == 0) return@withLock false
        val type = if (cfg.writeNoResponse) BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE else BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        writeD = CompletableDeferred()
        val started = if (Build.VERSION.SDK_INT >= 33) {
            g.writeCharacteristic(ch, data, type) == BluetoothStatusCodes.SUCCESS
        } else {
            ch.writeType = type
            ch.value = data
            g.writeCharacteristic(ch)
        }
        if (!started) return@withLock false
        withTimeoutOrNull(5_000) { writeD!!.await() } == true
    }

    override fun close() {
        generation++            // invalidates every callback of the previous session
        connectD?.complete(false); discoverD?.complete(false)
        readD?.complete(null); writeD?.complete(false); descD?.complete(false); mtuD?.complete(false)
        gatt?.let { runCatching { it.disconnect() }; runCatching { it.close() } }
        gatt = null
    }
}

/* ======================================================= Bluetooth Classic SPP */

@SuppressLint("MissingPermission")
class SppLink(private val device: BluetoothDevice, private val uuid: UUID) : LinkTransport {
    private val _incoming = MutableSharedFlow<ByteArray>(extraBufferCapacity = 64)
    override val incoming: SharedFlow<ByteArray> = _incoming
    private val _disconnected = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val disconnected: SharedFlow<Unit> = _disconnected
    private var socket: android.bluetooth.BluetoothSocket? = null
    private var readJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val writeLock = Mutex()

    override suspend fun open(): String? = withContext(Dispatchers.IO) {
        try {
            val s = device.createRfcommSocketToServiceRecord(uuid)
            socket = s
            s.connect()
            readJob = scope.launch {
                val buf = ByteArray(512)
                try {
                    while (isActive) {
                        val n = s.inputStream.read(buf)
                        if (n < 0) break
                        if (n > 0) _incoming.emit(buf.copyOf(n))
                    }
                } catch (_: IOException) { }
                _disconnected.tryEmit(Unit)
            }
            null
        } catch (e: IOException) {
            close(); "Serial (SPP) connection failed: ${e.message ?: "unknown"}"
        } catch (e: SecurityException) {
            close(); "Bluetooth permission is missing"
        }
    }

    override suspend fun write(data: ByteArray): Boolean = writeLock.withLock {
        withContext(Dispatchers.IO) {
            try { socket?.outputStream?.let { it.write(data); it.flush(); true } ?: false } catch (_: IOException) { false }
        }
    }

    override fun close() {
        readJob?.cancel()
        runCatching { socket?.close() }
        socket = null
    }
}
```

### `app/src/main/java/com/eclipsecompanion/app/bluetooth/Scanner.kt`
```kotlin
package com.eclipsecompanion.app.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.eclipsecompanion.app.protocol.ProtocolProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class Match(val label: String) {
    SERVICE_MATCH("Service matches profile"),
    NAME_MATCH("Name looks like Eclipse/Ronin"),
    UNKNOWN("Other device")
}

data class Candidate(val address: String, val name: String, val rssi: Int?, val bonded: Boolean, val match: Match)

object Perms {
    fun bluetooth(): Array<String> =
        if (Build.VERSION.SDK_INT >= 31) arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    fun granted(ctx: Context, perms: Array<String>) =
        perms.all { ContextCompat.checkSelfPermission(ctx, it) == PackageManager.PERMISSION_GRANTED }

    fun bluetoothGranted(ctx: Context) = granted(ctx, bluetooth())
    fun micGranted(ctx: Context) = granted(ctx, arrayOf(Manifest.permission.RECORD_AUDIO))
    fun redact(address: String) = if (address.length >= 5) "••:••:${address.takeLast(5)}" else "••"
}

@SuppressLint("MissingPermission")
class Scanner(private val context: Context) {
    private val _results = MutableStateFlow<List<Candidate>>(emptyList())
    val results: StateFlow<List<Candidate>> = _results
    private val _scanning = MutableStateFlow(false)
    val scanning: StateFlow<Boolean> = _scanning
    val error = MutableStateFlow<String?>(null)

    private val adapter get() = context.getSystemService(BluetoothManager::class.java)?.adapter

    private fun classify(name: String, uuids: List<String>, p: ProtocolProfile): Match {
        val wanted = p.identify.serviceUuids.map { it.lowercase() }
        if (wanted.isNotEmpty() && uuids.any { it.lowercase() in wanted }) return Match.SERVICE_MATCH
        if (p.identify.namePrefixes.any { name.startsWith(it, ignoreCase = true) } ||
            name.contains("eclipse", true) || name.contains("ronin", true)) return Match.NAME_MATCH
        return Match.UNKNOWN
    }

    /** Devices already paired in Android settings (usable for SPP and for BLE address reuse). */
    fun bonded(p: ProtocolProfile): List<Candidate> {
        val a = adapter ?: return emptyList()
        if (!Perms.bluetoothGranted(context)) return emptyList()
        return try {
            a.bondedDevices.orEmpty().map {
                val n = it.name ?: "Unnamed device"
                Candidate(it.address, n, null, true, classify(n, emptyList(), p))
            }.sortedBy { it.match.ordinal }
        } catch (e: SecurityException) { emptyList() }
    }

    /** Bounded scan; cancel by cancelling the calling coroutine. */
    suspend fun scan(p: ProtocolProfile, timeoutMs: Long = 12_000) {
        if (_scanning.value) return
        val a = adapter
        error.value = null
        if (a == null || !a.isEnabled) { error.value = "Bluetooth is off. Turn it on and try again."; return }
        if (!Perms.bluetoothGranted(context)) { error.value = "Nearby-devices permission is needed to find earbuds."; return }
        val scanner = a.bluetoothLeScanner ?: run { error.value = "Bluetooth LE scanning is unavailable."; return }
        val found = LinkedHashMap<String, Candidate>()
        val cb = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, r: ScanResult) {
                val name = r.scanRecord?.deviceName ?: runCatching { r.device.name }.getOrNull() ?: return
                val uuids = r.scanRecord?.serviceUuids.orEmpty().map { it.uuid.toString() }
                val bonded = try { r.device.bondState == android.bluetooth.BluetoothDevice.BOND_BONDED } catch (e: SecurityException) { false }
                found[r.device.address] = Candidate(r.device.address, name, r.rssi, bonded, classify(name, uuids, p))
                _results.value = found.values.sortedWith(compareBy({ it.match.ordinal }, { -(it.rssi ?: -127) }))
            }
            override fun onScanFailed(errorCode: Int) { error.value = "Scan failed (code $errorCode)" }
        }
        _results.value = emptyList()
        _scanning.value = true
        try {
            scanner.startScan(null, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build(), cb)
            delay(timeoutMs)
        } finally {
            runCatching { scanner.stopScan(cb) }
            _scanning.value = false
        }
    }
}
```

### `app/src/main/java/com/eclipsecompanion/app/bluetooth/EarbudController.kt`
```kotlin
package com.eclipsecompanion.app.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import com.eclipsecompanion.app.domain.BatteryState
import com.eclipsecompanion.app.domain.CommandResult
import com.eclipsecompanion.app.domain.ListeningMode
import com.eclipsecompanion.app.domain.Phase
import com.eclipsecompanion.app.domain.Reported
import com.eclipsecompanion.app.domain.mergeBattery
import com.eclipsecompanion.app.domain.validBattery
import com.eclipsecompanion.app.domain.compatibilityLabel
import com.eclipsecompanion.app.protocol.FrameCodec
import com.eclipsecompanion.app.protocol.Hex
import com.eclipsecompanion.app.protocol.ProtocolProfile
import com.eclipsecompanion.app.protocol.ResponseParsers
import com.eclipsecompanion.app.protocol.startsWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private sealed interface Exchange {
    data class Fail(val reason: String) : Exchange
    data object Sent : Exchange                         // written, no ack defined
    data object Timeout : Exchange                      // ack expected but not received
    data class Ack(val body: ByteArray) : Exchange
}

@SuppressLint("MissingPermission")
class EarbudController(private val context: Context, private val scope: CoroutineScope) {

    val phase = MutableStateFlow(Phase.IDLE)
    val message = MutableStateFlow<String?>(null)
    val connectedName = MutableStateFlow<String?>(null)
    val battery = MutableStateFlow(BatteryState.unavailable("Not connected"))
    val confirmedMode = MutableStateFlow<Reported<ListeningMode>>(Reported.Unavailable("Not confirmed yet"))
    val pendingMode = MutableStateFlow<ListeningMode?>(null)
    val firmware = MutableStateFlow<Reported<String>>(Reported.Unavailable("Not reported"))
    val lastComm = MutableStateFlow<Long?>(null)
    val profile = MutableStateFlow(ProtocolProfile.EMPTY)
    val explorer = MutableStateFlow<List<ServiceInfo>>(emptyList())
    val rawLog = MutableStateFlow<List<String>>(emptyList())
    val listening = MutableStateFlow<Set<String>>(emptySet())
    val busy = MutableStateFlow(false)
    val lastResult = MutableStateFlow<String?>(null)
    /** Emits once for every change the earbuds CONFIRMED (used for the optional haptic tick). Never emitted for unconfirmed commands. */
    val confirmedTick = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private var link: LinkTransport? = null
    private var address: String? = null
    private var jobs: List<Job> = emptyList()
    private var userDisconnected = false
    private var retries = 0
    private val cmdMutex = Mutex()

    fun setProfile(p: ProtocolProfile) { profile.value = p }

    /** Capability check used by every control. */
    fun can(name: String): Boolean =
        phase.value == Phase.READY && link != null && profile.value.transportConfigured && profile.value.isVerified(name)

    fun reasonFor(name: String): String = when {
        phase.value != Phase.READY -> "Connect your earbuds first."
        !profile.value.transportConfigured -> "No control channel is configured. Import a verified protocol profile."
        profile.value.commands[name] == null -> "This feature is not defined in the loaded protocol profile."
        !profile.value.isVerified(name) -> "Protocol not verified. Capture and import the real commands (see Help › Protocol)."
        else -> "Not available."
    }

    /* ------------------------------------------------ connection */

    suspend fun connect(addr: String, name: String) {
        userDisconnected = false; retries = 0
        doConnect(addr, name)
    }

    private suspend fun doConnect(addr: String, name: String): Boolean {
        teardown()
        val adapter = context.getSystemService(BluetoothManager::class.java)?.adapter
        if (adapter == null || !adapter.isEnabled) return fail("Bluetooth is off or unavailable.")
        if (!Perms.bluetoothGranted(context)) return fail("Nearby-devices permission is needed.")
        phase.value = Phase.CONNECTING; message.value = "Connecting to $name…"
        val device = try { adapter.getRemoteDevice(addr) } catch (e: IllegalArgumentException) { return fail("Invalid device address.") }
        val p = profile.value
        val l: LinkTransport = if (p.transport.type == "spp") {
            SppLink(device, UUID.fromString(p.transport.sppUuid))
        } else GattLink(context, device, p.transport)
        phase.value = Phase.DISCOVERING; message.value = "Connected. Checking supported controls…"
        val err = try { l.open() } catch (e: CancellationException) {
            // User pressed Cancel while connecting: never leave a half-open GATT/SPP link behind.
            l.close(); phase.value = Phase.DISCONNECTED; message.value = null; throw e
        }
        if (err != null) { l.close(); return fail(err) }
        link = l; address = addr; connectedName.value = name
        jobs = buildList {
            add(scope.launch { l.incoming.collect { handleIncoming(it) } })
            add(scope.launch { l.disconnected.first(); onLinkLost(name) })
            if (l is GattLink) add(scope.launch {
                l.allNotifications.collect { (u, v) ->
                    if (u.toString() in listening.value) appendLog("${u.toString().take(8)}  ${Hex.format(v)}")
                }
            })
        }
        phase.value = Phase.READY; message.value = null
        if (l is GattLink) { explorer.value = l.services(); readStandardBattery(l) }
        refresh()
        return true
    }

    private fun fail(msg: String): Boolean { phase.value = Phase.ERROR; message.value = msg; return false }

    private fun onLinkLost(name: String) {
        if (userDisconnected) return
        val addr = address
        teardown()
        phase.value = Phase.DISCONNECTED; message.value = "Connection lost."
        if (addr != null && retries < 2) {
            retries++
            scope.launch { delay(2_000L * retries); if (!userDisconnected && phase.value == Phase.DISCONNECTED) doConnect(addr, name) }
        }
    }

    fun disconnect() {
        userDisconnected = true
        teardown()
        phase.value = Phase.DISCONNECTED; message.value = null
    }

    private fun teardown() {
        jobs.forEach { it.cancel() }; jobs = emptyList()
        link?.close(); link = null
        explorer.value = emptyList(); listening.value = emptySet(); rawLog.value = emptyList()
        pendingMode.value = null; busy.value = false
    }

    /* ------------------------------------------------ reads */

    private suspend fun readStandardBattery(l: GattLink) {
        if (!l.hasService(STD_BATTERY_SERVICE)) return
        val v = l.readChar(STD_BATTERY_LEVEL)?.firstOrNull()?.toInt()?.and(0xFF)
        if (validBattery(v)) {
            val now = System.currentTimeMillis()
            val b = BatteryState.unavailable("Not reported")
            battery.value = mergeBattery(battery.value, b.copy(combined = Reported.Available(v!!, now)))
            lastComm.value = now
        }
    }

    suspend fun refresh() {
        if (can("readBattery")) (exchange("readBattery") as? Exchange.Ack)?.let { handleBody(it.body) }
        if (can("readMode")) (exchange("readMode") as? Exchange.Ack)?.let { handleBody(it.body) }
        if (can("readFirmware")) (exchange("readFirmware") as? Exchange.Ack)?.let { handleBody(it.body) }
        (link as? GattLink)?.let { readStandardBattery(it) }
    }

    private fun handleIncoming(raw: ByteArray) {
        val body = FrameCodec.parse(profile.value.framing, raw) ?: return
        handleBody(body)
    }

    private fun handleBody(body: ByteArray) {
        val p = profile.value
        val now = System.currentTimeMillis()
        lastComm.value = now
        ResponseParsers.battery(body, p.battery, now)?.let { battery.value = mergeBattery(battery.value, it) }
        ResponseParsers.mode(body, p.mode)?.let { confirmedMode.value = Reported.Available(it, now) }
        ResponseParsers.firmware(body, p.firmware)?.let { firmware.value = Reported.Available(it, now) }
    }

    /* ------------------------------------------------ command exchange (one in flight) */

    private suspend fun exchange(name: String, ints: Map<String, Int> = emptyMap(), strs: Map<String, String> = emptyMap()): Exchange =
        cmdMutex.withLock {
            val p = profile.value
            val cmd = p.commands[name]
            if (cmd == null || !cmd.verified) return@withLock Exchange.Fail(reasonFor(name))
            val l = link ?: return@withLock Exchange.Fail("Not connected.")
            val frame = FrameCodec.build(p.framing, cmd.request, ints, strs)
                ?: return@withLock Exchange.Fail("Could not build the command (missing or invalid value).")
            val ack = cmd.ack?.let { Hex.parse(it) }
            if (ack == null) {
                return@withLock if (l.write(frame)) Exchange.Sent else Exchange.Fail("The write was refused by the earbuds.")
            }
            coroutineScope {
                // Subscribe BEFORE writing so the acknowledgement cannot be missed.
                val waiter = async(start = CoroutineStart.UNDISPATCHED) {
                    withTimeoutOrNull(3_000) {
                        l.incoming.mapNotNull { FrameCodec.parse(p.framing, it) }.first { startsWith(it, ack) }
                    }
                }
                if (!l.write(frame)) { waiter.cancel(); Exchange.Fail("The write was refused by the earbuds.") }
                else waiter.await()?.let { Exchange.Ack(it) } ?: Exchange.Timeout
            }
        }

    private suspend fun execute(
        name: String, ints: Map<String, Int> = emptyMap(), strs: Map<String, String> = emptyMap(),
        done: String
    ): CommandResult {
        if (!can(name)) return CommandResult.Unsupported(reasonFor(name)).also { lastResult.value = describe(it) }
        if (busy.value) return CommandResult.Rejected("Another change is still in progress.")
        busy.value = true
        val result: CommandResult = try {
            when (val r = exchange(name, ints, strs)) {
                is Exchange.Fail -> CommandResult.Rejected(r.reason)
                Exchange.Timeout -> CommandResult.Rejected("The earbuds did not confirm the change. Nothing was marked as applied.")
                Exchange.Sent -> CommandResult.SentUnconfirmed("Command sent, but this command has no acknowledgement, so it cannot be confirmed.")
                is Exchange.Ack -> { handleBody(r.body); CommandResult.Confirmed(done) }
            }
        } finally { busy.value = false }
        if (result is CommandResult.Confirmed) confirmedTick.tryEmit(Unit)
        lastResult.value = describe(result)
        return result
    }

    private fun describe(r: CommandResult) = when (r) {
        is CommandResult.Confirmed -> r.text
        is CommandResult.SentUnconfirmed -> r.text
        is CommandResult.Rejected -> r.reason
        is CommandResult.Unsupported -> r.reason
    }

    /* ------------------------------------------------ public controls */

    suspend fun setMode(mode: ListeningMode): CommandResult {
        val p = profile.value
        if (!can("setMode")) return CommandResult.Unsupported(reasonFor("setMode")).also { lastResult.value = describe(it) }
        val v = p.modes[mode.key] ?: return CommandResult.Unsupported("The profile has no value for ${mode.label}.")
        if (pendingMode.value != null) return CommandResult.Rejected("Another mode change is in progress.")
        pendingMode.value = mode
        try {
            val r = execute("setMode", mapOf("mode" to v), done = "${mode.label} confirmed by the earbuds.")
            if (r is CommandResult.Rejected || r is CommandResult.Unsupported) return r
            // Prefer an explicit read-back; otherwise trust a protocol-level acknowledgement only.
            if (can("readMode")) {
                val rb = exchange("readMode")
                if (rb is Exchange.Ack) {
                    val got = ResponseParsers.mode(rb.body, p.mode)
                    return if (got == mode) {
                        confirmedMode.value = Reported.Available(mode, System.currentTimeMillis())
                        CommandResult.Confirmed("${mode.label} confirmed by read-back.").also { confirmedTick.tryEmit(Unit) }
                    } else CommandResult.Rejected("The earbuds reported a different mode. The display was not changed.")
                }
                return CommandResult.SentUnconfirmed("Mode command sent, but the earbuds did not report their mode back.")
            }
            if (r is CommandResult.Confirmed) confirmedMode.value = Reported.Available(mode, System.currentTimeMillis())
            return r
        } finally { pendingMode.value = null }
    }

    suspend fun setTransparencyLevel(level: Int) = execute("setTransparencyLevel", mapOf("level" to level), done = "Transparency level confirmed.")
    suspend fun setGaming(on: Boolean) = execute("setGaming", mapOf("on" to if (on) 1 else 0), done = if (on) "Gaming mode on (confirmed)." else "Gaming mode off (confirmed).")

    /** Only offered when the profile marks setAncLevel verified AND defines the real range in "ancLevel". */
    suspend fun setAncLevel(level: Int): CommandResult {
        val cfg = profile.value.ancLevel ?: return CommandResult.Unsupported("The profile does not define the noise-cancelling level range.")
        if (level !in cfg.min..cfg.max) return CommandResult.Rejected("Level must be between ${cfg.min} and ${cfg.max}.")
        return execute("setAncLevel", mapOf("level" to level), done = "Noise-cancelling level confirmed.")
    }

    suspend fun setWearDetection(on: Boolean) = execute("setWearDetection", mapOf("on" to if (on) 1 else 0), done = if (on) "Wear detection on (confirmed)." else "Wear detection off (confirmed).")
    suspend fun setVoicePrompts(on: Boolean) = execute("setVoicePrompts", mapOf("on" to if (on) 1 else 0), done = if (on) "Voice prompts on (confirmed)." else "Voice prompts off (confirmed).")

    suspend fun setName(name: String): CommandResult {
        if (name.length !in 1..20 || name.any { it.code !in 32..126 }) return CommandResult.Rejected("Use 1–20 plain ASCII characters.")
        return execute("setName", strs = mapOf("name" to name), done = "Earbud name change confirmed. Reconnect to see it.")
    }

    suspend fun factoryReset() = execute("factoryReset", done = "Reset confirmed by the earbuds. Re-pair them in Android Bluetooth settings.")

    suspend fun setEq(gains: List<Float>): CommandResult {
        val p = profile.value
        val cfg = p.eq ?: return CommandResult.Unsupported("The profile has no EQ layout.")
        if (gains.size != cfg.bands) return CommandResult.Unsupported("The earbuds expect ${cfg.bands} bands; this editor has ${gains.size}. Use a matching profile.")
        val ints = gains.mapIndexed { i, g -> "b$i" to Math.round(g / cfg.stepDb).plus(cfg.offset).coerceIn(0, 255) }.toMap()
        return execute("setEq", ints, done = "EQ confirmed by the earbuds.")
    }

    suspend fun setTouch(side: String, gesture: String, action: String): CommandResult {
        val t = profile.value.touch ?: return CommandResult.Unsupported("The profile has no touch layout.")
        val s = t.sides[side]; val g = t.gestures[gesture]; val a = t.actions[action]
        if (s == null || g == null || a == null) return CommandResult.Unsupported("That gesture/action is not defined in the profile.")
        return execute("setTouch", mapOf("side" to s, "gesture" to g, "action" to a), done = "Touch control confirmed.")
    }

    /* ------------------------------------------------ explorer (read-only) */

    suspend fun explorerRead(uuid: String): String {
        val l = link as? GattLink ?: return "Not a GATT connection"
        val u = uuidOrNull(uuid) ?: return "Bad UUID"
        val v = l.readChar(u) ?: return "Read failed or not readable"
        val ascii = v.map { it.toInt().toChar() }.filter { it.code in 32..126 }.joinToString("")
        return "${Hex.format(v)}   \"$ascii\""
    }

    suspend fun explorerListen(uuid: String, on: Boolean): Boolean {
        val l = link as? GattLink ?: return false
        val u = uuidOrNull(uuid) ?: return false
        val ok = l.setNotify(u, on)
        if (ok) listening.value = if (on) listening.value + uuid else listening.value - uuid
        return ok
    }

    private fun appendLog(line: String) {
        val t = SimpleDateFormat("HH:mm:ss", Locale.US).format(Date())
        rawLog.value = (rawLog.value + "$t  $line").takeLast(200)
    }

    fun clearLog() { rawLog.value = emptyList() }

    /* ------------------------------------------------ diagnostics (redacted) */

    fun diagnostics(appVersion: String): String = buildString {
        appendLine("Eclipse Companion diagnostics")
        appendLine("App: $appVersion   Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})   Phone: ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Bluetooth permission: ${Perms.bluetoothGranted(context)}")
        appendLine("Phase: ${phase.value}   Device: ${connectedName.value ?: "-"} ${address?.let { Perms.redact(it) } ?: ""}")
        val p = profile.value
        appendLine("Profile: ${p.name}   verified flag: ${p.verified}   transport: ${p.transport.type}")
        appendLine("Verified commands: ${p.commands.filterValues { it.verified }.keys.ifEmpty { setOf("none") }}")
        appendLine("Compatibility: ${compatibilityLabel(phase.value == Phase.READY, p.transportConfigured, p.commands.filterValues { it.verified }.keys)}")
        appendLine("GATT services seen: ${explorer.value.size}")
        appendLine("Last communication: ${lastComm.value?.let { Date(it) } ?: "never"}")
        appendLine("Last result: ${lastResult.value ?: "-"}")
    }
}
```
