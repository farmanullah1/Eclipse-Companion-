package com.example.quicksettings

import android.content.Context
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class EclipseTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()
        val prefs = getSharedPreferences("eclipse_widget_prefs", Context.MODE_PRIVATE)
        val current = prefs.getString("active_mode", "ANC") ?: "ANC"
        val next = when (current) {
            "ANC" -> "NORMAL"
            "NORMAL" -> "TRANSPARENCY"
            else -> "ANC"
        }
        prefs.edit().putString("active_mode", next).apply()
        updateTileState()
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val prefs = getSharedPreferences("eclipse_widget_prefs", Context.MODE_PRIVATE)
        val current = prefs.getString("active_mode", "ANC") ?: "ANC"

        when (current) {
            "ANC" -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "ANC On"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Noise Reduction"
                }
            }
            "NORMAL" -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "ANC Off"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Normal Mode"
                }
            }
            "TRANSPARENCY" -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "Transparency"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.subtitle = "Ambient Aware"
                }
            }
        }
        tile.updateTile()
    }
}
