package com.example.data

import com.example.domain.DeviceTouchSettings
import com.example.domain.EqProfile
import com.example.domain.ListeningMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val preferredListeningMode: Flow<ListeningMode?>
    val darkThemeEnabled: Flow<Boolean?> // null means follow system
    val lowBatteryWarningThreshold: Flow<Int>
    val lastConnectedDeviceAddress: Flow<String?>
    val lastConnectedDeviceName: Flow<String?>
    val savedEqProfiles: Flow<List<EqProfile>>
    val activeEqProfileId: Flow<String>
    val savedTouchSettings: Flow<DeviceTouchSettings>
    val gamingModeEnabled: Flow<Boolean>

    suspend fun setPreferredListeningMode(mode: ListeningMode?)
    suspend fun setDarkThemeEnabled(enabled: Boolean?)
    suspend fun setLowBatteryWarningThreshold(threshold: Int)
    suspend fun setLastConnectedDevice(address: String?, name: String?)
    suspend fun saveEqProfile(profile: EqProfile)
    suspend fun setActiveEqProfileId(id: String)
    suspend fun deleteEqProfile(id: String)
    suspend fun saveTouchSettings(settings: DeviceTouchSettings)
    suspend fun setGamingMode(enabled: Boolean)
    suspend fun clearLocalData()
}
