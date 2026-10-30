package com.example.bluetooth

import com.example.domain.ConnectionPhase

enum class Compatibility {
    VERIFIED_ECLIPSE,
    POSSIBLE_MATCH,
    UNSUPPORTED,
    UNKNOWN
}

data class NearbyDevice(
    val id: String,
    val displayName: String,
    val rssi: Int?,
    val compatibility: Compatibility,
    val advertisedServiceUuids: List<String> = emptyList(),
    val isSystemPaired: Boolean = false
)

data class ConnectionState(
    val phase: ConnectionPhase = ConnectionPhase.DISCONNECTED,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val isAudioConnected: Boolean = false,
    val message: String? = null,
    val lastConnectedTimeMillis: Long? = null
)

sealed interface DeviceCommandResult {
    data class Confirmed(val description: String) : DeviceCommandResult
    data class SentButUnconfirmed(val description: String) : DeviceCommandResult
    data class Rejected(val reason: String) : DeviceCommandResult
    data class Unsupported(val reason: String) : DeviceCommandResult
}

data class DiscoveredGattCharacteristic(
    val uuid: String,
    val name: String,
    val propertiesText: String,
    val canRead: Boolean,
    val canWrite: Boolean,
    val canNotify: Boolean,
    val lastValueHex: String? = null,
    val lastValueAscii: String? = null
)

data class DiscoveredGattService(
    val uuid: String,
    val name: String,
    val isVendor: Boolean,
    val characteristics: List<DiscoveredGattCharacteristic> = emptyList()
)
