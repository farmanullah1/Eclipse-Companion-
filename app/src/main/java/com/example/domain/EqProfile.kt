package com.example.domain

data class EqBand(
    val centerHz: Int,
    val gainDb: Float
) {
    val displayFrequency: String
        get() = if (centerHz >= 1000) "${centerHz / 1000}k" else "$centerHz"
}

enum class EqPresetCategory(val title: String) {
    MOOD("Mood"),
    ARTISTS("Artists"),
    CUSTOM("Custom")
}

data class EqProfile(
    val id: String,
    val name: String,
    val category: EqPresetCategory,
    val bands: List<EqBand>,
    val createdAtEpochMillis: Long = System.currentTimeMillis(),
    val isUserEditable: Boolean = false
) {
    fun updateBand(centerHz: Int, newGainDb: Float): EqProfile {
        require(newGainDb.isFinite()) { "Gain must be a finite number" }
        val clampedGain = newGainDb.coerceIn(MIN_GAIN_DB, MAX_GAIN_DB)
        val updatedBands = bands.map { band ->
            if (band.centerHz == centerHz) band.copy(gainDb = clampedGain) else band
        }
        return copy(bands = updatedBands)
    }

    companion object {
        const val MIN_GAIN_DB = -12.0f
        const val MAX_GAIN_DB = 12.0f

        val FREQUENCIES = listOf(32, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)

        fun createFlat(id: String, name: String, category: EqPresetCategory, isEditable: Boolean = true): EqProfile {
            return EqProfile(
                id = id,
                name = name,
                category = category,
                bands = FREQUENCIES.map { EqBand(it, 0.0f) },
                isUserEditable = isEditable
            )
        }

        fun createWithGains(
            id: String,
            name: String,
            category: EqPresetCategory,
            gains: List<Float>,
            isEditable: Boolean = false
        ): EqProfile {
            require(gains.size == FREQUENCIES.size) { "Expected ${FREQUENCIES.size} gains, got ${gains.size}" }
            return EqProfile(
                id = id,
                name = name,
                category = category,
                bands = FREQUENCIES.zip(gains) { hz, gain -> EqBand(hz, gain.coerceIn(MIN_GAIN_DB, MAX_GAIN_DB)) },
                isUserEditable = isEditable
            )
        }

        val OFFICIAL_MOOD_PRESETS = listOf(
            createWithGains("mood_default", "Default", EqPresetCategory.MOOD, listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)),
            createWithGains("mood_energetic", "Energetic", EqPresetCategory.MOOD, listOf(4f, 3.5f, 2f, 0f, -0.5f, 1f, 2f, 3.5f, 4f, 4.5f)),
            createWithGains("mood_calm", "Calm", EqPresetCategory.MOOD, listOf(-1f, 0f, 1.5f, 2f, 1.5f, 0.5f, -0.5f, -1.5f, -2.5f, -3.5f)),
            createWithGains("mood_chill", "Chill", EqPresetCategory.MOOD, listOf(2f, 3f, 1.5f, 0f, -1f, 0f, 1.5f, 2f, 1f, 0f)),
            createWithGains("mood_focus", "Focus", EqPresetCategory.MOOD, listOf(-3f, -2f, 0f, 2.5f, 4f, 3f, 2f, 1f, 0f, -1f)),
            createWithGains("mood_sad", "Sad", EqPresetCategory.MOOD, listOf(3.5f, 2.5f, 1f, 0f, -1f, -1.5f, -1f, 0f, 1f, 1.5f))
        )

        val OFFICIAL_ARTIST_PRESETS = listOf(
            createWithGains("artist_hasan_raheem", "Hasan Raheem", EqPresetCategory.ARTISTS, listOf(3f, 2f, 0.5f, 0f, 1.5f, 3f, 2.5f, 2f, 2.5f, 3f)),
            createWithGains("artist_annural_khalid", "Annural Khalid", EqPresetCategory.ARTISTS, listOf(1f, 1.5f, 0f, 1.5f, 3.5f, 4f, 3.5f, 3f, 3.5f, 4f)),
            createWithGains("artist_asim_azhar", "Asim Azhar", EqPresetCategory.ARTISTS, listOf(4f, 3f, 1f, -0.5f, 2f, 3.5f, 3f, 2.5f, 3.5f, 4f)),
            createWithGains("artist_abdul_hannan", "Abdul Hannan", EqPresetCategory.ARTISTS, listOf(2f, 2.5f, 1.5f, 1f, 2f, 3f, 2.5f, 1.5f, 2f, 2f))
        )

        val DEFAULT_CUSTOM_PRESETS = listOf(
            createFlat("custom_1", "Custom 1", EqPresetCategory.CUSTOM, isEditable = true),
            createFlat("custom_2", "Custom 2", EqPresetCategory.CUSTOM, isEditable = true)
        )

        val DEFAULT_PROFILE = OFFICIAL_MOOD_PRESETS.first()
    }
}
