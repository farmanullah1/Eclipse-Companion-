package com.example.bluetooth.protocol

import com.example.domain.ListeningMode
import java.util.UUID

object VerifiedEclipseProtocol {
    // Standard Bluetooth SIG UUIDs
    val UUID_BATTERY_SERVICE: UUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB")
    val UUID_BATTERY_LEVEL: UUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB")

    val UUID_DEVICE_INFO_SERVICE: UUID = UUID.fromString("0000180A-0000-1000-8000-00805F9B34FB")
    val UUID_MODEL_NUMBER: UUID = UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB")
    val UUID_FIRMWARE_REVISION: UUID = UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB")
    val UUID_MANUFACTURER_NAME: UUID = UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB")

    val UUID_CCCD: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")

    // JieLi (JL) RCSP TWS Companion Protocol UUIDs (Standard for Yscoco / RONiN STUDIO TWS)
    val UUID_JIELI_RCSP_SERVICE: UUID = UUID.fromString("0000AE00-0000-1000-8000-00805F9B34FB")
    val UUID_JIELI_RCSP_WRITE: UUID = UUID.fromString("0000AE01-0000-1000-8000-00805F9B34FB")
    val UUID_JIELI_RCSP_NOTIFY: UUID = UUID.fromString("0000AE02-0000-1000-8000-00805F9B34FB")

    // JieLi Alt Custom Service / Smart Case UUIDs
    val UUID_JIELI_ALT_SERVICE: UUID = UUID.fromString("0000AE30-0000-1000-8000-00805F9B34FB")
    val UUID_JIELI_ALT_WRITE: UUID = UUID.fromString("0000AE98-0000-1000-8000-00805F9B34FB")
    val UUID_JIELI_ALT_NOTIFY: UUID = UUID.fromString("0000AE99-0000-1000-8000-00805F9B34FB")

    // Common Vendor TWS UUIDs
    val UUID_VENDOR_FEE0: UUID = UUID.fromString("0000FEE0-0000-1000-8000-00805F9B34FB")
    val UUID_VENDOR_FEE1_WRITE: UUID = UUID.fromString("0000FEE1-0000-1000-8000-00805F9B34FB")
    val UUID_VENDOR_FEE2_NOTIFY: UUID = UUID.fromString("0000FEE2-0000-1000-8000-00805F9B34FB")

    val UUID_VENDOR_FFF0: UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB")
    val UUID_VENDOR_FFF1_WRITE: UUID = UUID.fromString("0000FFF1-0000-1000-8000-00805F9B34FB")
    val UUID_VENDOR_FFF2_WRITE: UUID = UUID.fromString("0000FFF2-0000-1000-8000-00805F9B34FB")
    val UUID_VENDOR_FFF4_NOTIFY: UUID = UUID.fromString("0000FFF4-0000-1000-8000-00805F9B34FB")

    val KNOWN_SERVICE_NAMES: Map<UUID, String> = mapOf(
        UUID_BATTERY_SERVICE to "Battery Service (SIG)",
        UUID_DEVICE_INFO_SERVICE to "Device Information Service (SIG)",
        UUID_JIELI_RCSP_SERVICE to "JieLi RCSP Control Service (Yscoco/Ronin)",
        UUID_JIELI_ALT_SERVICE to "JieLi Alt Control / Smart Case Service",
        UUID_VENDOR_FEE0 to "Vendor Audio Control Service (FEE0)",
        UUID_VENDOR_FFF0 to "Vendor Serial Channel (FFF0)"
    )

    val KNOWN_CHARACTERISTIC_NAMES: Map<UUID, String> = mapOf(
        UUID_BATTERY_LEVEL to "Battery Level (0x2A19)",
        UUID_MODEL_NUMBER to "Model Number String (0x2A24)",
        UUID_FIRMWARE_REVISION to "Firmware Revision (0x2A26)",
        UUID_MANUFACTURER_NAME to "Manufacturer Name (0x2A29)",
        UUID_JIELI_RCSP_WRITE to "RCSP Command Write (AE01)",
        UUID_JIELI_RCSP_NOTIFY to "RCSP Telemetry Notify (AE02)",
        UUID_JIELI_ALT_WRITE to "RCSP Alt Write (AE98)",
        UUID_JIELI_ALT_NOTIFY to "RCSP Alt Notify (AE99)",
        UUID_VENDOR_FEE1_WRITE to "Vendor Write (FEE1)",
        UUID_VENDOR_FEE2_NOTIFY to "Vendor Notify (FEE2)",
        UUID_VENDOR_FFF1_WRITE to "Vendor Write (FFF1)",
        UUID_VENDOR_FFF2_WRITE to "Vendor Write (FFF2)",
        UUID_VENDOR_FFF4_NOTIFY to "Vendor Notify (FFF4)"
    )

    fun parseBatteryLevel(data: ByteArray?): Int? {
        if (data == null || data.isEmpty()) return null
        val level = data[0].toInt() and 0xFF
        return if (level in 0..100) level else null
    }

    /**
     * Builds standard JieLi RCSP Command Frame for Listening Mode (ANC).
     * Preamble: 0xFE, 0xDC, 0xBA
     * Opcode: 0x07 (Device Settings)
     * Sub-opcode: 0x01 (ANC Mode)
     * Parameters: 0x00 = Off, 0x01 = ANC, 0x02 = Transparency
     */
    fun buildJieLiRcspAncCommand(mode: ListeningMode): ByteArray {
        val modeByte: Byte = when (mode) {
            ListeningMode.NORMAL -> 0x00
            ListeningMode.ANC -> 0x01
            ListeningMode.TRANSPARENCY -> 0x02
        }
        val payload = byteArrayOf(0x01, modeByte)
        val len = payload.size
        val header = byteArrayOf(
            0xFE.toByte(), 0xDC.toByte(), 0xBA.toByte(),
            0x00, // flags
            0x07, // opcode (device settings)
            ((len shr 8) and 0xFF).toByte(),
            (len and 0xFF).toByte()
        )
        val checksum = (header.fold(0) { acc, b -> acc xor (b.toInt() and 0xFF) } xor
                payload.fold(0) { acc, b -> acc xor (b.toInt() and 0xFF) }).toByte()
        return header + payload + byteArrayOf(checksum)
    }

    /**
     * Builds Yscoco vendor framed mode command.
     * Frame: [0xAA, 0x03, 0x01, modeByte, checksum]
     */
    fun buildVendorFramedAncCommand(mode: ListeningMode): ByteArray {
        val modeByte: Byte = when (mode) {
            ListeningMode.NORMAL -> 0x00
            ListeningMode.ANC -> 0x01
            ListeningMode.TRANSPARENCY -> 0x02
        }
        val checksum = (0xAA xor 0x03 xor 0x01 xor (modeByte.toInt() and 0xFF)).toByte()
        return byteArrayOf(0xAA.toByte(), 0x03, 0x01, modeByte, checksum)
    }

    /**
     * Parse listening mode from device telemetry notification.
     */
    fun parseListeningModeResponse(data: ByteArray?): ListeningMode? {
        if (data == null || data.isEmpty()) return null
        // Case 1: JieLi RCSP response with header 0xFE, 0xDC, 0xBA
        if (data.size >= 8 && data[0] == 0xFE.toByte() && data[1] == 0xDC.toByte() && data[2] == 0xBA.toByte()) {
            val opcode = data[4].toInt() and 0xFF
            if (opcode == 0x07 && data.size >= 9) {
                return when (data[8].toInt() and 0xFF) {
                    0x01 -> ListeningMode.ANC
                    0x02 -> ListeningMode.TRANSPARENCY
                    0x00 -> ListeningMode.NORMAL
                    else -> null
                }
            }
        }
        // Case 2: Vendor framed response [0xAA, 0x03, 0x01, modeByte, ...]
        if (data.size >= 4 && data[0] == 0xAA.toByte() && data[1] == 0x03.toByte()) {
            return when (data[3].toInt() and 0xFF) {
                0x01 -> ListeningMode.ANC
                0x02 -> ListeningMode.TRANSPARENCY
                0x00 -> ListeningMode.NORMAL
                else -> null
            }
        }
        // Case 3: Single byte state report
        if (data.size == 1) {
            return when (data[0].toInt() and 0xFF) {
                0x01 -> ListeningMode.ANC
                0x02 -> ListeningMode.TRANSPARENCY
                0x00 -> ListeningMode.NORMAL
                else -> null
            }
        }
        return null
    }

    fun ByteArray.toHexString(): String {
        return joinToString(" ") { String.format("%02X", it) }
    }
}
