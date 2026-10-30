package com.example.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AudioRouteInfo(
    val isBluetoothHeadsetConnected: Boolean = false,
    val isWiredHeadsetConnected: Boolean = false,
    val currentRouteName: String = "Built-in Speaker"
)

class AudioRouteMonitor(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _routeState = MutableStateFlow(AudioRouteInfo())
    val routeState: StateFlow<AudioRouteInfo> = _routeState.asStateFlow()

    init {
        updateCurrentRoute()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && audioManager != null) {
            audioManager.registerAudioDeviceCallback(object : AudioDeviceCallback() {
                override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
                    updateCurrentRoute()
                }

                override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
                    updateCurrentRoute()
                }
            }, null)
        }
    }

    fun updateCurrentRoute() {
        val am = audioManager ?: return
        var bt = false
        var wired = false
        var route = "Phone Speaker"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (d in devices) {
                when (d.type) {
                    AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
                    AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
                    AudioDeviceInfo.TYPE_BLE_HEADSET -> {
                        bt = true
                        route = "Bluetooth Earbuds (${d.productName.takeIf { !it.isNullOrBlank() } ?: "Ronin Eclipse"})"
                    }
                    AudioDeviceInfo.TYPE_WIRED_HEADSET,
                    AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                    AudioDeviceInfo.TYPE_USB_HEADSET -> {
                        wired = true
                        if (!bt) route = "Wired Headphones"
                    }
                }
            }
        } else {
            @Suppress("DEPRECATION")
            bt = am.isBluetoothA2dpOn || am.isBluetoothScoOn
            @Suppress("DEPRECATION")
            wired = am.isWiredHeadsetOn
            if (bt) route = "Bluetooth Audio"
            else if (wired) route = "Wired Headphones"
        }

        _routeState.value = AudioRouteInfo(
            isBluetoothHeadsetConnected = bt,
            isWiredHeadsetConnected = wired,
            currentRouteName = route
        )
    }
}
