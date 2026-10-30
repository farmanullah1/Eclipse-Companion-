package com.example.domain

enum class ListeningMode(val displayName: String) {
    ANC("Noise Reduction"),
    NORMAL("Off"),
    TRANSPARENCY("Transparency");

    companion object {
        fun fromId(id: String?): ListeningMode {
            return when (id?.uppercase()) {
                "ANC", "NOISE_REDUCTION" -> ANC
                "TRANSPARENCY", "AMBIENT" -> TRANSPARENCY
                else -> NORMAL
            }
        }
    }
}

enum class ConnectionPhase {
    DISCONNECTED,
    SCANNING,
    CONNECTING,
    DISCOVERING_SERVICES,
    SUBSCRIBING,
    READY,
    RECONNECTING,
    ERROR
}

data class ProtocolCapabilities(
    val batteryLeft: Boolean = false,
    val batteryRight: Boolean = false,
    val batteryCase: Boolean = false,
    val listeningModes: Set<ListeningMode> = emptySet(),
    val transparencyLevel: Boolean = false,
    val hardwareEq: Boolean = false,
    val touchMapping: Boolean = false,
    val gamingMode: Boolean = false,
    val deviceRename: Boolean = false,
    val meditationControl: Boolean = false,
    val firmwareRead: Boolean = false,
    val firmwareUpdate: Boolean = false,
    val factoryReset: Boolean = false
) {
    companion object {
        val DEFAULT_UNVERIFIED = ProtocolCapabilities()
        val OFFICIAL_ECLIPSE_DETECTED = ProtocolCapabilities(
            batteryLeft = true,
            batteryRight = true,
            batteryCase = false, // Case battery only reported conditionally when in-case/lid open
            listeningModes = setOf(ListeningMode.ANC, ListeningMode.NORMAL, ListeningMode.TRANSPARENCY),
            transparencyLevel = false, // Step intensity not exposed in standard firmware
            hardwareEq = true,
            touchMapping = true,
            gamingMode = true,
            deviceRename = true,
            meditationControl = false, // Local phone-side audio synthesis
            firmwareRead = true,
            firmwareUpdate = false, // OTA updates locked without vendor signing key
            factoryReset = false
        )
    }
}
