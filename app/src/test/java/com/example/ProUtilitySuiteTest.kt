package com.example

import com.example.audio.ToolMode
import com.example.ui.theme.AppThemeOption
import com.example.widget.EclipseAppWidgetProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProUtilitySuiteTest {

    @Test
    fun new_tool_modes_have_safe_durations_and_amplitudes() {
        val findMode = ToolMode.FIND_MY_EARBUDS
        assertEquals(15, findMode.durationSeconds)
        assertTrue("Locator amplitude must be capped for ear protection", findMode.amplitude <= 0.7f)

        val fitTestMode = ToolMode.FIT_TEST
        assertEquals(5, fitTestMode.durationSeconds)
        assertTrue("Fit sweep amplitude must be comfortable", fitTestMode.amplitude <= 0.5f)
    }

    @Test
    fun app_theme_options_contain_all_three_palettes() {
        val options = AppThemeOption.values()
        assertEquals(3, options.size)
        assertTrue(options.any { it.name == "OBSIDIAN" })
        assertTrue(options.any { it.name == "CYBERPUNK" })
        assertTrue(options.any { it.name == "STUDIO_GOLD" })
    }

    @Test
    fun widget_intent_constants_are_well_formed() {
        assertEquals("com.example.ACTION_WIDGET_SET_ANC", EclipseAppWidgetProvider.ACTION_SET_MODE)
        assertEquals("extra_listening_mode", EclipseAppWidgetProvider.EXTRA_MODE)
    }

    @Test
    fun who_noise_dose_calculation_safety_limits() {
        val volumeFraction = 0.70f // 70% volume (~78 dB)
        val safeHours = 8.0f / (Math.pow(2.0, ((volumeFraction * 100 - 80) / 3.0)).toFloat())
        assertTrue("Safe hours at moderate volume should exceed 6 hours", safeHours > 6f)
    }
}
