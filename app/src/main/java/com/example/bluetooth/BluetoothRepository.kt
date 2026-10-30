package com.example.bluetooth

import com.example.domain.BatteryState
import com.example.domain.DeviceTouchSettings
import com.example.domain.EqProfile
import com.example.domain.ListeningMode
import com.example.domain.ProtocolCapabilities
import kotlinx.coroutines.flow.StateFlow

interface BluetoothRepository {
    val connectionState: StateFlow<ConnectionState>
    val nearbyDevices: StateFlow<List<NearbyDevice>>
    val batteryState: StateFlow<BatteryState>
    val currentListeningMode: StateFlow<ListeningMode?>
    val capabilities: StateFlow<ProtocolCapabilities>
    val diagnosticsLogs: StateFlow<List<String>>
    val discoveredServices: StateFlow<List<DiscoveredGattService>>

    suspend fun startScan(timeoutMillis: Long = 12_000L)
    suspend fun stopScan()
    suspend fun connect(deviceId: String)
    suspend fun disconnect()
    suspend fun refreshBattery(): BatteryState
    suspend fun setListeningMode(mode: ListeningMode): DeviceCommandResult
    suspend fun applyEqProfile(profile: EqProfile): DeviceCommandResult
    suspend fun applyTouchSettings(settings: DeviceTouchSettings): DeviceCommandResult
    suspend fun setGamingMode(enabled: Boolean): DeviceCommandResult
    suspend fun setDeviceName(newName: String): DeviceCommandResult
    suspend fun readFirmwareVersion(): String?
    suspend fun sendRawCommand(payload: ByteArray): DeviceCommandResult
}
