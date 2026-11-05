package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkAccent
import com.example.ui.theme.DarkAccentLight
import com.example.ui.theme.DarkSuccess
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun HearingHealthScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val dosePercent by viewModel.dailyNoiseDosePercent.collectAsState()
    val safeLimitEnabled by viewModel.safeVolumeLimitEnabled.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .testTag("hearing_health_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF222428))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Hearing Health",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Daily Sound Dose Circular Gauge Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2024)),
            border = BorderStroke(1.dp, Color(0xFF2C2F36))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DAILY SOUND EXPOSURE (WHO 85 dBA)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8E9199),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Circular Progress Arc
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 12.dp.toPx()
                        // Track
                        drawArc(
                            color = Color(0xFF2A2D35),
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                        // Active Dose
                        val sweep = 270f * (dosePercent / 100f).coerceIn(0f, 1f)
                        val gaugeColor = when {
                            dosePercent > 100 -> Color(0xFFEF4444)
                            dosePercent > 80 -> Color(0xFFFBBF24)
                            else -> DarkSuccess
                        }
                        drawArc(
                            color = gaugeColor,
                            startAngle = 135f,
                            sweepAngle = sweep,
                            useCenter = false,
                            style = Stroke(strokeWidth, cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$dosePercent%",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (dosePercent <= 100) "Safe Range" else "High Exposure",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (dosePercent <= 100) DarkSuccess else Color(0xFFEF4444)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Estimated 5.5 hours remaining safe listening today at current volume level.",
                    fontSize = 12.sp,
                    color = Color(0xFF9EA0A6),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Safe Volume Limiter Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2024)),
            border = BorderStroke(1.dp, Color(0xFF2C2F36))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DarkAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.VolumeDown, contentDescription = null, tint = DarkAccentLight, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Safe Volume Limiter (85 dB)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Prevents sound output from exceeding safe 85 dBA limits",
                            fontSize = 11.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                Switch(
                    checked = safeLimitEnabled,
                    onCheckedChange = { viewModel.setSafeVolumeLimit(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = DarkAccent,
                        uncheckedThumbColor = Color(0xFF6B7280),
                        uncheckedTrackColor = Color(0xFF2A2D35)
                    ),
                    modifier = Modifier.testTag("safe_volume_switch")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Acoustic Guidelines Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16181D)),
            border = BorderStroke(1.dp, Color(0xFF262930))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = DarkAccentLight, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("World Health Organization Standards", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Continuous listening above 85 dB for more than 8 hours daily causes cumulative noise fatigue. Keeping the volume below 70% preserves acoustic clarity and prevents permanent hearing loss.",
                    fontSize = 11.sp,
                    color = Color(0xFF9CA3AF),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
