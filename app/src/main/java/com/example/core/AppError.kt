package com.example.core

sealed class AppError(open val userMessage: String, open val technicalDetail: String? = null) {
    data class BluetoothUnavailable(override val technicalDetail: String? = null) :
        AppError("Bluetooth hardware is not available on this device", technicalDetail)

    data class BluetoothDisabled(override val technicalDetail: String? = null) :
        AppError("Bluetooth is currently turned off. Please enable Bluetooth in system settings", technicalDetail)

    data class PermissionDenied(val missingPermissions: List<String>) :
        AppError("Required permissions are not granted: ${missingPermissions.joinToString()}", null)

    data class DeviceNotFound(val target: String) :
        AppError("No compatible device '$target' found nearby. Ensure earbuds are in pairing mode", null)

    data class ConnectionFailed(val reason: String, override val technicalDetail: String? = null) :
        AppError("Failed to connect to earbuds: $reason", technicalDetail)

    data class Disconnected(val reason: String) :
        AppError("Earbuds disconnected: $reason", null)

    data class OperationTimeout(val operation: String) :
        AppError("The earbud operation '$operation' timed out waiting for device acknowledgement", null)

    data class UnsupportedFeature(val feature: String) :
        AppError("The feature '$feature' is not supported by the detected hardware or firmware", null)

    data class ProtocolNotVerified(val operation: String, val details: String) :
        AppError("Protocol for '$operation' is unverified for this firmware revision: $details", details)

    data class AudioEngineError(val reason: String) :
        AppError("Audio engine error: $reason", null)
}
