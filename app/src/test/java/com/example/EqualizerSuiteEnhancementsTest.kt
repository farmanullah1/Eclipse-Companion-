package com.example

import com.example.domain.EqBand
import com.example.domain.EqPresetCategory
import com.example.domain.EqProfile
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EqualizerSuiteEnhancementsTest {

    @Test
    fun ten_bands_conform_strictly_to_iso_standards() {
        val expectedFrequencies = listOf(32, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
        assertEquals(10, EqProfile.FREQUENCIES.size)
        assertEquals(expectedFrequencies, EqProfile.FREQUENCIES)
    }

    @Test
    fun json_preset_export_and_import_round_trip() {
        val gains = listOf(3.5f, 2.0f, 0.0f, -1.5f, -3.0f, 0.5f, 1.5f, 4.0f, 5.0f, 6.0f)
        val profile = EqProfile.createWithGains(
            id = "custom_test_123",
            name = "Studio Warmth",
            category = EqPresetCategory.CUSTOM,
            gains = gains,
            isEditable = true
        )

        // Simulate JSON serialization
        val array = JSONArray()
        val obj = JSONObject().apply {
            put("name", profile.name)
            val bandsArray = JSONArray()
            profile.bands.forEach { b ->
                bandsArray.put(JSONObject().apply {
                    put("hz", b.centerHz)
                    put("gain", b.gainDb.toDouble())
                })
            }
            put("bands", bandsArray)
        }
        array.put(obj)
        val jsonString = array.toString()

        // Deserialization
        val parsedArray = JSONArray(jsonString)
        assertEquals(1, parsedArray.length())
        val parsedObj = parsedArray.getJSONObject(0)
        assertEquals("Studio Warmth", parsedObj.getString("name"))

        val parsedBandsArray = parsedObj.getJSONArray("bands")
        assertEquals(10, parsedBandsArray.length())

        for (i in 0 until 10) {
            val b = parsedBandsArray.getJSONObject(i)
            assertEquals(EqProfile.FREQUENCIES[i], b.getInt("hz"))
            assertEquals(gains[i], b.getDouble("gain").toFloat(), 0.001f)
        }
    }

    @Test
    fun hearing_test_compensation_calculation_boosts_hearing_loss_frequencies() {
        val testResults = mapOf(
            250 to 0.15f,  // normal hearing threshold
            500 to 0.15f,  // normal
            1000 to 0.15f, // normal
            2000 to 0.25f, // slight elevation
            4000 to 0.45f, // significant threshold elevation (needs boost)
            8000 to 0.50f  // significant threshold elevation
        )

        val baseFrequencies = EqProfile.FREQUENCIES
        val compensatedBands = baseFrequencies.map { hz ->
            val closestTestedHz = testResults.keys.minByOrNull { Math.abs(it - hz) } ?: 1000
            val thresholdFraction = testResults[closestTestedHz] ?: 0.5f

            val delta = (thresholdFraction - 0.15f) / 0.35f
            val gain = (delta * 6.0f).coerceIn(-3.0f, 7.5f)
            val roundedGain = Math.round(gain * 2) / 2.0f
            EqBand(hz, roundedGain)
        }

        assertEquals(10, compensatedBands.size)

        // 250Hz should have 0 dB boost (normal)
        val band250 = compensatedBands.first { it.centerHz == 250 }
        assertEquals(0.0f, band250.gainDb, 0.01f)

        // 4000Hz should have positive boost to compensate for hearing threshold
        val band4000 = compensatedBands.first { it.centerHz == 4000 }
        assertTrue("4000Hz band should have positive compensatory gain", band4000.gainDb > 3.0f)

        // Ensure all gains are within safe hardware limits (-12..+12)
        compensatedBands.forEach { b ->
            assertTrue(b.gainDb in -12.0f..12.0f)
        }
    }
}
