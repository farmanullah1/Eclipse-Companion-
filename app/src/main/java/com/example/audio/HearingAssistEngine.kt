package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

class HearingAssistEngine(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val processor = AudioProcessor(SAMPLE_RATE)

    private val _phase = MutableStateFlow(HearingAssistPhase.STOPPED)
    val phase: StateFlow<HearingAssistPhase> = _phase.asStateFlow()

    private val _settings = MutableStateFlow(HearingAssistSettings())
    val settings: StateFlow<HearingAssistSettings> = _settings.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val isRunning = AtomicBoolean(false)
    private var workerThread: Thread? = null

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null

    fun updateSettings(newSettings: HearingAssistSettings) {
        _settings.value = newSettings
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    fun start(): Boolean {
        if (isRunning.get()) return true

        _phase.value = HearingAssistPhase.PREPARING_AUDIO
        _errorMessage.value = null

        // Request audio focus
        val focusResult = audioManager?.requestAudioFocus(
            null,
            AudioManager.STREAM_VOICE_CALL,
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
        )

        try {
            val minRecordBuf = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val minTrackBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            val bufferSize = maxOf(minRecordBuf, minTrackBuf, BUFFER_FRAMES * 2)

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize * 2
            )

            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val audioFormat = AudioFormat.Builder()
                .setSampleRate(SAMPLE_RATE)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .build()

            audioTrack = AudioTrack(
                audioAttributes,
                audioFormat,
                bufferSize * 2,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED ||
                audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
                releaseResources()
                _phase.value = HearingAssistPhase.ERROR
                _errorMessage.value = "Unable to initialize audio hardware for Hearing Assist"
                return false
            }

            audioRecord?.startRecording()
            audioTrack?.play()
            processor.reset()
            isRunning.set(true)
            _phase.value = HearingAssistPhase.RUNNING

            workerThread = Thread({
                val inputBuffer = ShortArray(BUFFER_FRAMES)
                val outputBuffer = ShortArray(BUFFER_FRAMES)

                while (isRunning.get()) {
                    val read = audioRecord?.read(inputBuffer, 0, BUFFER_FRAMES) ?: -1
                    if (read > 0) {
                        processor.processBuffer(inputBuffer, outputBuffer, read, _settings.value)
                        audioTrack?.write(outputBuffer, 0, read)
                    } else if (read < 0) {
                        // Error condition
                        break
                    }
                }
            }, "HearingAssist-Worker").apply {
                priority = Thread.MAX_PRIORITY
                start()
            }

            return true
        } catch (e: Exception) {
            releaseResources()
            _phase.value = HearingAssistPhase.ERROR
            _errorMessage.value = "Hearing assist failed to start: ${e.message}"
            return false
        }
    }

    @Synchronized
    fun stop() {
        if (!isRunning.getAndSet(false)) {
            _phase.value = HearingAssistPhase.STOPPED
            return
        }

        try {
            workerThread?.interrupt()
            workerThread?.join(500)
        } catch (_: Exception) {}
        workerThread = null

        releaseResources()
        audioManager?.abandonAudioFocus(null)
        _phase.value = HearingAssistPhase.STOPPED
    }

    private fun releaseResources() {
        try {
            audioRecord?.stop()
        } catch (_: Exception) {}
        try {
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null

        try {
            audioTrack?.stop()
        } catch (_: Exception) {}
        try {
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }

    companion object {
        const val SAMPLE_RATE = 16000
        const val BUFFER_FRAMES = 256
    }
}
