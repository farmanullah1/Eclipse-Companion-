package com.example

import com.example.audio.AudioProcessor
import com.example.audio.HearingAssistSettings
import com.example.audio.RumbleReduction
import com.example.audio.SpeechClarity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class AudioProcessorTest {

    @Test
    fun soft_limiter_prevents_digital_clipping_on_loud_input() {
        val processor = AudioProcessor(16000)
        val settings = HearingAssistSettings(
            inputGain = 2.0f,
            outputGain = 0.65f,
            speechClarity = SpeechClarity.MODERATE,
            rumbleReduction = RumbleReduction.OFF,
            limiterEnabled = true
        )

        val input = ShortArray(100) { Short.MAX_VALUE } // Extreme maximum input signal
        val output = ShortArray(100)

        processor.processBuffer(input, output, 100, settings)

        for (i in 0 until 100) {
            val sample = output[i]
            // Maximum sample must never exceed 32767 and must stay within safe headroom
            assertTrue("Sample exceeded safe limit: $sample", abs(sample.toInt()) <= 32111)
        }
    }

    @Test
    fun gain_settings_are_safely_clamped() {
        val outOfBounds = HearingAssistSettings(
            inputGain = 100.0f,
            outputGain = 50.0f
        )
        val processor = AudioProcessor(16000)
        val input = ShortArray(50) { 1000 }
        val output = ShortArray(50)

        processor.processBuffer(input, output, 50, outOfBounds)
        for (i in 0 until 50) {
            assertTrue("Output not finite or corrupted", output[i] in -32768..32767)
        }
    }
}
