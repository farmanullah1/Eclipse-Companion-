package com.example.audio

import kotlin.math.abs
import kotlin.math.atan

class AudioProcessor(sampleRate: Int = 16000) {

    // First-order high-pass filter state for 120Hz cutoff
    private var hpState = 0.0f
    private val hpAlpha: Float = run {
        val dt = 1.0f / sampleRate
        val rc = 1.0f / (2.0f * Math.PI.toFloat() * 120.0f)
        rc / (rc + dt)
    }
    private var prevInput = 0.0f

    // Soft limiter threshold (normalized float: -1.0 to 1.0)
    private val limitThreshold = 0.75f

    fun processBuffer(
        input: ShortArray,
        output: ShortArray,
        length: Int,
        settings: HearingAssistSettings
    ) {
        val inGain = settings.inputGain.coerceIn(HearingAssistSettings.MIN_INPUT_GAIN, HearingAssistSettings.MAX_INPUT_GAIN)
        val outGain = settings.outputGain.coerceIn(HearingAssistSettings.MIN_OUTPUT_GAIN, HearingAssistSettings.MAX_OUTPUT_GAIN)
        val useRumble = settings.rumbleReduction != RumbleReduction.OFF
        val clarityBoost = when (settings.speechClarity) {
            SpeechClarity.OFF -> 1.0f
            SpeechClarity.MILD -> 1.35f
            SpeechClarity.MODERATE -> 1.75f
        }

        for (i in 0 until length) {
            // Normalize 16-bit PCM to [-1.0, 1.0]
            var sample = (input[i] / 32768.0f) * inGain

            // Apply high-pass rumble filter if enabled
            if (useRumble) {
                val filtered = hpAlpha * (hpState + sample - prevInput)
                prevInput = sample
                hpState = filtered
                sample = filtered
            }

            // Apply speech clarity boost
            sample *= clarityBoost

            // Apply output attenuation
            sample *= outGain

            // Soft Limiter (Hyperbolic / ArcTan curve prevents hard digital clipping)
            if (settings.limiterEnabled && abs(sample) > limitThreshold) {
                val sign = if (sample > 0) 1.0f else -1.0f
                val excess = abs(sample) - limitThreshold
                val compressed = (2.0f / Math.PI.toFloat()) * atan(excess * 1.5f) * (1.0f - limitThreshold)
                sample = sign * (limitThreshold + compressed)
            }

            // Safety hard clamp
            sample = sample.coerceIn(-0.98f, 0.98f)

            // Convert back to 16-bit PCM
            output[i] = (sample * 32767.0f).toInt().toShort()
        }
    }

    fun reset() {
        hpState = 0.0f
        prevInput = 0.0f
    }
}
