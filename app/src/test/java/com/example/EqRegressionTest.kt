package com.example

import com.example.domain.EqBand
import com.example.domain.EqPresetCategory
import com.example.domain.EqProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class EqRegressionTest {

    @Test
    fun editing_one_band_leaves_all_other_bands_byte_for_byte_unchanged() {
        val initialGains = listOf(2.0f, -1.5f, 3.0f, 0.0f, 4.5f, -2.0f, 1.0f, -3.5f, 2.5f, -0.5f)
        val profile = EqProfile.createWithGains(
            id = "test_profile",
            name = "Test EQ",
            category = EqPresetCategory.CUSTOM,
            gains = initialGains,
            isEditable = true
        )

        // Modify band 1000 Hz by +3.0 dB
        val targetHz = 1000
        val targetOldGain = profile.bands.first { it.centerHz == targetHz }.gainDb
        val targetNewGain = targetOldGain + 3.0f

        val modified = profile.updateBand(targetHz, targetNewGain)

        // Verify target band was updated
        val modifiedTargetBand = modified.bands.first { it.centerHz == targetHz }
        assertEquals(targetNewGain, modifiedTargetBand.gainDb, 0.0001f)

        // Assert EVERY other band is completely unchanged
        profile.bands.forEach { originalBand ->
            if (originalBand.centerHz != targetHz) {
                val otherBand = modified.bands.first { it.centerHz == originalBand.centerHz }
                assertEquals(
                    "Band ${originalBand.centerHz} Hz was mutated unexpectedly!",
                    originalBand.gainDb,
                    otherBand.gainDb,
                    0.000001f
                )
            }
        }
    }

    @Test
    fun invalid_nan_and_infinite_gains_are_rejected() {
        val profile = EqProfile.createFlat("p1", "Flat", EqPresetCategory.CUSTOM)
        try {
            profile.updateBand(1000, Float.NaN)
            fail("Should have thrown IllegalArgumentException for NaN")
        } catch (_: IllegalArgumentException) {}

        try {
            profile.updateBand(1000, Float.POSITIVE_INFINITY)
            fail("Should have thrown IllegalArgumentException for Infinity")
        } catch (_: IllegalArgumentException) {}
    }

    @Test
    fun gains_are_clamped_within_safe_range() {
        val profile = EqProfile.createFlat("p1", "Flat", EqPresetCategory.CUSTOM)
        val boosted = profile.updateBand(1000, 25.0f)
        assertEquals(EqProfile.MAX_GAIN_DB, boosted.bands.first { it.centerHz == 1000 }.gainDb, 0.001f)

        val cut = profile.updateBand(1000, -30.0f)
        assertEquals(EqProfile.MIN_GAIN_DB, cut.bands.first { it.centerHz == 1000 }.gainDb, 0.001f)
    }
}
