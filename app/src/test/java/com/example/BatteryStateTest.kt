package com.example

import com.example.domain.BatteryState
import com.example.domain.ReportedValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BatteryStateTest {

    @Test
    fun unknown_battery_is_unavailable_and_never_defaults_to_zero() {
        val state = BatteryState.unavailable("Not connected")
        assertFalse(state.left.isAvailable)
        assertFalse(state.right.isAvailable)
        assertFalse(state.case.isAvailable)

        assertNull(state.left.valueOrNull())
        assertNull(state.right.valueOrNull())
        assertNull(state.case.valueOrNull())
    }

    @Test
    fun out_of_range_values_are_sanitized_to_unavailable() {
        val now = 1000L
        val invalidLow = BatteryState.createSafe(
            leftPercent = -5,
            rightPercent = 105,
            casePercent = null,
            timestamp = now
        )

        assertFalse(invalidLow.left.isAvailable)
        assertFalse(invalidLow.right.isAvailable)
        assertFalse(invalidLow.case.isAvailable)
    }

    @Test
    fun valid_percentages_are_reported_independently() {
        val now = 1700000000000L
        val validState = BatteryState.createSafe(
            leftPercent = 85,
            rightPercent = 90,
            casePercent = 65,
            timestamp = now
        )

        assertTrue(validState.left.isAvailable)
        assertEquals(85, validState.left.valueOrNull())
        assertEquals(90, validState.right.valueOrNull())
        assertEquals(65, validState.case.valueOrNull())

        val leftAvailable = validState.left as ReportedValue.Available
        assertEquals(now, leftAvailable.reportedAtEpochMillis)
    }
}
