package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ListeningMode
import com.example.ui.theme.DarkAccentLight

@Composable
fun ListeningModeSelector(
    activeMode: ListeningMode?,
    pendingMode: ListeningMode?,
    onSelectMode: (ListeningMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAnyPending = pendingMode != null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF222428))
            .border(1.dp, Color(0xFF33363D), RoundedCornerShape(32.dp))
            .padding(4.dp)
            .testTag("listening_mode_selector")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeItem(
                title = "Noise Reduction",
                icon = Icons.Default.GraphicEq,
                isSelected = activeMode == ListeningMode.ANC,
                isPending = pendingMode == ListeningMode.ANC,
                modifier = Modifier.weight(1.3f),
                onClick = {
                    if (!isAnyPending && activeMode != ListeningMode.ANC) {
                        onSelectMode(ListeningMode.ANC)
                    }
                }
            )

            ModeItem(
                title = "Off",
                icon = Icons.Default.PowerSettingsNew,
                isSelected = activeMode == ListeningMode.NORMAL,
                isPending = pendingMode == ListeningMode.NORMAL,
                modifier = Modifier.weight(1f),
                onClick = {
                    if (!isAnyPending && activeMode != ListeningMode.NORMAL) {
                        onSelectMode(ListeningMode.NORMAL)
                    }
                }
            )

            ModeItem(
                title = "Transparency",
                icon = Icons.Default.SurroundSound,
                isSelected = activeMode == ListeningMode.TRANSPARENCY,
                isPending = pendingMode == ListeningMode.TRANSPARENCY,
                modifier = Modifier.weight(1.3f),
                onClick = {
                    if (!isAnyPending && activeMode != ListeningMode.TRANSPARENCY) {
                        onSelectMode(ListeningMode.TRANSPARENCY)
                    }
                }
            )
        }
    }
}

@Composable
private fun ModeItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    isPending: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF4B4D58) else Color.Transparent,
        animationSpec = tween(150),
        label = "mode_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFFF3F3F0) else Color(0xFF9EA0A6),
        animationSpec = tween(150),
        label = "mode_text"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) DarkAccentLight else Color(0xFF9EA0A6),
        animationSpec = tween(150),
        label = "mode_icon"
    )

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isPending) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = DarkAccentLight
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                maxLines = 1
            )
        }
    }
}
