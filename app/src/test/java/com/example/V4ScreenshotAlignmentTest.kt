package com.example

import com.example.audio.ToolMode
import com.example.domain.EqProfile
import com.example.domain.ListeningMode
import com.example.domain.TouchAction
import com.example.domain.TouchGesture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V4ScreenshotAlignmentTest {

    @Test
    fun bandFrequenciesMatchOfficialLayout() {
        assertEquals(
            listOf(32, 63, 125, 250, 500, 1000, 2000, 4000, 8000, 16000),
            EqProfile.FREQUENCIES
        )
    }

    @Test
    fun moodPresetsAreWellFormedAndUnique() {
        val moods = EqProfile.OFFICIAL_MOOD_PRESETS
        assertEquals(6, moods.size)
        moods.forEach { profile ->
            assertEquals(10, profile.bands.size)
            assertTrue(profile.bands.all { it.gainDb in -12f..12f })
        }
        val ids = moods.map { it.id }.toSet()
        assertEquals(moods.size, ids.size)
    }

    @Test
    fun gestureLabelsMatchOfficialNames() {
        assertEquals("One Tap", TouchGesture.ONE_TAP.displayName)
        assertEquals("Two Tap", TouchGesture.TWO_TAP.displayName)
        assertEquals("Three Tap", TouchGesture.THREE_TAP.displayName)
        assertEquals("Tap & Hold", TouchGesture.TAP_AND_HOLD.displayName)
    }

    @Test
    fun touchActionNamesMatchOfficialNames() {
        assertEquals("No Action", TouchAction.NO_ACTION.displayName)
        assertEquals("ANC Switching", TouchAction.ANC_SWITCHING.displayName)
    }

    @Test
    fun listeningModesMatchOfficialNames() {
        assertEquals("Noise Reduction", ListeningMode.ANC.displayName)
        assertEquals("Off", ListeningMode.NORMAL.displayName)
        assertEquals("Transparency", ListeningMode.TRANSPARENCY.displayName)
    }

    @Test
    fun acousticMaintenanceModesAreSafeAndCapped() {
        assertEquals(60, ToolMode.CLEAN_DUST.durationSeconds)
        assertEquals(30, ToolMode.DRAIN_MOISTURE.durationSeconds)
        assertTrue(ToolMode.CLEAN_DUST.amplitude in 0.01f..0.5f)
        assertTrue(ToolMode.DRAIN_MOISTURE.amplitude in 0.01f..0.5f)
    }
}
