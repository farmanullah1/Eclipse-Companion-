package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.DeviceTouchSettings
import com.example.domain.EarbudSide
import com.example.domain.EarbudTouchConfig
import com.example.domain.EqBand
import com.example.domain.EqPresetCategory
import com.example.domain.EqProfile
import com.example.domain.ListeningMode
import com.example.domain.TouchAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "eclipse_companion_prefs")

class LocalSettingsRepository(private val context: Context) : SettingsRepository {

    private object Keys {
        val PREFERRED_MODE = stringPreferencesKey("pref_listening_mode")
        val DARK_THEME = stringPreferencesKey("pref_dark_theme")
        val LOW_BATTERY_THRESHOLD = intPreferencesKey("pref_low_batt_threshold")
        val LAST_DEVICE_ADDRESS = stringPreferencesKey("pref_last_device_addr")
        val LAST_DEVICE_NAME = stringPreferencesKey("pref_last_device_name")
        val ACTIVE_EQ_ID = stringPreferencesKey("pref_active_eq_id")
        val CUSTOM_EQ_JSON = stringPreferencesKey("pref_custom_eq_json")
        val TOUCH_SETTINGS_JSON = stringPreferencesKey("pref_touch_settings_json")
        val GAMING_MODE = booleanPreferencesKey("pref_gaming_mode")
    }

    override val preferredListeningMode: Flow<ListeningMode?> = context.dataStore.data.map { prefs ->
        prefs[Keys.PREFERRED_MODE]?.let { modeStr ->
            try { ListeningMode.valueOf(modeStr) } catch (_: Exception) { null }
        }
    }

    override val darkThemeEnabled: Flow<Boolean?> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.DARK_THEME]) {
            "true" -> true
            "false" -> false
            else -> null
        }
    }

    override val lowBatteryWarningThreshold: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.LOW_BATTERY_THRESHOLD] ?: 20
    }

    override val lastConnectedDeviceAddress: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_DEVICE_ADDRESS]
    }

    override val lastConnectedDeviceName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_DEVICE_NAME]
    }

    override val activeEqProfileId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_EQ_ID] ?: "mood_default"
    }

    override val savedEqProfiles: Flow<List<EqProfile>> = context.dataStore.data.map { prefs ->
        val customJson = prefs[Keys.CUSTOM_EQ_JSON]
        val customProfiles = if (!customJson.isNullOrBlank()) {
            deserializeCustomEqProfiles(customJson)
        } else {
            EqProfile.DEFAULT_CUSTOM_PRESETS
        }
        EqProfile.OFFICIAL_MOOD_PRESETS + EqProfile.OFFICIAL_ARTIST_PRESETS + customProfiles
    }

    override val savedTouchSettings: Flow<DeviceTouchSettings> = context.dataStore.data.map { prefs ->
        val json = prefs[Keys.TOUCH_SETTINGS_JSON]
        if (!json.isNullOrBlank()) {
            deserializeTouchSettings(json)
        } else {
            DeviceTouchSettings()
        }
    }

    override val gamingModeEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.GAMING_MODE] ?: false
    }

    override suspend fun setPreferredListeningMode(mode: ListeningMode?) {
        context.dataStore.edit { prefs ->
            if (mode != null) prefs[Keys.PREFERRED_MODE] = mode.name
            else prefs.remove(Keys.PREFERRED_MODE)
        }
    }

    override suspend fun setDarkThemeEnabled(enabled: Boolean?) {
        context.dataStore.edit { prefs ->
            if (enabled != null) prefs[Keys.DARK_THEME] = enabled.toString()
            else prefs.remove(Keys.DARK_THEME)
        }
    }

    override suspend fun setLowBatteryWarningThreshold(threshold: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LOW_BATTERY_THRESHOLD] = threshold.coerceIn(5, 50)
        }
    }

    override suspend fun setLastConnectedDevice(address: String?, name: String?) {
        context.dataStore.edit { prefs ->
            if (address != null) prefs[Keys.LAST_DEVICE_ADDRESS] = address else prefs.remove(Keys.LAST_DEVICE_ADDRESS)
            if (name != null) prefs[Keys.LAST_DEVICE_NAME] = name else prefs.remove(Keys.LAST_DEVICE_NAME)
        }
    }

    override suspend fun saveEqProfile(profile: EqProfile) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[Keys.CUSTOM_EQ_JSON]
            val currentList = if (!currentJson.isNullOrBlank()) deserializeCustomEqProfiles(currentJson) else EqProfile.DEFAULT_CUSTOM_PRESETS
            val updated = currentList.filter { it.id != profile.id } + profile
            prefs[Keys.CUSTOM_EQ_JSON] = serializeCustomEqProfiles(updated)
            prefs[Keys.ACTIVE_EQ_ID] = profile.id
        }
    }

    override suspend fun setActiveEqProfileId(id: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_EQ_ID] = id
        }
    }

    override suspend fun deleteEqProfile(id: String) {
        context.dataStore.edit { prefs ->
            val currentJson = prefs[Keys.CUSTOM_EQ_JSON]
            val currentList = if (!currentJson.isNullOrBlank()) deserializeCustomEqProfiles(currentJson) else EqProfile.DEFAULT_CUSTOM_PRESETS
            val updated = currentList.filter { it.id != id }
            prefs[Keys.CUSTOM_EQ_JSON] = serializeCustomEqProfiles(updated)
            if (prefs[Keys.ACTIVE_EQ_ID] == id) {
                prefs[Keys.ACTIVE_EQ_ID] = "mood_default"
            }
        }
    }

    override suspend fun saveTouchSettings(settings: DeviceTouchSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOUCH_SETTINGS_JSON] = serializeTouchSettings(settings)
        }
    }

    override suspend fun setGamingMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GAMING_MODE] = enabled
        }
    }

    override suspend fun clearLocalData() {
        context.dataStore.edit { it.clear() }
    }

    private fun serializeCustomEqProfiles(profiles: List<EqProfile>): String {
        val array = JSONArray()
        for (p in profiles) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("category", p.category.name)
                put("createdAt", p.createdAtEpochMillis)
                val bandsArray = JSONArray()
                for (b in p.bands) {
                    bandsArray.put(JSONObject().apply {
                        put("hz", b.centerHz)
                        put("gain", b.gainDb.toDouble())
                    })
                }
                put("bands", bandsArray)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun deserializeCustomEqProfiles(json: String): List<EqProfile> {
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<EqProfile>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.getString("id")
                val name = obj.getString("name")
                val cat = EqPresetCategory.CUSTOM
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                val bandsArray = obj.getJSONArray("bands")
                val bands = mutableListOf<EqBand>()
                for (j in 0 until bandsArray.length()) {
                    val bObj = bandsArray.getJSONObject(j)
                    val hz = bObj.getInt("hz")
                    val gain = bObj.getDouble("gain").toFloat()
                    bands.add(EqBand(hz, gain))
                }
                list.add(EqProfile(id, name, cat, bands, createdAt, isUserEditable = true))
            }
            list
        } catch (_: Exception) {
            EqProfile.DEFAULT_CUSTOM_PRESETS
        }
    }

    private fun serializeTouchSettings(settings: DeviceTouchSettings): String {
        return JSONObject().apply {
            put("left", JSONObject().apply {
                put("oneTap", settings.leftConfig.oneTap.name)
                put("twoTap", settings.leftConfig.twoTap.name)
                put("threeTap", settings.leftConfig.threeTap.name)
                put("tapAndHold", settings.leftConfig.tapAndHold.name)
            })
            put("right", JSONObject().apply {
                put("oneTap", settings.rightConfig.oneTap.name)
                put("twoTap", settings.rightConfig.twoTap.name)
                put("threeTap", settings.rightConfig.threeTap.name)
                put("tapAndHold", settings.rightConfig.tapAndHold.name)
            })
            put("applied", settings.isAppliedToEarbuds)
            put("appliedAt", settings.lastAppliedTimestamp ?: 0L)
        }.toString()
    }

    private fun deserializeTouchSettings(json: String): DeviceTouchSettings {
        return try {
            val root = JSONObject(json)
            val leftObj = root.optJSONObject("left")
            val rightObj = root.optJSONObject("right")

            fun parseAction(obj: JSONObject?, key: String, default: TouchAction): TouchAction {
                val name = obj?.optString(key) ?: return default
                return try { TouchAction.valueOf(name) } catch (_: Exception) { default }
            }

            val left = EarbudTouchConfig(
                side = EarbudSide.LEFT,
                oneTap = parseAction(leftObj, "oneTap", TouchAction.NO_ACTION),
                twoTap = parseAction(leftObj, "twoTap", TouchAction.ANC_SWITCHING),
                threeTap = parseAction(leftObj, "threeTap", TouchAction.PREVIOUS_TRACK),
                tapAndHold = parseAction(leftObj, "tapAndHold", TouchAction.VOICE_ASSISTANT)
            )

            val right = EarbudTouchConfig(
                side = EarbudSide.RIGHT,
                oneTap = parseAction(rightObj, "oneTap", TouchAction.NO_ACTION),
                twoTap = parseAction(rightObj, "twoTap", TouchAction.PLAY_PAUSE),
                threeTap = parseAction(rightObj, "threeTap", TouchAction.NEXT_TRACK),
                tapAndHold = parseAction(rightObj, "tapAndHold", TouchAction.GAMING_MODE)
            )

            DeviceTouchSettings(
                leftConfig = left,
                rightConfig = right,
                isAppliedToEarbuds = root.optBoolean("applied", false),
                lastAppliedTimestamp = root.optLong("appliedAt").takeIf { it > 0 }
            )
        } catch (_: Exception) {
            DeviceTouchSettings()
        }
    }
}
