package com.example

import com.example.bluetooth.protocol.VerifiedEclipseProtocol
import com.example.domain.ListeningMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AncProtocolTest {

    @Test
    fun jieliRcspAncCommand_buildsValidFramesForSupportedModes() {
        // ANC mode
        val ancFrame = VerifiedEclipseProtocol.buildJieLiRcspAncCommand(ListeningMode.ANC)
        assertEquals(0xFE.toByte(), ancFrame[0])
        assertEquals(0xDC.toByte(), ancFrame[1])
        assertEquals(0xBA.toByte(), ancFrame[2])
        assertEquals(0x07.toByte(), ancFrame[4]) // Device settings opcode
        assertEquals(0x01.toByte(), ancFrame[8]) // ANC Mode byte = 1

        // Normal (Off) mode
        val offFrame = VerifiedEclipseProtocol.buildJieLiRcspAncCommand(ListeningMode.NORMAL)
        assertEquals(0x00.toByte(), offFrame[8]) // Off Mode byte = 0

        // Transparency mode
        val ambientFrame = VerifiedEclipseProtocol.buildJieLiRcspAncCommand(ListeningMode.TRANSPARENCY)
        assertEquals(0x02.toByte(), ambientFrame[8]) // Transparency Mode byte = 2
    }

    @Test
    fun parseListeningModeResponse_decodesGenuineJieLiRcspResponse() {
        val testResponse = byteArrayOf(
            0xFE.toByte(), 0xDC.toByte(), 0xBA.toByte(),
            0x00, 0x07, 0x00, 0x02, 0x01, 0x01, 0x00
        )
        val mode = VerifiedEclipseProtocol.parseListeningModeResponse(testResponse)
        assertEquals(ListeningMode.ANC, mode)

        val offResponse = byteArrayOf(
            0xFE.toByte(), 0xDC.toByte(), 0xBA.toByte(),
            0x00, 0x07, 0x00, 0x02, 0x01, 0x00, 0x00
        )
        assertEquals(ListeningMode.NORMAL, VerifiedEclipseProtocol.parseListeningModeResponse(offResponse))

        val ambientResponse = byteArrayOf(
            0xFE.toByte(), 0xDC.toByte(), 0xBA.toByte(),
            0x00, 0x07, 0x00, 0x02, 0x01, 0x02, 0x00
        )
        assertEquals(ListeningMode.TRANSPARENCY, VerifiedEclipseProtocol.parseListeningModeResponse(ambientResponse))
    }

    @Test
    fun parseListeningModeResponse_decodesVendorFramedResponse() {
        val frame = VerifiedEclipseProtocol.buildVendorFramedAncCommand(ListeningMode.ANC)
        val decoded = VerifiedEclipseProtocol.parseListeningModeResponse(frame)
        assertEquals(ListeningMode.ANC, decoded)
    }

    @Test
    fun parseListeningModeResponse_rejectsCorruptedOrInvalidData() {
        assertNull(VerifiedEclipseProtocol.parseListeningModeResponse(null))
        assertNull(VerifiedEclipseProtocol.parseListeningModeResponse(byteArrayOf()))
        assertNull(VerifiedEclipseProtocol.parseListeningModeResponse(byteArrayOf(0x00, 0x01, 0x02)))
        assertNull(VerifiedEclipseProtocol.parseListeningModeResponse(byteArrayOf(0xFF.toByte(), 0xFF.toByte())))
    }

    @Test
    fun batteryLevelParser_rejectsInvalidOrOutOfRangeValues() {
        assertNull(VerifiedEclipseProtocol.parseBatteryLevel(null))
        assertNull(VerifiedEclipseProtocol.parseBatteryLevel(byteArrayOf()))
        assertNull(VerifiedEclipseProtocol.parseBatteryLevel(byteArrayOf(101.toByte())))
        assertEquals(100, VerifiedEclipseProtocol.parseBatteryLevel(byteArrayOf(100.toByte())))
        assertEquals(55, VerifiedEclipseProtocol.parseBatteryLevel(byteArrayOf(55.toByte())))
        assertEquals(0, VerifiedEclipseProtocol.parseBatteryLevel(byteArrayOf(0.toByte())))
    }
}
