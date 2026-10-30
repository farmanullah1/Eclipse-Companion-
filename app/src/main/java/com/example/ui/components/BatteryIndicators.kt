package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.BatteryState
import com.example.domain.ReportedValue
import com.example.ui.theme.DarkSuccess

@Composable
fun EarbudBatteryRow(
    batteryState: BatteryState,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BatteryPill(
            label = "L",
            reading = batteryState.left
        )
        BatteryPill(
            label = "R",
            reading = batteryState.right
        )
        if (batteryState.case.isAvailable) {
            BatteryPill(
                label = "Case",
                reading = batteryState.case
            )
        }
    }
}

@Composable
private fun BatteryPill(
    label: String,
    reading: ReportedValue<Int>
) {
    val (text, color, icon) = when (reading) {
        is ReportedValue.Available -> {
            val pct = reading.value
            val c = if (pct <= 20) Color(0xFFF2A09A) else DarkSuccess
            Triple("$pct%", c, Icons.Default.BatteryFull)
        }
        is ReportedValue.Unavailable -> {
            Triple("--", Color(0xFF7A7D85), Icons.Default.BatteryFull)
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF222428))
            .border(1.dp, Color(0xFF33363D), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB0B3BA)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF3F3F0)
            )
        }
    }
}
