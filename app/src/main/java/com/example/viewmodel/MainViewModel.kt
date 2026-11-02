package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioRouteMonitor
import com.example.audio.HearingAssistEngine
import com.example.audio.HearingAssistService
import com.example.audio.HearingAssistSettings
import com.example.audio.RelaxationSound
import com.example.audio.SoundToolsEngine
import com.example.bluetooth.AndroidBluetoothRepository
import com.example.bluetooth.BluetoothRepository
import com.example.bluetooth.DeviceCommandResult
import com.example.data.LocalSettingsRepository
import com.example.data.SettingsRepository
import com.example.domain.ConnectionPhase
import com.example.domain.DeviceTouchSettings
import com.example.domain.EarbudSide
import com.example.domain.EqProfile
import com.example.domain.ListeningMode
import com.example.domain.TouchAction
import com.example.domain.TouchGesture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    SCAN,
    SOUND_SETTINGS,
    TOUCH_SETTINGS,
    HEARING_ASSIST,
    RELAXATION,
    SOUND_TOOLS,
    DIAGNOSTICS,
    HELP,
    SETTINGS,
    PRODUCTS,
    TIPS,
    EQUALIZER
}

data class UserNotification(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val isError: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val bluetoothRepository: BluetoothRepository = AndroidBluetoothRepository(application, viewModelScope)
    val settingsRepository: SettingsRepository = LocalSettingsRepository(application)
    val audioRouteMonitor = AudioRouteMonitor(application)
    val hearingAssistEngine = HearingAssistEngine(application)
    val soundToolsEngine = SoundToolsEngine(viewModelScope)

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _notification = MutableStateFlow<UserNotification?>(null)
    val notification: StateFlow<UserNotification?> = _notification.asStateFlow()

    private val _isModePending = MutableStateFlow(false)
    val isModePending: StateFlow<Boolean> = _isModePending.asStateFlow()

    // Editing draft for touch settings
    private val _draftTouchSettings = MutableStateFlow(DeviceTouchSettings())
    val draftTouchSettings: StateFlow<DeviceTouchSettings> = _draftTouchSettings.asStateFlow()

    // Active selected EQ profile being edited/viewed
    private val _activeEqProfile = MutableStateFlow<EqProfile>(EqProfile.OFFICIAL_MOOD_PRESETS.first())
    val activeEqProfile: StateFlow<EqProfile> = _activeEqProfile.asStateFlow()

    // Preselected Sound Tool tab (Clean Dust vs Drain Moisture)
    val selectedSoundTool = MutableStateFlow(com.example.audio.ToolMode.CLEAN_DUST)

    // A/B Comparison state (Curve vs Flat 0 dB)
    private val _isAbBypassActive = MutableStateFlow(false)
    val isAbBypassActive: StateFlow<Boolean> = _isAbBypassActive.asStateFlow()
    private var preBypassProfile: EqProfile? = null

    // Audio Enhancements (Bass Boost & Spatial Virtualizer)
    private val _bassBoostEnabled = MutableStateFlow(false)
    val bassBoostEnabled: StateFlow<Boolean> = _bassBoostEnabled.asStateFlow()
    private val _bassBoostLevel = MutableStateFlow(50) // 0..100%
    val bassBoostLevel: StateFlow<Int> = _bassBoostLevel.asStateFlow()

    private val _virtualizerEnabled = MutableStateFlow(false)
    val virtualizerEnabled: StateFlow<Boolean> = _virtualizerEnabled.asStateFlow()
    private val _virtualizerLevel = MutableStateFlow(50) // 0..100%
    val virtualizerLevel: StateFlow<Int> = _virtualizerLevel.asStateFlow()

    val savedEqProfiles = settingsRepository.savedEqProfiles.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        EqProfile.OFFICIAL_MOOD_PRESETS + EqProfile.OFFICIAL_ARTIST_PRESETS + EqProfile.DEFAULT_CUSTOM_PRESETS
    )

    val savedTouchSettings = settingsRepository.savedTouchSettings.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DeviceTouchSettings()
    )

    init {
        // Sync active EQ profile ID from settings
        viewModelScope.launch {
            combine(settingsRepository.activeEqProfileId, savedEqProfiles) { activeId, profiles ->
                profiles.find { it.id == activeId } ?: profiles.firstOrNull()
            }.collect { matched ->
                if (matched != null) {
                    _activeEqProfile.value = matched
                }
            }
        }

        // Initialize draft touch settings from saved
        viewModelScope.launch {
            savedTouchSettings.collect { saved ->
                _draftTouchSettings.value = saved
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun showNotification(message: String, isError: Boolean = false) {
        _notification.value = UserNotification(message = message, isError = isError)
    }

    fun dismissNotification() {
        _notification.value = null
    }

    fun startBleScan() {
        viewModelScope.launch {
            bluetoothRepository.startScan()
        }
    }

    fun stopBleScan() {
        viewModelScope.launch {
            bluetoothRepository.stopScan()
        }
    }

    fun connectDevice(deviceId: String) {
        viewModelScope.launch {
            bluetoothRepository.connect(deviceId)
            settingsRepository.setLastConnectedDevice(deviceId, "Ronin Eclipse")
            _currentScreen.value = AppScreen.HOME
        }
    }

    fun disconnectDevice() {
        viewModelScope.launch {
            bluetoothRepository.disconnect()
            showNotification("Disconnected from earbuds")
        }
    }

    private val _pendingListeningMode = MutableStateFlow<ListeningMode?>(null)
    val pendingListeningMode: StateFlow<ListeningMode?> = _pendingListeningMode.asStateFlow()

    fun refreshBattery() {
        viewModelScope.launch {
            bluetoothRepository.refreshBattery()
            showNotification("Battery status refreshed")
        }
    }

    fun selectListeningMode(mode: ListeningMode) {
        if (_isModePending.value) {
            // Prevent conflicting or duplicated commands while a hardware write is in flight
            return
        }
        viewModelScope.launch {
            _isModePending.value = true
            _pendingListeningMode.value = mode
            when (val res = bluetoothRepository.setListeningMode(mode)) {
                is DeviceCommandResult.Confirmed -> {
                    settingsRepository.setPreferredListeningMode(mode)
                    showNotification(res.description)
                }
                is DeviceCommandResult.Rejected -> {
                    showNotification(res.reason, isError = true)
                }
                is DeviceCommandResult.Unsupported -> {
                    showNotification(res.reason, isError = true)
                }
                is DeviceCommandResult.SentButUnconfirmed -> {
                    showNotification(res.description)
                }
            }
            _isModePending.value = false
            _pendingListeningMode.value = null
        }
    }

    fun sendRawDiagnosticCommand(payload: ByteArray) {
        viewModelScope.launch {
            when (val res = bluetoothRepository.sendRawCommand(payload)) {
                is DeviceCommandResult.Confirmed -> showNotification(res.description)
                is DeviceCommandResult.Rejected -> showNotification(res.reason, isError = true)
                is DeviceCommandResult.Unsupported -> showNotification(res.reason, isError = true)
                is DeviceCommandResult.SentButUnconfirmed -> showNotification(res.description)
            }
        }
    }

    fun queryFirmwareVersion() {
        viewModelScope.launch {
            val fw = bluetoothRepository.readFirmwareVersion()
            showNotification("Firmware: ${fw ?: "Not returned by hardware"}")
        }
    }

    // Independent EQ band editing
    fun updateActiveEqBand(centerHz: Int, gainDb: Float) {
        val current = _activeEqProfile.value
        val updated = current.updateBand(centerHz, gainDb)
        _activeEqProfile.value = updated
        viewModelScope.launch {
            if (updated.isUserEditable) {
                settingsRepository.saveEqProfile(updated)
            }
            bluetoothRepository.applyEqProfile(updated)
        }
    }

    fun selectEqProfile(profile: EqProfile) {
        _activeEqProfile.value = profile
        viewModelScope.launch {
            settingsRepository.setActiveEqProfileId(profile.id)
            bluetoothRepository.applyEqProfile(profile)
            showNotification("EQ '${profile.name}' selected")
        }
    }

    fun resetActiveEqProfile() {
        val current = _activeEqProfile.value
        val flat = current.copy(bands = EqProfile.FREQUENCIES.map { com.example.domain.EqBand(it, 0.0f) })
        _activeEqProfile.value = flat
        viewModelScope.launch {
            if (flat.isUserEditable) {
                settingsRepository.saveEqProfile(flat)
            }
            bluetoothRepository.applyEqProfile(flat)
            showNotification("EQ profile reset to flat")
        }
    }

    fun saveCustomEqProfile(name: String) {
        val currentBands = _activeEqProfile.value.bands
        val customId = "custom_${System.currentTimeMillis()}"
        val newProfile = EqProfile(
            id = customId,
            name = name.ifBlank { "Custom" },
            category = com.example.domain.EqPresetCategory.CUSTOM,
            bands = currentBands,
            isUserEditable = true
        )
        viewModelScope.launch {
            settingsRepository.saveEqProfile(newProfile)
            settingsRepository.setActiveEqProfileId(newProfile.id)
            _activeEqProfile.value = newProfile
            bluetoothRepository.applyEqProfile(newProfile)
            showNotification("Saved '${newProfile.name}' as custom preset")
        }
    }

    fun deleteCustomEqProfile(profileId: String) {
        viewModelScope.launch {
            settingsRepository.deleteEqProfile(profileId)
            if (_activeEqProfile.value.id == profileId) {
                val fallback = EqProfile.DEFAULT_PROFILE
                _activeEqProfile.value = fallback
                settingsRepository.setActiveEqProfileId(fallback.id)
                bluetoothRepository.applyEqProfile(fallback)
            }
            showNotification("Custom preset deleted")
        }
    }

    // A/B Comparison Bypass Toggle
    fun toggleAbBypass() {
        if (_isAbBypassActive.value) {
            // Restore saved curve
            preBypassProfile?.let { saved ->
                _activeEqProfile.value = saved
                viewModelScope.launch {
                    bluetoothRepository.applyEqProfile(saved)
                }
            }
            _isAbBypassActive.value = false
            showNotification("A/B: Restored custom EQ curve")
        } else {
            // Save current profile and apply flat 0 dB
            val current = _activeEqProfile.value
            preBypassProfile = current
            val flat = current.copy(bands = EqProfile.FREQUENCIES.map { com.example.domain.EqBand(it, 0.0f) })
            _activeEqProfile.value = flat
            _isAbBypassActive.value = true
            viewModelScope.launch {
                bluetoothRepository.applyEqProfile(flat)
            }
            showNotification("A/B: Comparing against Flat (0 dB)")
        }
    }

    // Bass Boost & Spatial Virtualizer Controls
    fun setBassBoost(enabled: Boolean, level: Int = _bassBoostLevel.value) {
        _bassBoostEnabled.value = enabled
        _bassBoostLevel.value = level.coerceIn(0, 100)
        showNotification(if (enabled) "Bass Boost active ($level%)" else "Bass Boost disabled")
    }

    fun setVirtualizer(enabled: Boolean, level: Int = _virtualizerLevel.value) {
        _virtualizerEnabled.value = enabled
        _virtualizerLevel.value = level.coerceIn(0, 100)
        showNotification(if (enabled) "Spatial Virtualizer active ($level%)" else "Virtualizer disabled")
    }

    // Hearing Calibration Engine Integration
    fun playHearingTone(freqHz: Double, amp: Float) {
        soundToolsEngine.playHearingTestTone(freqHz = freqHz, amplitude = amp, durationMs = 500)
    }

    fun stopHearingTone() {
        soundToolsEngine.stop()
    }

    fun applyHearingTestCompensation(testResults: Map<Int, Float>) {
        // Calculate compensatory gain for each of the 10 bands
        // If detection threshold fraction is higher (ear needs higher sound to hear), provide positive gain boost
        val baseFrequencies = EqProfile.FREQUENCIES
        val compensatedBands = baseFrequencies.map { hz ->
            // Find closest tested frequency
            val closestTestedHz = testResults.keys.minByOrNull { Math.abs(it - hz) } ?: 1000
            val thresholdFraction = testResults[closestTestedHz] ?: 0.5f // 0.05..0.6

            // Normal threshold is around 0.15f. If user only hears at 0.4f, boost by up to +6 dB
            val delta = (thresholdFraction - 0.15f) / 0.35f
            val gain = (delta * 6.0f).coerceIn(-3.0f, 7.5f)
            val roundedGain = Math.round(gain * 2) / 2.0f
            com.example.domain.EqBand(hz, roundedGain)
        }

        val customProfile = EqProfile(
            id = "hearing_calibrated_${System.currentTimeMillis()}",
            name = "Personal Hearing Curve",
            category = com.example.domain.EqPresetCategory.CUSTOM,
            bands = compensatedBands,
            isUserEditable = true
        )

        viewModelScope.launch {
            settingsRepository.saveEqProfile(customProfile)
            settingsRepository.setActiveEqProfileId(customProfile.id)
            _activeEqProfile.value = customProfile
            bluetoothRepository.applyEqProfile(customProfile)
            showNotification("Calibrated EQ applied & saved to presets!")
        }
    }


    // Touch controls draft & apply
    fun updateDraftTouchAction(side: EarbudSide, gesture: TouchGesture, action: TouchAction) {
        val current = _draftTouchSettings.value
        val updated = if (side == EarbudSide.LEFT) {
            current.copy(leftConfig = current.leftConfig.withAction(gesture, action))
        } else {
            current.copy(rightConfig = current.rightConfig.withAction(gesture, action))
        }
        _draftTouchSettings.value = updated
    }

    fun applyTouchSettings() {
        viewModelScope.launch {
            val toSave = _draftTouchSettings.value.copy(
                isAppliedToEarbuds = true,
                lastAppliedTimestamp = System.currentTimeMillis()
            )
            settingsRepository.saveTouchSettings(toSave)
            when (val res = bluetoothRepository.applyTouchSettings(toSave)) {
                is DeviceCommandResult.Confirmed -> {
                    showNotification("Touch settings applied to Ronin Eclipse")
                }
                is DeviceCommandResult.Rejected -> {
                    showNotification("Device rejected touch settings: ${res.reason}", isError = true)
                }
                else -> {
                    showNotification("Touch settings saved locally")
                }
            }
        }
    }

    fun resetTouchSettingsToDefault() {
        val def = DeviceTouchSettings()
        _draftTouchSettings.value = def
        viewModelScope.launch {
            settingsRepository.saveTouchSettings(def)
            bluetoothRepository.applyTouchSettings(def)
            showNotification("Touch controls restored to factory defaults")
        }
    }

    // Hearing assist
    fun startHearingAssist(): Boolean {
        val started = hearingAssistEngine.start()
        if (started) {
            HearingAssistService.startService(getApplication())
            showNotification("Hearing Assist started")
        } else {
            showNotification(hearingAssistEngine.errorMessage.value ?: "Could not start audio stream", isError = true)
        }
        return started
    }

    fun stopHearingAssist() {
        hearingAssistEngine.stop()
        HearingAssistService.stopService(getApplication())
        showNotification("Hearing Assist stopped")
    }

    fun updateHearingAssistSettings(settings: HearingAssistSettings) {
        hearingAssistEngine.updateSettings(settings)
    }

    // Sound tools
    fun startCleanDust() {
        soundToolsEngine.startCleanDust(15)
        showNotification("Dust cleaning vibration pulse started")
    }

    fun startDrainMoisture() {
        soundToolsEngine.startDrainMoisture(15)
        showNotification("Moisture drainage pulse started")
    }

    fun startRelaxation(sound: RelaxationSound) {
        soundToolsEngine.startRelaxation(sound, 10)
        showNotification("Playing ${sound.displayName}")
    }

    fun stopSoundTool() {
        soundToolsEngine.stop()
        showNotification("Audio stopped")
    }

    fun renameEarbuds(newName: String) {
        viewModelScope.launch {
            when (val res = bluetoothRepository.setDeviceName(newName)) {
                is DeviceCommandResult.Confirmed -> {
                    showNotification("Renamed to '$newName'")
                }
                is DeviceCommandResult.Rejected -> {
                    showNotification(res.reason, isError = true)
                }
                else -> {
                    showNotification("Name change saved locally")
                }
            }
        }
    }

    fun openSoundTool(mode: com.example.audio.ToolMode) {
        selectedSoundTool.value = mode
        _currentScreen.value = AppScreen.SOUND_TOOLS
    }

    fun exportCustomProfilesJson(): String {
        val customOnly = savedEqProfiles.value.filter { it.category == com.example.domain.EqPresetCategory.CUSTOM }
        val array = org.json.JSONArray()
        for (p in customOnly) {
            val obj = org.json.JSONObject().apply {
                put("version", 1)
                put("name", p.name)
                val bandsArray = org.json.JSONArray()
                p.bands.forEach { b ->
                    bandsArray.put(org.json.JSONObject().apply {
                        put("hz", b.centerHz)
                        put("gain", b.gainDb.toDouble())
                    })
                }
                put("bands", bandsArray)
            }
            array.put(obj)
        }
        return array.toString(2)
    }

    fun importCustomProfilesJson(jsonStr: String): Boolean {
        return try {
            val array = org.json.JSONArray(jsonStr)
            var count = 0
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val name = obj.getString("name")
                val bandsArray = obj.getJSONArray("bands")
                val bands = mutableListOf<com.example.domain.EqBand>()
                for (j in 0 until bandsArray.length()) {
                    val bObj = bandsArray.getJSONObject(j)
                    val hz = bObj.getInt("hz")
                    val gain = bObj.getDouble("gain").toFloat()
                    bands.add(com.example.domain.EqBand(hz, gain.coerceIn(-12f, 12f)))
                }
                val newProfile = com.example.domain.EqProfile(
                    id = "imported_${System.currentTimeMillis()}_$i",
                    name = name,
                    category = com.example.domain.EqPresetCategory.CUSTOM,
                    bands = bands,
                    isUserEditable = true
                )
                viewModelScope.launch {
                    settingsRepository.saveEqProfile(newProfile)
                }
                count++
            }
            showNotification("Successfully imported $count custom EQ profiles")
            true
        } catch (e: Exception) {
            showNotification("Invalid EQ profile JSON format", isError = true)
            false
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            settingsRepository.clearLocalData()
            bluetoothRepository.disconnect()
            showNotification("All local app preferences cleared")
        }
    }
}
