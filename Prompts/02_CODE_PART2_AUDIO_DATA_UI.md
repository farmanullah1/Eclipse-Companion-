# File 02 — Audio, system effects, relaxation, storage, ViewModel, MainActivity, tests (v3.0)

Package `com.eclipsecompanion.app`. UI screens are in **files 02B, 02C, 02D**, and new v3.0 extras are in **file 04**; Gemini must create all of them. Same rules: copy verbatim, fix only real compile errors.

---

## 1. DSP

### `app/src/main/java/com/eclipsecompanion/app/audio/Dsp.kt`
```kotlin
package com.eclipsecompanion.app.audio

import com.eclipsecompanion.app.domain.EqDefaults
import com.eclipsecompanion.app.domain.HearingSettings
import com.eclipsecompanion.app.domain.SpeechClarity
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tanh

/** Direct-form-I biquad. Coefficients from the RBJ Audio EQ Cookbook. */
class Biquad(
    private val b0: Double, private val b1: Double, private val b2: Double,
    private val a1: Double, private val a2: Double
) {
    private var x1 = 0.0; private var x2 = 0.0; private var y1 = 0.0; private var y2 = 0.0

    fun process(x: Float): Float {
        val xd = x.toDouble()
        val y = b0 * xd + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2 = x1; x1 = xd; y2 = y1; y1 = y
        return y.toFloat()
    }

    companion object {
        private fun norm(b0: Double, b1: Double, b2: Double, a0: Double, a1: Double, a2: Double) =
            Biquad(b0 / a0, b1 / a0, b2 / a0, a1 / a0, a2 / a0)

        fun highPass(fs: Int, f0: Double, q: Double = 0.7071): Biquad {
            val w = 2 * PI * f0 / fs; val c = cos(w); val al = sin(w) / (2 * q)
            return norm((1 + c) / 2, -(1 + c), (1 + c) / 2, 1 + al, -2 * c, 1 - al)
        }

        fun peaking(fs: Int, f0: Double, q: Double, gainDb: Double): Biquad {
            val a = 10.0.pow(gainDb / 40); val w = 2 * PI * f0 / fs; val c = cos(w); val al = sin(w) / (2 * q)
            return norm(1 + al * a, -2 * c, 1 - al * a, 1 + al / a, -2 * c, 1 - al / a)
        }

        fun highShelf(fs: Int, f0: Double, gainDb: Double): Biquad {
            val a = 10.0.pow(gainDb / 40); val w = 2 * PI * f0 / fs; val c = cos(w)
            val al = sin(w) / 2 * sqrt(2.0); val beta = 2 * sqrt(a) * al
            return norm(
                a * ((a + 1) + (a - 1) * c + beta), -2 * a * ((a - 1) + (a + 1) * c), a * ((a + 1) + (a - 1) * c - beta),
                (a + 1) - (a - 1) * c + beta, 2 * ((a - 1) - (a + 1) * c), (a + 1) - (a - 1) * c - beta
            )
        }
    }
}

/** Stateless soft limiter: identity below [threshold], smooth saturation above, output strictly < 1.0. */
class SoftLimiter(private val threshold: Float = 0.8f) {
    fun process(x: Float): Float {
        val a = abs(x)
        if (a <= threshold) return x
        val over = (a - threshold) / (1f - threshold)
        val y = threshold + (1f - threshold) * tanh(over)
        return if (x < 0) -y else y
    }
}

/**
 * Gentle feed-forward compressor for Hearing Assist comfort: signals above [threshold] are reduced by [ratio]:1.
 * Quiet speech passes unchanged; sudden loud sounds (door slam, cutlery) are tamed before the final limiter.
 */
class MildCompressor(fs: Int, private val threshold: Float = 0.25f, private val ratio: Float = 2.5f) {
    private var env = 0f
    private val attack = 1f - exp(-1f / (0.005f * fs))     // ~5 ms
    private val release = 1f - exp(-1f / (0.120f * fs))    // ~120 ms

    fun process(x: Float): Float {
        val a = abs(x)
        env += (if (a > env) attack else release) * (a - env)
        val g = if (env > threshold) (threshold + (env - threshold) / ratio) / env else 1f
        return x * g
    }
}

/** Hearing Assist filter chain: optional rumble cut + optional speech-clarity boost. */
class HearingChain(private val fs: Int) {
    private var rumble: Biquad? = null
    private var presence: Biquad? = null
    private var air: Biquad? = null
    private var lastClarity: SpeechClarity? = null
    private var lastRumble: Boolean? = null

    fun configure(s: HearingSettings) {
        if (s.rumbleCut != lastRumble) {
            rumble = if (s.rumbleCut) Biquad.highPass(fs, 120.0) else null
            lastRumble = s.rumbleCut
        }
        if (s.clarity != lastClarity) {
            when (s.clarity) {
                SpeechClarity.OFF -> { presence = null; air = null }
                SpeechClarity.MILD -> { presence = Biquad.peaking(fs, 2500.0, 1.0, 3.0); air = null }
                SpeechClarity.MODERATE -> { presence = Biquad.peaking(fs, 2500.0, 1.0, 6.0); air = Biquad.highShelf(fs, 4000.0, 3.0) }
            }
            lastClarity = s.clarity
        }
    }

    fun filter(x: Float): Float {
        var y = x
        rumble?.let { y = it.process(y) }
        presence?.let { y = it.process(y) }
        air?.let { y = it.process(y) }
        return y
    }
}

/** Log-frequency interpolation of the 10-band EQ, used to map onto a system equalizer's own bands. */
fun interpolateGainDb(gains: List<Float>, hz: Float): Float {
    val f = EqDefaults.FREQS
    if (gains.size != f.size) return 0f
    if (hz <= f.first()) return gains.first()
    if (hz >= f.last()) return gains.last()
    for (i in 0 until f.size - 1) {
        if (hz <= f[i + 1]) {
            val t = ln(hz / f[i]) / ln(f[i + 1].toFloat() / f[i])
            return gains[i] + (gains[i + 1] - gains[i]) * t
        }
    }
    return 0f
}
```

---

## 2. Hearing Assist (engine + hub + foreground service)

### `app/src/main/java/com/eclipsecompanion/app/audio/HearingAssist.kt`
```kotlin
package com.eclipsecompanion.app.audio

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Process
import com.eclipsecompanion.app.MainActivity
import com.eclipsecompanion.app.bluetooth.Perms
import com.eclipsecompanion.app.domain.HearingSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

data class AssistUi(
    val running: Boolean = false,
    val status: String = "Stopped",
    val route: String = "No earbuds output detected",
    val level: Float = 0f,
    val elapsedSec: Int = 0,
    val error: String? = null,
    val bufferMs: Int = 0       // upper bound of mic+speaker buffering; Bluetooth adds more
)

/** Shared state between the service (writer) and the UI (reader). Holds NO audio. */
object HearingAssistHub {
    val ui = MutableStateFlow(AssistUi())
    @Volatile var settings = HearingSettings()

    fun routeName(ctx: Context): String {
        val d = findEarbudOutput(ctx)
        return if (d != null) "Output: ${d.productName ?: "Bluetooth earbuds"}" else "No Bluetooth earbuds output detected"
    }

    fun findEarbudOutput(ctx: Context): AudioDeviceInfo? {
        val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).firstOrNull {
            it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
                (Build.VERSION.SDK_INT >= 31 && it.type == AudioDeviceInfo.TYPE_BLE_HEADSET)
        }
    }
}

@SuppressLint("MissingPermission")
class HearingAssistEngine(private val ctx: Context) {
    @Volatile private var running = false
    @Volatile private var stopReason: String? = null
    private var thread: Thread? = null
    private val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    /** Returns an error message, or null when the session thread has been started. */
    fun start(onStopped: (String) -> Unit): String? {
        if (running) return null
        if (!Perms.micGranted(ctx)) return "Microphone permission is needed."
        val out = HearingAssistHub.findEarbudOutput(ctx) ?: return "Connect your earbuds first. Hearing Assist only plays through Bluetooth earbuds."
        if (am.mode != AudioManager.MODE_NORMAL) return "A call is active. Finish the call first."

        val rate = listOf(48_000, 44_100, 16_000).firstOrNull {
            AudioRecord.getMinBufferSize(it, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) > 0
        } ?: return "This phone does not support a suitable microphone format."
        val frame = rate / 50  // 20 ms
        val recMin = AudioRecord.getMinBufferSize(rate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val trkMin = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (recMin <= 0 || trkMin <= 0) return "Audio buffers are unavailable on this phone."

        val rec = AudioRecord(MediaRecorder.AudioSource.MIC, rate, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT, max(recMin, frame * 4))
        if (rec.state != AudioRecord.STATE_INITIALIZED) { rec.release(); return "Could not open the microphone (another app may be using it)." }

        val trackBuilder = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(max(trkMin, frame * 4))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setPerformanceMode(AudioTrack.PERFORMANCE_MODE_LOW_LATENCY)
        val track = trackBuilder.build()
        track.setPreferredDevice(out)

        val focusListener = AudioManager.OnAudioFocusChangeListener { change ->
            if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
                change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) requestStop("Stopped: another app took the audio.")
        }
        val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build())
            .setOnAudioFocusChangeListener(focusListener).build()
        if (am.requestAudioFocus(focus) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            rec.release(); track.release(); return "Could not get audio focus. Stop other audio and try again."
        }

        val outId = out.id
        val devCb = object : AudioDeviceCallback() {
            override fun onAudioDevicesRemoved(removed: Array<out AudioDeviceInfo>) {
                if (removed.any { it.id == outId }) requestStop("Stopped: the earbuds output was lost.")
            }
        }
        am.registerAudioDeviceCallback(devCb, Handler(Looper.getMainLooper()))

        stopReason = null
        running = true
        val bufferMs = ((max(recMin, frame * 4) + max(trkMin, frame * 4)) * 500L / rate).toInt()
        HearingAssistHub.ui.value = AssistUi(true, "Running", "Output: ${out.productName ?: "Bluetooth earbuds"}", bufferMs = bufferMs)
        val main = Handler(Looper.getMainLooper())

        thread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO)
            val buf = ShortArray(frame)
            val chain = HearingChain(rate)
            val limiter = SoftLimiter(0.8f)
            val comp = MildCompressor(rate)
            var ramp = 0f
            val rampStep = frame.toFloat() / rate      // 1 second fade-in
            var hot = 0
            var loops = 0
            var lastSecond = System.currentTimeMillis()
            var elapsed = 0
            try {
                rec.startRecording(); track.play()
                while (running) {
                    val n = rec.read(buf, 0, frame)
                    if (n <= 0) { requestStop("Stopped: the microphone stopped delivering audio."); break }
                    val s = HearingAssistHub.settings.clamped()
                    chain.configure(s)
                    ramp = min(1f, ramp + rampStep)
                    var peak = 0f; var engaged = 0
                    for (i in 0 until n) {
                        var x = buf[i] / 32768f * s.inputGain
                        x = comp.process(chain.filter(x)) * s.outputGain * ramp
                        if (abs(x) > 0.8f) engaged++
                        val y = limiter.process(x)
                        peak = max(peak, abs(y))
                        buf[i] = (y * 32767f).toInt().toShort()
                    }
                    if (track.write(buf, 0, n) < 0) { requestStop("Stopped: audio output error."); break }
                    hot = if (engaged > n / 4) hot + 1 else 0
                    if (hot > 60) { requestStop("Stopped: possible acoustic feedback. Move the phone away from the earbuds and lower the level."); break }
                    if (++loops % 25 == 0 && am.mode != AudioManager.MODE_NORMAL) { requestStop("Stopped: a call or communication app is active."); break }
                    val now = System.currentTimeMillis()
                    if (now - lastSecond >= 1000) { lastSecond = now; elapsed++ }
                    HearingAssistHub.ui.value = HearingAssistHub.ui.value.copy(level = peak, elapsedSec = elapsed)
                }
                // Graceful fade-out (~160 ms) ONLY for a normal user stop. Safety stops (feedback, call, route loss) are immediate.
                if (stopReason == "Stopped.") {
                    var g = 1f
                    repeat(8) {
                        val m = rec.read(buf, 0, frame)
                        if (m > 0) {
                            val s2 = HearingAssistHub.settings.clamped()
                            for (i in 0 until m) {
                                val x = comp.process(chain.filter(buf[i] / 32768f * s2.inputGain)) * s2.outputGain * ramp * g
                                buf[i] = (limiter.process(x) * 32767f).toInt().toShort()
                            }
                            track.write(buf, 0, m)
                        }
                        g = (g - 0.125f).coerceAtLeast(0f)
                    }
                }
            } catch (e: Exception) {
                stopReason = stopReason ?: "Stopped: audio error (${e.javaClass.simpleName})."
            } finally {
                running = false
                runCatching { rec.stop() }; rec.release()
                runCatching { track.stop() }; track.release()
                am.abandonAudioFocusRequest(focus)
                am.unregisterAudioDeviceCallback(devCb)
                val reason = stopReason ?: "Stopped."
                HearingAssistHub.ui.value = AssistUi(false, reason, HearingAssistHub.routeName(ctx))
                main.post { onStopped(reason) }
            }
        }, "HearingAssist").also { it.start() }
        return null
    }

    fun requestStop(reason: String) { if (stopReason == null) stopReason = reason; running = false }

    fun stopAndJoin() { requestStop("Stopped."); thread?.join(1500) }
}

class HearingAssistService : Service() {
    companion object {
        const val ACTION_START = "com.eclipsecompanion.app.ASSIST_START"
        const val ACTION_STOP = "com.eclipsecompanion.app.ASSIST_STOP"
        private const val CHANNEL = "assist"
        private const val NOTIF_ID = 41

        fun start(ctx: Context) = ctx.startForegroundService(Intent(ctx, HearingAssistService::class.java).setAction(ACTION_START))
        fun stop(ctx: Context) { ctx.startService(Intent(ctx, HearingAssistService::class.java).setAction(ACTION_STOP)) }
    }

    private var engine: HearingAssistEngine? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> { engine?.stopAndJoin(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
            else -> {
                val nm = getSystemService(NotificationManager::class.java)
                nm.createNotificationChannel(NotificationChannel(CHANNEL, getString(com.eclipsecompanion.app.R.string.notif_channel_assist), NotificationManager.IMPORTANCE_LOW))
                val stopPi = PendingIntent.getService(this, 1, Intent(this, HearingAssistService::class.java).setAction(ACTION_STOP), PendingIntent.FLAG_IMMUTABLE)
                val openPi = PendingIntent.getActivity(this, 2, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
                val n = Notification.Builder(this, CHANNEL)
                    .setSmallIcon(com.eclipsecompanion.app.R.drawable.ic_stat_assist)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setContentTitle("Hearing Assist is using the microphone")
                    .setContentText("Tap Stop to end the session")
                    .setContentIntent(openPi).setOngoing(true)
                    .addAction(Notification.Action.Builder(null, "Stop", stopPi).build())
                    .build()
                if (Build.VERSION.SDK_INT >= 29) startForeground(NOTIF_ID, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
                else startForeground(NOTIF_ID, n)
                val e = engine ?: HearingAssistEngine(applicationContext).also { engine = it }
                val err = e.start { stopSelf() }
                if (err != null) {
                    HearingAssistHub.ui.value = AssistUi(false, "Stopped", HearingAssistHub.routeName(this), error = err)
                    stopForeground(STOP_FOREGROUND_REMOVE); stopSelf()
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() { engine?.stopAndJoin(); super.onDestroy() }
}
```

---

## 3. System equalizer (audio-effect sessions)

Honest scope: works only with players that announce their audio session through Android's audio-effect broadcast. It does not affect apps that do not (some streaming/video apps).

### `app/src/main/java/com/eclipsecompanion/app/audio/SystemEffects.kt`
```kotlin
package com.eclipsecompanion.app.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import androidx.core.content.ContextCompat
import com.eclipsecompanion.app.domain.EqDefaults
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentHashMap

class EffectSessionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val session = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, -1)
        when (intent.action) {
            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> SystemEffects.open(session)
            AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> SystemEffects.close(session)
        }
    }
}

object SystemEffects {
    private class Fx(val eq: Equalizer?, val bass: BassBoost?, val virt: Virtualizer?, val loud: LoudnessEnhancer?) {
        fun release() { eq?.release(); bass?.release(); virt?.release(); loud?.release() }
    }

    private val sessions = ConcurrentHashMap<Int, Fx>()
    val activeSessions = MutableStateFlow(0)
    @Volatile private var enabled = false
    @Volatile private var gains: List<Float> = EqDefaults.flat()
    @Volatile private var bass = 0          // 0..1000
    @Volatile private var virt = 0          // 0..1000
    @Volatile private var loudMb = 0        // 0..1500 millibel
    @Volatile private var headroom = true   // lower the whole curve by the largest boost so nothing clips
    private var registered = false

    /** Dynamic registration while the app process is alive (manifest receiver covers explicit broadcasts). */
    @Synchronized fun registerDynamic(ctx: Context) {
        if (registered) return
        registered = true
        val f = IntentFilter().apply {
            addAction(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION)
            addAction(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION)
        }
        ContextCompat.registerReceiver(ctx.applicationContext, EffectSessionReceiver(), f, ContextCompat.RECEIVER_EXPORTED)
    }

    fun open(session: Int) {
        if (session <= 0 || sessions.containsKey(session)) return
        val fx = Fx(
            runCatching { Equalizer(0, session) }.getOrNull(),
            runCatching { BassBoost(0, session) }.getOrNull(),
            runCatching { Virtualizer(0, session) }.getOrNull(),
            runCatching { LoudnessEnhancer(session) }.getOrNull()
        )
        sessions[session] = fx
        apply(fx)
        activeSessions.value = sessions.size
    }

    fun close(session: Int) {
        sessions.remove(session)?.release()
        activeSessions.value = sessions.size
    }

    fun update(enabled: Boolean, gains: List<Float>, bass: Int, virt: Int, loudMb: Int, headroom: Boolean = true) {
        this.enabled = enabled
        this.headroom = headroom
        this.gains = EqDefaults.sanitize(gains) ?: EqDefaults.flat()
        this.bass = bass.coerceIn(0, 1000); this.virt = virt.coerceIn(0, 1000); this.loudMb = loudMb.coerceIn(0, 1500)
        sessions.values.forEach { apply(it) }
    }

    private fun apply(fx: Fx) {
        runCatching {
            fx.eq?.let { e ->
                val range = e.bandLevelRange
                for (b in 0 until e.numberOfBands) {
                    val hz = e.getCenterFreq(b.toShort()) / 1000f
                    val head = if (headroom) EqDefaults.peakBoostDb(gains) else 0f
                    val level = ((interpolateGainDb(gains, hz) - head) * 100).toInt().coerceIn(range[0].toInt(), range[1].toInt())
                    e.setBandLevel(b.toShort(), level.toShort())
                }
                e.enabled = enabled
            }
        }
        runCatching { fx.bass?.let { if (it.strengthSupported) it.setStrength(bass.toShort()); it.enabled = enabled && bass > 0 } }
        runCatching { fx.virt?.let { if (it.strengthSupported) it.setStrength(virt.toShort()); it.enabled = enabled && virt > 0 } }
        runCatching { fx.loud?.let { it.setTargetGain(loudMb); it.enabled = enabled && loudMb > 0 } }
    }
}
```

---

## 4. Relaxation sounds and channel test

### `app/src/main/java/com/eclipsecompanion/app/audio/Relaxation.kt`
```kotlin
package com.eclipsecompanion.app.audio

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.content.Context
import android.os.Process
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

enum class RelaxSound(val label: String) {
    WHITE("White noise"), PINK("Pink noise"), BROWN("Brown noise"),
    OCEAN("Ocean waves"), RAIN("Rain"), FAN("Fan"), WIND("Soft wind"), STREAM("Stream")
}

/** Procedurally generated sounds. No assets, no network, no licensing. Max volume is capped. */
class RelaxationPlayer(private val ctx: Context) {
    val playing = MutableStateFlow(false)
    val remainingSec = MutableStateFlow(0)       // 0 = no timer
    @Volatile var volume = 0.2f                  // 0..MAX_VOLUME
    @Volatile private var run = false
    private var thread: Thread? = null
    private val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focus: AudioFocusRequest? = null

    companion object { const val MAX_VOLUME = 0.5f }

    fun start(sound: RelaxSound, minutes: Int) {
        stop()
        val rate = 44_100
        val minBuf = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        if (minBuf <= 0) return
        val attrs = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
        val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setAudioAttributes(attrs)
            .setOnAudioFocusChangeListener { c -> if (c == AudioManager.AUDIOFOCUS_LOSS || c == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) run = false }.build()
        if (am.requestAudioFocus(req) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return
        focus = req
        val track = AudioTrack.Builder().setAudioAttributes(attrs)
            .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setBufferSizeInBytes(max(minBuf, 8192)).setTransferMode(AudioTrack.MODE_STREAM).build()
        run = true; playing.value = true
        remainingSec.value = minutes * 60
        thread = Thread({
            Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO)
            val buf = ShortArray(2048)
            var b0 = 0f; var b1 = 0f; var b2 = 0f; var b3 = 0f; var b4 = 0f; var b5 = 0f; var b6 = 0f  // pink state
            var brown = 0f; var drop = 0f; var t = 0.0; var fade = 0f
            var samples = 0L
            val totalSamples = if (minutes > 0) minutes * 60L * rate else Long.MAX_VALUE
            try {
                track.play()
                while (run && samples < totalSamples) {
                    val v = volume.coerceIn(0f, MAX_VOLUME)
                    // Last 10 s of a timed session fade out smoothly instead of cutting off.
                    val left = totalSamples - samples
                    val endGain = if (minutes > 0 && left < rate * 10L) (left.toFloat() / (rate * 10f)).coerceIn(0f, 1f) else 1f
                    for (i in buf.indices) {
                        val w = Random.nextFloat() * 2f - 1f
                        // Paul Kellet pink filter
                        b0 = 0.99886f * b0 + w * 0.0555179f; b1 = 0.99332f * b1 + w * 0.0750759f
                        b2 = 0.96900f * b2 + w * 0.1538520f; b3 = 0.86650f * b3 + w * 0.3104856f
                        b4 = 0.55000f * b4 + w * 0.5329522f; b5 = -0.7616f * b5 - w * 0.0168980f
                        val pink = (b0 + b1 + b2 + b3 + b4 + b5 + b6 + w * 0.5362f) * 0.11f
                        b6 = w * 0.115926f
                        brown = (brown + 0.02f * w) / 1.02f
                        val br = brown * 3.5f
                        t += 1.0 / rate
                        val s = when (sound) {
                            RelaxSound.WHITE -> w * 0.5f
                            RelaxSound.PINK -> pink
                            RelaxSound.BROWN -> br
                            RelaxSound.OCEAN -> br * (0.55f + 0.45f * sin(2 * PI * t / 9.0).toFloat())
                            RelaxSound.RAIN -> { drop = if (Random.nextFloat() < 0.002f) 0.5f else drop * 0.995f; pink * 0.35f + w * drop * 0.3f }
                            RelaxSound.FAN -> br * 0.6f + pink * 0.15f + 0.05f * sin(2 * PI * 120 * t).toFloat()
                            RelaxSound.WIND -> br * (0.35f + 0.65f * (0.5f + 0.5f * sin(2 * PI * t / 11.0).toFloat()))
                            RelaxSound.STREAM -> pink * 0.6f + w * 0.12f * (0.5f + 0.5f * sin(2 * PI * t * 0.7).toFloat())
                        }
                        fade = min(1f, fade + 1f / (rate * 2f))        // 2 s fade-in
                        buf[i] = (s.coerceIn(-1f, 1f) * v * fade * endGain * 32767f).toInt().toShort()
                    }
                    if (track.write(buf, 0, buf.size) < 0) break
                    samples += buf.size
                    if (minutes > 0) remainingSec.value = ((totalSamples - samples) / rate).toInt().coerceAtLeast(0)
                }
            } finally {
                runCatching { track.stop() }; track.release()
                focus?.let { am.abandonAudioFocusRequest(it) }; focus = null
                run = false; playing.value = false; remainingSec.value = 0
            }
        }, "Relaxation").also { it.start() }
    }

    fun stop() { run = false; thread?.join(1500); thread = null }
}

/** Short left/right test tones at a fixed LOW level (amplitude 0.08). */
object ToneTest {
    fun play(scope: CoroutineScope, left: Boolean, onDone: () -> Unit) {
        scope.launch(Dispatchers.IO) {
            val rate = 44_100; val seconds = 1.5; val frames = (rate * seconds).toInt()
            val data = ShortArray(frames * 2)
            for (i in 0 until frames) {
                val env = min(1f, min(i, frames - i) / (rate * 0.1f))    // 100 ms fades
                val v = (sin(2 * PI * 440.0 * i / rate) * 0.08 * env * 32767).toInt().toShort()
                if (left) data[2 * i] = v else data[2 * i + 1] = v
            }
            val track = AudioTrack.Builder()
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build())
                .setBufferSizeInBytes(data.size * 2).setTransferMode(AudioTrack.MODE_STATIC).build()
            try { track.write(data, 0, data.size); track.play(); kotlinx.coroutines.delay((seconds * 1000).toLong() + 150) }
            finally { runCatching { track.stop() }; track.release(); onDone() }
        }
    }
}
```

---

## 5. Local storage

### `app/src/main/java/com/eclipsecompanion/app/data/SettingsStore.kt`
```kotlin
package com.eclipsecompanion.app.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.eclipsecompanion.app.domain.EqDefaults
import com.eclipsecompanion.app.domain.EqProfile
import com.eclipsecompanion.app.domain.SpeechClarity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore by preferencesDataStore(name = "eclipse_settings")

data class AppSettings(
    val theme: String = "system",               // system | light | dark
    val simpleMode: Boolean = false,
    val alias: String = "My Eclipse",
    val deviceAddress: String? = null,
    val deviceName: String? = null,
    val profileJson: String? = null,            // imported protocol profile
    val customEq: List<EqProfile> = emptyList(),
    val currentEqId: String = "builtin_flat",
    val currentGains: List<Float> = EqDefaults.flat(),
    val touchDrafts: Map<String, String> = emptyMap(),
    val lowBattery: Int = 20,
    val criticalBattery: Int = 10,
    val systemEqEnabled: Boolean = false,
    val bassBoost: Int = 0,
    val virtualizer: Int = 0,
    val loudnessMb: Int = 0,
    val clarity: SpeechClarity = SpeechClarity.OFF,
    val rumbleCut: Boolean = true,
    val breakReminderMin: Int = 30,
    val firstRunDone: Boolean = false,
    val preferredMode: String? = null,          // ListeningMode.key; only ever a SUGGESTION, never written silently
    val autoHeadroom: Boolean = true,
    val haptics: Boolean = true,
    val listenReminderMin: Int = 0,             // 0 = off
    val loaded: Boolean = false                 // false only for the placeholder emitted before DataStore has been read
)

object Json {
    fun eqToJson(list: List<EqProfile>): String = JSONArray().also { a ->
        list.forEach { p -> a.put(JSONObject().put("id", p.id).put("name", p.name).put("gains", JSONArray(p.gains.map { it.toDouble() }))) }
    }.toString()

    fun eqFromJson(s: String?): List<EqProfile> {
        if (s.isNullOrBlank()) return emptyList()
        return try {
            val a = JSONArray(s)
            (0 until a.length()).mapNotNull { i ->
                val o = a.getJSONObject(i)
                val g = o.getJSONArray("gains")
                val gains = (0 until g.length()).map { g.getDouble(it).toFloat() }
                EqDefaults.sanitize(gains)?.let { EqProfile(o.getString("id"), o.getString("name").take(40), it) }
            }
        } catch (e: Exception) { emptyList() }
    }

    fun mapToJson(m: Map<String, String>): String = JSONObject(m).toString()
    fun mapFromJson(s: String?): Map<String, String> {
        if (s.isNullOrBlank()) return emptyMap()
        return try { val o = JSONObject(s); o.keys().asSequence().associateWith { o.getString(it) } } catch (e: Exception) { emptyMap() }
    }
}

class SettingsStore(private val context: Context) {
    private object K {
        val theme = stringPreferencesKey("theme"); val simple = booleanPreferencesKey("simple")
        val alias = stringPreferencesKey("alias"); val addr = stringPreferencesKey("addr"); val dname = stringPreferencesKey("dname")
        val profile = stringPreferencesKey("profile"); val custom = stringPreferencesKey("custom_eq")
        val eqId = stringPreferencesKey("eq_id"); val gains = stringPreferencesKey("gains"); val touch = stringPreferencesKey("touch")
        val low = intPreferencesKey("low"); val crit = intPreferencesKey("crit")
        val sysEq = booleanPreferencesKey("sys_eq"); val bass = intPreferencesKey("bass"); val virt = intPreferencesKey("virt"); val loud = intPreferencesKey("loud")
        val clarity = stringPreferencesKey("clarity"); val rumble = booleanPreferencesKey("rumble"); val brk = intPreferencesKey("break_min")
        val firstRun = booleanPreferencesKey("first_run"); val prefMode = stringPreferencesKey("pref_mode")
        val headroom = booleanPreferencesKey("headroom"); val haptics = booleanPreferencesKey("haptics"); val listenMin = intPreferencesKey("listen_min")
    }

    val flow: Flow<AppSettings> = context.dataStore.data.map { p: Preferences ->
        val gains = p[K.gains]?.split(",")?.mapNotNull { it.toFloatOrNull() }?.let { EqDefaults.sanitize(it) } ?: EqDefaults.flat()
        AppSettings(
            theme = p[K.theme] ?: "system", simpleMode = p[K.simple] ?: false, alias = p[K.alias] ?: "My Eclipse",
            deviceAddress = p[K.addr], deviceName = p[K.dname], profileJson = p[K.profile],
            customEq = Json.eqFromJson(p[K.custom]), currentEqId = p[K.eqId] ?: "builtin_flat", currentGains = gains,
            touchDrafts = Json.mapFromJson(p[K.touch]),
            lowBattery = p[K.low] ?: 20, criticalBattery = p[K.crit] ?: 10,
            systemEqEnabled = p[K.sysEq] ?: false, bassBoost = p[K.bass] ?: 0, virtualizer = p[K.virt] ?: 0, loudnessMb = p[K.loud] ?: 0,
            clarity = runCatching { SpeechClarity.valueOf(p[K.clarity] ?: "OFF") }.getOrDefault(SpeechClarity.OFF),
            rumbleCut = p[K.rumble] ?: true, breakReminderMin = p[K.brk] ?: 30,
            firstRunDone = p[K.firstRun] ?: false, preferredMode = p[K.prefMode], autoHeadroom = p[K.headroom] ?: true,
            haptics = p[K.haptics] ?: true, listenReminderMin = p[K.listenMin] ?: 0, loaded = true
        )
    }

    suspend fun setTheme(v: String) = context.dataStore.edit { it[K.theme] = v }
    suspend fun setSimple(v: Boolean) = context.dataStore.edit { it[K.simple] = v }
    suspend fun setAlias(v: String) = context.dataStore.edit { it[K.alias] = v.take(30) }
    suspend fun setDevice(addr: String?, name: String?) = context.dataStore.edit {
        if (addr == null) { it.remove(K.addr); it.remove(K.dname) } else { it[K.addr] = addr; it[K.dname] = name ?: "" }
    }
    suspend fun setProfile(json: String?) = context.dataStore.edit { if (json == null) it.remove(K.profile) else it[K.profile] = json }
    suspend fun setCustomEq(l: List<EqProfile>) = context.dataStore.edit { it[K.custom] = Json.eqToJson(l) }
    suspend fun setCurrentEq(id: String, g: List<Float>) = context.dataStore.edit { it[K.eqId] = id; it[K.gains] = g.joinToString(",") }
    suspend fun setTouchDrafts(m: Map<String, String>) = context.dataStore.edit { it[K.touch] = Json.mapToJson(m) }
    suspend fun setThresholds(low: Int, crit: Int) = context.dataStore.edit { it[K.low] = low.coerceIn(5, 50); it[K.crit] = crit.coerceIn(1, 30) }
    suspend fun setFx(on: Boolean, bass: Int, virt: Int, loud: Int) = context.dataStore.edit {
        it[K.sysEq] = on; it[K.bass] = bass; it[K.virt] = virt; it[K.loud] = loud
    }
    suspend fun setHearingPrefs(c: SpeechClarity, rumble: Boolean) = context.dataStore.edit { it[K.clarity] = c.name; it[K.rumble] = rumble }
    suspend fun setBreakMin(v: Int) = context.dataStore.edit { it[K.brk] = v.coerceIn(5, 120) }
    suspend fun setFirstRunDone(v: Boolean) = context.dataStore.edit { it[K.firstRun] = v }
    suspend fun setPreferredMode(key: String?) = context.dataStore.edit { if (key == null) it.remove(K.prefMode) else it[K.prefMode] = key }
    suspend fun setAutoHeadroom(v: Boolean) = context.dataStore.edit { it[K.headroom] = v }
    suspend fun setHaptics(v: Boolean) = context.dataStore.edit { it[K.haptics] = v }
    suspend fun setListenReminder(min: Int) = context.dataStore.edit { it[K.listenMin] = min.coerceIn(0, 240) }
    suspend fun clearAll() = context.dataStore.edit { it.clear() }
}
```

---

## 6. ViewModel

### `app/src/main/java/com/eclipsecompanion/app/AppViewModel.kt`
```kotlin
package com.eclipsecompanion.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.eclipsecompanion.app.audio.AssistUi
import com.eclipsecompanion.app.audio.HearingAssistHub
import com.eclipsecompanion.app.audio.HearingAssistService
import com.eclipsecompanion.app.audio.RelaxSound
import com.eclipsecompanion.app.audio.RelaxationPlayer
import com.eclipsecompanion.app.audio.SystemEffects
import com.eclipsecompanion.app.audio.ToneTest
import com.eclipsecompanion.app.bluetooth.Candidate
import com.eclipsecompanion.app.bluetooth.EarbudController
import com.eclipsecompanion.app.bluetooth.Scanner
import com.eclipsecompanion.app.bluetooth.SystemMonitor
import com.eclipsecompanion.app.bluetooth.Match
import com.eclipsecompanion.app.data.AppSettings
import com.eclipsecompanion.app.data.Json
import com.eclipsecompanion.app.data.SettingsStore
import com.eclipsecompanion.app.domain.CommandResult
import com.eclipsecompanion.app.domain.EqDefaults
import com.eclipsecompanion.app.domain.EqProfile
import com.eclipsecompanion.app.domain.GENERIC_TOUCH_ACTIONS
import com.eclipsecompanion.app.domain.Gesture
import com.eclipsecompanion.app.domain.HearingSettings
import com.eclipsecompanion.app.domain.ListeningMode
import com.eclipsecompanion.app.domain.Phase
import com.eclipsecompanion.app.domain.Reported
import com.eclipsecompanion.app.domain.Side
import com.eclipsecompanion.app.domain.SpeechClarity
import com.eclipsecompanion.app.domain.touchKey
import com.eclipsecompanion.app.protocol.ProtocolProfile
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

const val APP_VERSION = "1.0.0"

data class EqState(val id: String, val gains: List<Float>)

class AppViewModel(app: Application) : AndroidViewModel(app) {
    private val ctx = app.applicationContext
    val store = SettingsStore(ctx)
    val controller = EarbudController(ctx, viewModelScope)
    val scanner = Scanner(ctx)
    val relax = RelaxationPlayer(ctx)
    val system = SystemMonitor(ctx)

    val settings: StateFlow<AppSettings> = store.flow.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())
    val ready = MutableStateFlow(false)

    val eq = MutableStateFlow(EqState("builtin_flat", EqDefaults.flat()))
    val eqStatus = MutableStateFlow("Saved on this phone only.")
    val hearing = MutableStateFlow(HearingSettings())
    val touchDraft = MutableStateFlow<Map<String, String>>(emptyMap())
    private var touchBase: Map<String, String> = emptyMap()
    val touchStatus = MutableStateFlow<String?>(null)
    val profileStatus = MutableStateFlow("Using the bundled profile (nothing verified).")
    val soundTab = MutableStateFlow("Modes")                       // Modes | Equalizer | Effects
    val modePrompt = MutableStateFlow<ListeningMode?>(null)        // "switch back to your preferred mode?" suggestion
    private var promptDismissed = false
    @Volatile private var foreground = true
    private var connectedSince = 0L
    private var remindCount = 0
    private var connectJob: Job? = null
    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val events: SharedFlow<String> = _events
    private fun emit(msg: String) { _events.tryEmit(msg) }

    private val warned = mutableSetOf<String>()
    private var lastBreakSec = 0
    private var scanJob: Job? = null

    init {
        SystemEffects.registerDynamic(ctx)
        viewModelScope.launch {
            val s = settings.first { it.loaded }
            eq.value = EqState(s.currentEqId, s.currentGains)
            hearing.value = HearingSettings(clarity = s.clarity, rumbleCut = s.rumbleCut).clamped()
            HearingAssistHub.settings = hearing.value
            touchDraft.value = s.touchDrafts; touchBase = s.touchDrafts
            loadProfile(s.profileJson)
            ready.value = true
            // Auto-reconnect only to the previously selected device, only if permission already granted.
            if (s.deviceAddress != null && com.eclipsecompanion.app.bluetooth.Perms.bluetoothGranted(ctx)) {
                controller.connect(s.deviceAddress, s.deviceName ?: "Earbuds")
            }
        }
        viewModelScope.launch {
            combine(settings, eq) { s, e -> s to e }.collect { (s, e) ->
                SystemEffects.update(s.systemEqEnabled, e.gains, s.bassBoost, s.virtualizer, s.loudnessMb, s.autoHeadroom)
            }
        }
        viewModelScope.launch {
            controller.battery.collect { b ->
                val s = settings.value
                listOf("Left earbud" to b.left, "Right earbud" to b.right, "Case" to b.case, "Earbuds" to b.combined).forEach { (n, r) ->
                    val v = (r as? Reported.Available)?.value ?: return@forEach
                    if (v > s.lowBattery + 5) { warned.remove("$n-l"); warned.remove("$n-c") }
                    else if (v <= s.criticalBattery) { if (warned.add("$n-c")) emit("$n battery is critically low ($v%).") }
                    else if (v <= s.lowBattery) { if (warned.add("$n-l")) emit("$n battery is low ($v%).") }
                }
            }
        }
        viewModelScope.launch {
            HearingAssistHub.ui.collect { u ->
                val every = settings.value.breakReminderMin * 60
                if (u.running && u.elapsedSec > 0 && u.elapsedSec % every == 0 && u.elapsedSec != lastBreakSec) {
                    lastBreakSec = u.elapsedSec; emit("You have used Hearing Assist for a while. Consider a quiet break.")
                }
                if (!u.running) lastBreakSec = 0
            }
        }
        // Android Bluetooth state + audio route (separate from the app's control link).
        system.start()
        // Conservative battery/mode polling: once a minute, only while connected AND the app is visible.
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                if (foreground && controller.phase.value == Phase.READY) controller.refresh()
            }
        }
        // Optional listening reminder (local, in-app only).
        viewModelScope.launch {
            controller.phase.collect { ph ->
                if (ph == Phase.READY) { if (connectedSince == 0L) { connectedSince = System.currentTimeMillis(); remindCount = 0 } }
                else connectedSince = 0L
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                val every = settings.value.listenReminderMin
                if (every > 0 && connectedSince > 0L) {
                    val n = ((System.currentTimeMillis() - connectedSince) / (every * 60_000L)).toInt()
                    if (n > remindCount) { remindCount = n; emit("You have been connected for about ${n * every} minutes. Consider a short break and keep the volume comfortable.") }
                }
            }
        }
        // Suggest (never silently apply) the preferred listening mode after connecting.
        viewModelScope.launch {
            combine(controller.phase, controller.confirmedMode, settings) { ph, m, st -> Triple(ph, m, st) }.collect { (ph, m, st) ->
                if (ph != Phase.READY) promptDismissed = false
                val pref = st.preferredMode?.let { k -> ListeningMode.values().firstOrNull { it.key == k } }
                val cur = (m as? Reported.Available)?.value
                modePrompt.value = if (ph == Phase.READY && pref != null && cur != null && cur != pref && controller.can("setMode") && !promptDismissed) pref else null
            }
        }
    }

    /* ---------------- protocol profile */

    /** Blank template for the user to fill in (Protocol screen: Save / Copy template). */
    fun templateText(): String = bundledProfileText() ?: ""

    private fun bundledProfileText(): String? = runCatching { ctx.assets.open("protocol/eclipse_r7065.json").bufferedReader().use { it.readText() } }.getOrNull()

    private fun loadProfile(stored: String?) {
        val text = stored ?: bundledProfileText()
        val r = text?.let { ProtocolProfile.fromJson(it) }
        if (r != null && r.isSuccess) {
            controller.setProfile(r.getOrThrow())
            profileStatus.value = if (stored != null) "Imported profile: ${r.getOrThrow().name}" else "Using the bundled profile (nothing verified)."
        } else {
            controller.setProfile(ProtocolProfile.EMPTY)
            profileStatus.value = "Profile could not be loaded: ${r?.exceptionOrNull()?.message ?: "missing"}. Using an empty profile."
        }
    }

    fun importProfile(text: String) {
        val r = ProtocolProfile.fromJson(text)
        if (r.isFailure) { profileStatus.value = r.exceptionOrNull()?.message ?: "Invalid profile."; return }
        val p = r.getOrThrow()
        controller.setProfile(p)
        viewModelScope.launch { store.setProfile(text) }
        val v = p.commands.filterValues { it.verified }.keys
        profileStatus.value = "Imported '${p.name}'. Verified commands: ${if (v.isEmpty()) "none" else v.joinToString()}. Reconnect your earbuds to use it."
    }

    fun resetProfile() {
        viewModelScope.launch { store.setProfile(null) }
        loadProfile(null)
    }

    /* ---------------- connection */

    fun startScan() { scanJob?.cancel(); scanJob = viewModelScope.launch { scanner.scan(controller.profile.value) } }
    fun stopScan() { scanJob?.cancel() }
    fun bonded(): List<Candidate> = scanner.bonded(controller.profile.value)

    /** Paired Android devices whose name/service looks like an Eclipse: powers the Home "Compatible earbuds found" card (no scan needed). */
    fun bondedMatches(): List<Candidate> = bonded().filter { it.match != Match.UNKNOWN }

    fun setForeground(v: Boolean) { foreground = v }
    fun dismissModePrompt() { promptDismissed = true; modePrompt.value = null }
    fun acceptModePrompt() { val m = modePrompt.value; promptDismissed = true; modePrompt.value = null; if (m != null) setMode(m) }

    fun connect(c: Candidate) {
        stopScan()
        connectJob?.cancel()
        connectJob = viewModelScope.launch {
            controller.connect(c.address, c.name)
            if (controller.phase.value == Phase.READY) store.setDevice(c.address, c.name)
        }
    }

    /** Cancel an attempt that is taking too long (the official app can hang on "Identifying"; this one never does). */
    fun cancelConnect() { connectJob?.cancel(); controller.disconnect() }

    fun reconnectSaved() {
        val s = settings.value
        val a = s.deviceAddress ?: return
        connectJob?.cancel()
        connectJob = viewModelScope.launch { controller.connect(a, s.deviceName ?: "Earbuds") }
    }

    fun disconnect() { connectJob?.cancel(); controller.disconnect() }
    fun forgetDevice() { controller.disconnect(); viewModelScope.launch { store.setDevice(null, null) } }
    fun refresh() { viewModelScope.launch { controller.refresh() } }

    /* ---------------- modes */

    fun setMode(m: ListeningMode) { viewModelScope.launch { controller.setMode(m) } }
    fun setGaming(on: Boolean) { viewModelScope.launch { controller.setGaming(on) } }
    fun setAncLevel(level: Int) { viewModelScope.launch { controller.setAncLevel(level) } }
    fun setWearDetection(on: Boolean) { viewModelScope.launch { controller.setWearDetection(on) } }
    fun setVoicePrompts(on: Boolean) { viewModelScope.launch { controller.setVoicePrompts(on) } }
    fun setTransparencyLevel(l: Int) { viewModelScope.launch { controller.setTransparencyLevel(l) } }
    fun renameEarbuds(name: String) { viewModelScope.launch { controller.setName(name) } }
    fun factoryReset() { viewModelScope.launch { controller.factoryReset() } }

    /* ---------------- equalizer */

    fun allEqProfiles(): List<EqProfile> = EqDefaults.PRESETS + settings.value.customEq

    private fun persistEq() { val e = eq.value; viewModelScope.launch { store.setCurrentEq(e.id, e.gains) } }

    fun selectEq(p: EqProfile) { eq.value = EqState(p.id, p.gains); eqStatus.value = "Saved on this phone only."; persistEq() }

    fun setBand(index: Int, value: Float) {
        val e = eq.value
        eq.value = EqState("edited", EqDefaults.withBand(e.gains, index, value))   // only this band changes
        persistEq()
    }

    fun resetEq() = selectEq(EqDefaults.PRESETS.first())

    fun saveEqAs(name: String) {
        val n = name.trim().take(40).ifEmpty { return }
        val p = EqProfile("custom_${System.currentTimeMillis()}", n, eq.value.gains)
        val list = (settings.value.customEq + p).takeLast(50)
        viewModelScope.launch { store.setCustomEq(list) }
        eq.value = EqState(p.id, p.gains); persistEq()
    }

    fun deleteEq(id: String) {
        if (id.startsWith("builtin_")) return
        viewModelScope.launch { store.setCustomEq(settings.value.customEq.filterNot { it.id == id }) }
    }

    fun renameEq(id: String, name: String) {
        val n = name.trim().take(40).ifEmpty { return }
        viewModelScope.launch { store.setCustomEq(settings.value.customEq.map { if (it.id == id) it.copy(name = n) else it }) }
    }

    fun duplicateEq(id: String) {
        val src = allEqProfiles().firstOrNull { it.id == id } ?: return
        val copy = EqProfile("custom_${System.currentTimeMillis()}", "${src.name} copy".take(40), src.gains)
        viewModelScope.launch { store.setCustomEq((settings.value.customEq + copy).takeLast(50)) }
    }

    fun exportEqJson(): String = Json.eqToJson(settings.value.customEq)

    fun importEqJson(text: String): Int {
        val list = Json.eqFromJson(text)
        if (list.isEmpty()) return 0
        val stamp = System.currentTimeMillis()
        val fresh = list.mapIndexed { i, p -> p.copy(id = "custom_${stamp}_$i") }
        viewModelScope.launch { store.setCustomEq((settings.value.customEq + fresh).takeLast(50)) }
        return fresh.size
    }

    fun applyEqToEarbuds() {
        viewModelScope.launch {
            eqStatus.value = when (val r = controller.setEq(eq.value.gains)) {
                is CommandResult.Confirmed -> "Applied to earbuds (confirmed)."
                is CommandResult.SentUnconfirmed -> "Sent to earbuds, not confirmed."
                is CommandResult.Rejected -> "Not applied: ${r.reason}"
                is CommandResult.Unsupported -> "Not applied: ${r.reason}"
            }
        }
    }

    fun setFx(on: Boolean, bass: Int, virt: Int, loudMb: Int) { viewModelScope.launch { store.setFx(on, bass, virt, loudMb) } }

    /* ---------------- touch controls */

    fun touchActions(): List<String> = controller.profile.value.touch?.actions?.keys?.toList()?.ifEmpty { null } ?: GENERIC_TOUCH_ACTIONS

    fun setTouchDraft(side: Side, g: Gesture, action: String) { touchDraft.value = touchDraft.value + (touchKey(side, g) to action) }

    fun saveTouchLocal(side: Side) {
        touchBase = touchBase.filterKeys { !it.startsWith(side.name) } + touchDraft.value.filterKeys { it.startsWith(side.name) }
        viewModelScope.launch { store.setTouchDrafts(touchBase) }
        touchStatus.value = "Saved on this phone only."
    }

    fun cancelTouch(side: Side) {
        touchDraft.value = touchDraft.value.filterKeys { !it.startsWith(side.name) } + touchBase.filterKeys { it.startsWith(side.name) }
        touchStatus.value = null
    }

    fun applyTouch(side: Side) {
        viewModelScope.launch {
            val mine = touchDraft.value.filterKeys { it.startsWith(side.name) }
            if (mine.isEmpty()) { touchStatus.value = "Nothing to apply."; return@launch }
            var ok = 0; var fail: String? = null
            for ((k, action) in mine) {
                val gesture = k.substringAfter(":")
                when (val r = controller.setTouch(side.name, gesture, action)) {
                    is CommandResult.Confirmed -> ok++
                    is CommandResult.SentUnconfirmed -> { ok++ }
                    is CommandResult.Rejected -> { fail = r.reason; break }
                    is CommandResult.Unsupported -> { fail = r.reason; break }
                }
            }
            if (fail == null) { saveTouchLocal(side); touchStatus.value = "Applied $ok gesture(s) to the earbuds." }
            else touchStatus.value = "Not applied: $fail"
        }
    }

    /* ---------------- Hearing Assist */

    fun updateHearing(f: (HearingSettings) -> HearingSettings) {
        val n = f(hearing.value).clamped()
        hearing.value = n; HearingAssistHub.settings = n
        viewModelScope.launch { store.setHearingPrefs(n.clarity, n.rumbleCut) }
    }

    fun assistPreset(name: String) = updateHearing {
        when (name) {
            "Conversation" -> it.copy(inputGain = 1.5f, outputGain = 0.2f, clarity = SpeechClarity.MODERATE, rumbleCut = true)
            "TV" -> it.copy(inputGain = 1.0f, outputGain = 0.2f, clarity = SpeechClarity.MILD, rumbleCut = true)
            "Restaurant" -> it.copy(inputGain = 2.0f, outputGain = 0.2f, clarity = SpeechClarity.MODERATE, rumbleCut = true)
            "Outdoors" -> it.copy(inputGain = 1.2f, outputGain = 0.18f, clarity = SpeechClarity.MILD, rumbleCut = true)
            else -> it.copy(inputGain = 2.0f, outputGain = 0.15f, clarity = SpeechClarity.MILD, rumbleCut = true)   // Quiet room
        }
    }

    fun startAssist() {
        updateHearing { it.copy(outputGain = HearingSettings.SAFE_OUTPUT_START) }     // every session starts quiet
        HearingAssistHub.ui.value = AssistUi(false, "Starting…", HearingAssistHub.routeName(ctx))
        HearingAssistService.start(ctx)
    }

    fun stopAssist() = HearingAssistService.stop(ctx)
    fun refreshRoute() { if (!HearingAssistHub.ui.value.running) HearingAssistHub.ui.value = HearingAssistHub.ui.value.copy(route = HearingAssistHub.routeName(ctx)) }

    /* ---------------- relaxation / tests */

    fun playRelax(sound: RelaxSound, minutes: Int) = relax.start(sound, minutes)
    fun stopRelax() = relax.stop()
    fun playTone(left: Boolean, onDone: () -> Unit) = ToneTest.play(viewModelScope, left, onDone)

    /* ---------------- settings */

    fun setTheme(v: String) { viewModelScope.launch { store.setTheme(v) } }
    fun setSimple(v: Boolean) { viewModelScope.launch { store.setSimple(v) } }
    fun setAlias(v: String) { viewModelScope.launch { store.setAlias(v) } }
    fun setThresholds(low: Int, crit: Int) { viewModelScope.launch { store.setThresholds(low, minOf(crit, low - 1)) } }
    fun setBreakMin(v: Int) { viewModelScope.launch { store.setBreakMin(v) } }
    fun setFirstRunDone(v: Boolean) { viewModelScope.launch { store.setFirstRunDone(v) } }
    fun setPreferredMode(key: String?) { viewModelScope.launch { store.setPreferredMode(key) } }
    fun setAutoHeadroom(v: Boolean) { viewModelScope.launch { store.setAutoHeadroom(v) } }
    fun setHaptics(v: Boolean) { viewModelScope.launch { store.setHaptics(v) } }
    fun setListenReminder(min: Int) { viewModelScope.launch { store.setListenReminder(min) } }

    fun diagnostics(): String = controller.diagnostics(APP_VERSION)

    /** Backup contains settings only: no device address, no profile, no audio. */
    fun backupJson(): String {
        val s = settings.value
        return JSONObject()
            .put("app", "EclipseCompanion").put("version", 1)
            .put("theme", s.theme).put("simpleMode", s.simpleMode).put("alias", s.alias)
            .put("customEq", org.json.JSONArray(Json.eqToJson(s.customEq)))
            .put("touch", JSONObject(touchBase)).put("low", s.lowBattery).put("critical", s.criticalBattery)
            .put("breakMin", s.breakReminderMin).put("haptics", s.haptics).put("autoHeadroom", s.autoHeadroom)
            .put("listenReminderMin", s.listenReminderMin).put("preferredMode", s.preferredMode ?: "").toString(2)
    }

    fun restoreBackup(text: String): Boolean = try {
        val o = JSONObject(text)
        require(o.optString("app") == "EclipseCompanion")
        viewModelScope.launch {
            store.setTheme(o.optString("theme", "system")); store.setSimple(o.optBoolean("simpleMode", false))
            store.setAlias(o.optString("alias", "My Eclipse"))
            store.setCustomEq(Json.eqFromJson(o.optJSONArray("customEq")?.toString()))
            o.optJSONObject("touch")?.let { t -> store.setTouchDrafts(t.keys().asSequence().associateWith { t.getString(it) }) }
            store.setThresholds(o.optInt("low", 20), o.optInt("critical", 10)); store.setBreakMin(o.optInt("breakMin", 30))
            store.setHaptics(o.optBoolean("haptics", true)); store.setAutoHeadroom(o.optBoolean("autoHeadroom", true))
            store.setListenReminder(o.optInt("listenReminderMin", 0)); store.setPreferredMode(o.optString("preferredMode", "").ifEmpty { null })
        }
        true
    } catch (e: Exception) { false }

    fun clearAllData() {
        controller.disconnect(); relax.stop()
        viewModelScope.launch {
            store.clearAll()
            eq.value = EqState("builtin_flat", EqDefaults.flat()); touchDraft.value = emptyMap(); touchBase = emptyMap()
            hearing.value = HearingSettings(); HearingAssistHub.settings = hearing.value
            loadProfile(null)
        }
    }

    override fun onCleared() { relax.stop(); stopScan(); system.stop(); super.onCleared() }
}
```

---

## 7. MainActivity and host bridge

### `app/src/main/java/com/eclipsecompanion/app/MainActivity.kt`
```kotlin
package com.eclipsecompanion.app

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eclipsecompanion.app.bluetooth.Perms
import com.eclipsecompanion.app.ui.EclipseApp
import com.eclipsecompanion.app.ui.EclipseTheme

/** Everything the Compose UI needs from the Activity (permissions, file pickers, clipboard). */
interface Host {
    fun requestBluetooth(then: () -> Unit)
    fun requestAssist(then: () -> Unit)
    fun pickJson(onText: (String) -> Unit)
    fun saveJson(fileName: String, text: String)
    fun openBluetoothSettings()
    fun openAppSettings()
    fun copy(label: String, text: String)
    fun toast(msg: String)
}

class MainActivity : ComponentActivity(), Host {
    private val vm: AppViewModel by viewModels()
    private var afterBluetooth: (() -> Unit)? = null
    private var afterAssist: (() -> Unit)? = null
    private var onPicked: ((String) -> Unit)? = null
    private var pendingSave: String? = null
    /** Where a launcher shortcut / a music player's "Equalizer" button wants to go: assist | equalizer | relax | null. */
    private val route = androidx.compose.runtime.mutableStateOf<String?>(null)

    private fun routeFrom(i: Intent?) {
        route.value = when {
            i == null -> null
            i.action == AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL -> "equalizer"
            else -> i.getStringExtra("open")?.takeIf { it in setOf("assist", "equalizer", "relax") }
        }
    }

    private val btLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r.values.all { it }) afterBluetooth?.invoke() else toast("Nearby-devices permission was denied. You can allow it in Android settings.")
        afterBluetooth = null
    }
    private val assistLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { r ->
        if (r[Manifest.permission.RECORD_AUDIO] == true || Perms.micGranted(this)) afterAssist?.invoke()
        else toast("Microphone permission is required for Hearing Assist.")
        afterAssist = null
    }
    private val openDoc = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        val cb = onPicked; onPicked = null
        if (uri != null && cb != null) {
            val text = try {
                contentResolver.openInputStream(uri)?.use { s ->
                    val bytes = s.readNBytesCompat(200_000)
                    String(bytes, Charsets.UTF_8)
                }
            } catch (e: Exception) { null }
            if (text == null) toast("Could not read that file.") else cb(text)
        }
    }
    private val createDoc = registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val t = pendingSave; pendingSave = null
        if (uri != null && t != null) {
            try { contentResolver.openOutputStream(uri)?.use { it.write(t.toByteArray(Charsets.UTF_8)) }; toast("Saved.") }
            catch (e: Exception) { toast("Could not save the file.") }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) routeFrom(intent)
        setContent {
            val s by vm.settings.collectAsStateWithLifecycle()
            EclipseTheme(s.theme) { EclipseApp(vm, this@MainActivity, route.value) { route.value = null } }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        routeFrom(intent)
    }

    override fun onStart() { super.onStart(); vm.setForeground(true) }
    override fun onStop() { vm.setForeground(false); super.onStop() }

    override fun requestBluetooth(then: () -> Unit) {
        if (Perms.bluetoothGranted(this)) then() else { afterBluetooth = then; btLauncher.launch(Perms.bluetooth()) }
    }

    override fun requestAssist(then: () -> Unit) {
        val perms = buildList {
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
        }.toTypedArray()
        if (Perms.micGranted(this) && (Build.VERSION.SDK_INT < 33 || Perms.granted(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS)))) then()
        else { afterAssist = then; assistLauncher.launch(perms) }
    }

    override fun pickJson(onText: (String) -> Unit) { onPicked = onText; openDoc.launch(arrayOf("application/json", "text/plain", "*/*")) }
    override fun saveJson(fileName: String, text: String) { pendingSave = text; createDoc.launch(fileName) }
    override fun openBluetoothSettings() { startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS)) }
    override fun openAppSettings() {
        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
    }
    override fun copy(label: String, text: String) {
        (getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(label, text))
        toast("Copied.")
    }
    override fun toast(msg: String) { Toast.makeText(this, msg, Toast.LENGTH_LONG).show() }
}

/** Reads at most [max] bytes (API 26 compatible replacement for readNBytes). */
private fun java.io.InputStream.readNBytesCompat(max: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buf = ByteArray(8192)
    var total = 0
    while (total < max) {
        val n = read(buf, 0, minOf(buf.size, max - total))
        if (n < 0) break
        out.write(buf, 0, n); total += n
    }
    return out.toByteArray()
}
```

---

## 8. Unit tests

### `app/src/test/java/com/eclipsecompanion/app/CoreTests.kt`
```kotlin
package com.eclipsecompanion.app

import com.eclipsecompanion.app.audio.SoftLimiter
import com.eclipsecompanion.app.audio.interpolateGainDb
import com.eclipsecompanion.app.domain.BatteryState
import com.eclipsecompanion.app.domain.EqDefaults
import com.eclipsecompanion.app.domain.HearingSettings
import com.eclipsecompanion.app.domain.Reported
import com.eclipsecompanion.app.domain.mergeBattery
import com.eclipsecompanion.app.domain.validBattery
import com.eclipsecompanion.app.protocol.BatteryParse
import com.eclipsecompanion.app.protocol.FrameCodec
import com.eclipsecompanion.app.protocol.Framing
import com.eclipsecompanion.app.protocol.Hex
import com.eclipsecompanion.app.protocol.ProtocolProfile
import com.eclipsecompanion.app.protocol.ResponseParsers
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreTests {
    private val sumFraming = Framing(byteArrayOf(0xAA.toByte()), true, "sum8", 0)

    @Test fun frameBuildsWithLengthAndChecksum() {
        val f = FrameCodec.build(sumFraming, "01 {mode}", mapOf("mode" to 2), emptyMap())
        assertArrayEquals(Hex.parse("AA 02 01 02 AF"), f)
    }

    @Test fun frameParseRoundTripAndRejectsCorruption() {
        val frame = Hex.parse("AA 02 01 02 AF")!!
        assertArrayEquals(Hex.parse("01 02"), FrameCodec.parse(sumFraming, frame))
        val bad = frame.copyOf(); bad[4] = 0x00
        assertNull(FrameCodec.parse(sumFraming, bad))
        assertNull(FrameCodec.parse(sumFraming, Hex.parse("AA 05 01 02 AF")!!))   // wrong length
    }

    @Test fun missingOrOutOfRangeTokenBuildsNothing() {
        assertNull(FrameCodec.build(sumFraming, "01 {mode}", emptyMap(), emptyMap()))
        assertNull(FrameCodec.build(sumFraming, "01 {mode}", mapOf("mode" to 300), emptyMap()))
    }

    @Test fun crc16ModbusKnownVector() {
        val crc = FrameCodec.checksum("crc16modbus", "123456789".toByteArray())
        assertArrayEquals(byteArrayOf(0x37, 0x4B), crc)   // 0x4B37, low byte first
    }

    @Test fun xorChecksum() {
        assertArrayEquals(byteArrayOf(0x03), FrameCodec.checksum("xor8", byteArrayOf(1, 2)))
    }

    @Test fun batteryUnknownIsNotZero() {
        val bp = BatteryParse("05", 1, 2, 3, listOf(255))
        val s = ResponseParsers.battery(Hex.parse("05 50 FF 20")!!, bp, 1000L)!!
        assertEquals(80, (s.left as Reported.Available).value)
        assertTrue(s.right is Reported.Unavailable)       // 0xFF = invalid, must NOT become 0
        assertEquals(32, (s.case as Reported.Available).value)
    }

    @Test fun batteryRangeValidation() {
        assertTrue(validBattery(0)); assertTrue(validBattery(100))
        assertFalse(validBattery(101)); assertFalse(validBattery(-1)); assertFalse(validBattery(null))
    }

    @Test fun mergeKeepsKnownValueWhenNewIsUnavailable() {
        val old = BatteryState(Reported.Available(70, 1L), Reported.Unavailable("x"), Reported.Unavailable("x"), Reported.Unavailable("x"))
        val new = BatteryState.unavailable("none")
        assertEquals(70, (mergeBattery(old, new).left as Reported.Available).value)
    }

    @Test fun eqBandsAreIndependent() {
        val g = EqDefaults.flat()
        val out = EqDefaults.withBand(g, 4, 5f)
        for (i in out.indices) assertEquals(if (i == 4) 5f else 0f, out[i], 0f)
        assertEquals(0f, g[4], 0f)                                     // original untouched
        assertEquals(12f, EqDefaults.withBand(g, 0, 99f)[0], 0f)       // clamped
    }

    @Test fun presetsAreWellFormed() {
        assertEquals(26, EqDefaults.PRESETS.size)
        EqDefaults.PRESETS.forEach { assertEquals(10, it.gains.size); assertTrue(it.gains.all { g -> g in -12f..12f }) }
        assertEquals(EqDefaults.PRESETS.size, EqDefaults.PRESETS.map { it.id }.toSet().size)
    }

    @Test fun eqInterpolationHitsBandsAndClamps() {
        val g = listOf(0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f)
        assertEquals(5f, interpolateGainDb(g, 1000f), 0.001f)
        assertEquals(0f, interpolateGainDb(g, 10f), 0.001f)
        assertEquals(9f, interpolateGainDb(g, 20000f), 0.001f)
        assertTrue(interpolateGainDb(g, 700f) in 4f..5f)
    }

    @Test fun limiterNeverReachesFullScale() {
        val l = SoftLimiter(0.8f)
        assertEquals(0.5f, l.process(0.5f), 0f)
        for (x in listOf(1f, 3f, 100f, -1f, -100f)) assertTrue(Math.abs(l.process(x)) < 1f)
    }

    @Test fun hearingGainsAreClamped() {
        val c = HearingSettings(inputGain = 100f, outputGain = 5f).clamped()
        assertEquals(HearingSettings.INPUT_MAX, c.inputGain, 0f)
        assertEquals(HearingSettings.OUTPUT_MAX, c.outputGain, 0f)
        assertTrue(HearingSettings.SAFE_OUTPUT_START < HearingSettings.OUTPUT_MAX)
    }

    @Test fun profileRejectsUnknownCommandAndBadVerifiedEntry() {
        assertTrue(ProtocolProfile.fromJson("""{"schema":1,"commands":{"explode":{"request":"01","verified":false}}}""").isFailure)
        assertTrue(ProtocolProfile.fromJson("""{"schema":1,"commands":{"setMode":{"request":"","verified":true}}}""").isFailure)
        assertTrue(ProtocolProfile.fromJson("not json").isFailure)
        assertTrue(ProtocolProfile.fromJson("""{"schema":2}""").isFailure)
    }

    @Test fun profileAcceptsMinimalAndOnlyVerifiedCommandsCount() {
        val p = ProtocolProfile.fromJson(
            """{"schema":1,"name":"t","commands":{"setMode":{"request":"01 {mode}","verified":false},"readMode":{"request":"02","verified":true}}}"""
        ).getOrThrow()
        assertFalse(p.isVerified("setMode")); assertTrue(p.isVerified("readMode"))
        assertNotNull(p.commands["setMode"])
    }
}
```
