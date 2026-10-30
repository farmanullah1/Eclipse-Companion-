package com.example.audio

enum class HearingAssistPhase {
    STOPPED,
    REQUESTING_PERMISSION,
    PREPARING_AUDIO,
    RUNNING,
    PAUSED,
    ERROR
}

enum class SpeechClarity(val displayName: String) {
    OFF("Off"),
    MILD("Mild"),
    MODERATE("Moderate")
}

enum class RumbleReduction(val displayName: String) {
    OFF("Off"),
    MILD("Mild (High-pass 120Hz)")
}

data class HearingAssistSettings(
    val inputGain: Float = 1.0f,
    val outputGain: Float = 0.15f, // Conservative safe starting level
    val speechClarity: SpeechClarity = SpeechClarity.OFF,
    val rumbleReduction: RumbleReduction = RumbleReduction.MILD,
    val limiterEnabled: Boolean = true
) {
    companion object {
        const val MIN_INPUT_GAIN = 0.5f
        const val MAX_INPUT_GAIN = 2.0f
        const val MIN_OUTPUT_GAIN = 0.05f
        const val MAX_OUTPUT_GAIN = 0.65f // Hard safety cap
    }
}
