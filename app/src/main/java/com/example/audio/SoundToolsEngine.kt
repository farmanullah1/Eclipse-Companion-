package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.sin

enum class ToolMode {
    IDLE,
    CLEAN_DUST,
    DRAIN_MOISTURE,
    RELAXATION
}

enum class RelaxationSound(val displayName: String) {
    RAIN("Gentle Rain"),
    OCEAN("Ocean Waves"),
    ZEN_BELL("Zen Bell Meditation"),
    WHITE_NOISE("Soft White Noise")
}

class SoundToolsEngine(private val coroutineScope: CoroutineScope) {

    private val _currentMode = MutableStateFlow(ToolMode.IDLE)
    val currentMode: StateFlow<ToolMode> = _currentMode.asStateFlow()

    private val _progressSeconds = MutableStateFlow(0)
    val progressSeconds: StateFlow<Int> = _progressSeconds.asStateFlow()

    private val _totalDurationSeconds = MutableStateFlow(15)
    val totalDurationSeconds: StateFlow<Int> = _totalDurationSeconds.asStateFlow()

    private val _activeRelaxationSound = MutableStateFlow(RelaxationSound.RAIN)
    val activeRelaxationSound: StateFlow<RelaxationSound> = _activeRelaxationSound.asStateFlow()

    private val isPlaying = AtomicBoolean(false)
    private var playbackJob: Job? = null
    private var audioTrack: AudioTrack? = null

    fun selectRelaxationSound(sound: RelaxationSound) {
        _activeRelaxationSound.value = sound
        if (_currentMode.value == ToolMode.RELAXATION && isPlaying.get()) {
            stop()
            startRelaxation(sound, _totalDurationSeconds.value)
        }
    }

    fun startCleanDust(durationSeconds: Int = 15) {
        stop()
        _currentMode.value = ToolMode.CLEAN_DUST
        _totalDurationSeconds.value = durationSeconds
        _progressSeconds.value = 0
        isPlaying.set(true)

        playbackJob = coroutineScope.launch(Dispatchers.Default) {
            val track = createTrack()
            audioTrack = track
            track.play()

            val sampleRate = 44100
            val buffer = ShortArray(1024)
            var phase = 0.0
            val startTime = System.currentTimeMillis()

            while (isActive && isPlaying.get()) {
                val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                _progressSeconds.value = elapsedSec
                if (elapsedSec >= durationSeconds) break

                // High energy diaphragm sweep between 350Hz and 650Hz
                val progress = (elapsedSec.toDouble() / durationSeconds).coerceIn(0.0, 1.0)
                val freq = 350.0 + progress * 300.0

                for (i in buffer.indices) {
                    val sample = sin(phase) * 0.85
                    buffer[i] = (sample * 32767.0).toInt().toShort()
                    phase += 2.0 * PI * freq / sampleRate
                    if (phase > 2.0 * PI) phase -= 2.0 * PI
                }
                track.write(buffer, 0, buffer.size)
            }
            stop()
        }
    }

    fun startDrainMoisture(durationSeconds: Int = 15) {
        stop()
        _currentMode.value = ToolMode.DRAIN_MOISTURE
        _totalDurationSeconds.value = durationSeconds
        _progressSeconds.value = 0
        isPlaying.set(true)

        playbackJob = coroutineScope.launch(Dispatchers.Default) {
            val track = createTrack()
            audioTrack = track
            track.play()

            val sampleRate = 44100
            val buffer = ShortArray(1024)
            var phase = 0.0
            val startTime = System.currentTimeMillis()

            while (isActive && isPlaying.get()) {
                val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                _progressSeconds.value = elapsedSec
                if (elapsedSec >= durationSeconds) break

                // Low-frequency hydraulic displacement pulse: 145Hz pulses with rhythmic amplitude modulation
                val cycleSec = (System.currentTimeMillis() % 1000) / 1000.0
                val amp = if (cycleSec < 0.6) 0.9 else 0.05
                val freq = 145.0

                for (i in buffer.indices) {
                    val sample = sin(phase) * amp
                    buffer[i] = (sample * 32767.0).toInt().toShort()
                    phase += 2.0 * PI * freq / sampleRate
                    if (phase > 2.0 * PI) phase -= 2.0 * PI
                }
                track.write(buffer, 0, buffer.size)
            }
            stop()
        }
    }

    fun startRelaxation(sound: RelaxationSound, durationMinutes: Int = 10) {
        stop()
        _currentMode.value = ToolMode.RELAXATION
        _activeRelaxationSound.value = sound
        val totalSec = durationMinutes * 60
        _totalDurationSeconds.value = totalSec
        _progressSeconds.value = 0
        isPlaying.set(true)

        playbackJob = coroutineScope.launch(Dispatchers.Default) {
            val track = createTrack()
            audioTrack = track
            track.play()

            val sampleRate = 44100
            val buffer = ShortArray(1024)
            val random = Random()
            var filterState = 0.0
            var wavePhase = 0.0
            val startTime = System.currentTimeMillis()

            while (isActive && isPlaying.get()) {
                val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).toInt()
                _progressSeconds.value = elapsedSec
                if (elapsedSec >= totalSec) break

                when (sound) {
                    RelaxationSound.RAIN -> {
                        // Pink noise / rain drop acoustic synthesis
                        for (i in buffer.indices) {
                            val white = (random.nextGaussian() * 0.18).coerceIn(-1.0, 1.0)
                            filterState = 0.94 * filterState + 0.06 * white
                            buffer[i] = (filterState * 32767.0).toInt().toShort()
                        }
                    }
                    RelaxationSound.OCEAN -> {
                        // Periodic swell using slow sine modulation
                        val swell = (sin(wavePhase) + 1.0) * 0.5 * 0.28
                        wavePhase += 2.0 * PI * 0.12 / sampleRate
                        if (wavePhase > 2.0 * PI) wavePhase -= 2.0 * PI

                        for (i in buffer.indices) {
                            val white = (random.nextGaussian() * swell).coerceIn(-1.0, 1.0)
                            filterState = 0.9 * filterState + 0.1 * white
                            buffer[i] = (filterState * 32767.0).toInt().toShort()
                        }
                    }
                    RelaxationSound.ZEN_BELL -> {
                        // Gentle ambient Tibetan harmonic sine bell with decay
                        val t = (elapsedSec % 10) + ((System.currentTimeMillis() % 1000) / 1000.0)
                        val decay = Math.exp(-t * 0.45) * 0.35
                        for (i in buffer.indices) {
                            val sample = (sin(wavePhase * 432.0) + 0.5 * sin(wavePhase * 864.0)) * decay
                            wavePhase += 2.0 * PI / sampleRate
                            if (wavePhase > 2.0 * PI) wavePhase -= 2.0 * PI
                            buffer[i] = (sample * 32767.0).toInt().toShort()
                        }
                    }
                    RelaxationSound.WHITE_NOISE -> {
                        for (i in buffer.indices) {
                            val sample = (random.nextGaussian() * 0.12).coerceIn(-0.9, 0.9)
                            buffer[i] = (sample * 32767.0).toInt().toShort()
                        }
                    }
                }
                track.write(buffer, 0, buffer.size)
            }
            stop()
        }
    }

    fun stop() {
        if (!isPlaying.getAndSet(false)) return
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.pause()
            audioTrack?.flush()
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
        _currentMode.value = ToolMode.IDLE
        _progressSeconds.value = 0
    }

    private fun createTrack(): AudioTrack {
        val sampleRate = 44100
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        val audioFormat = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .build()

        return AudioTrack(
            audioAttributes,
            audioFormat,
            minBuf * 2,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
    }
}
