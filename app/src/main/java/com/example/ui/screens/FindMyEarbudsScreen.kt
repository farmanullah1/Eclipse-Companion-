package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.ToolMode
import com.example.ui.theme.DarkAccent
import com.example.ui.theme.DarkAccentLight
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun FindMyEarbudsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val soundMode by viewModel.soundToolsEngine.currentMode.collectAsState()
    val isChirping = soundMode == ToolMode.FIND_MY_EARBUDS

    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_ring"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .testTag("find_my_earbuds_screen"),
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
                onClick = {
                    viewModel.stopSoundTool()
                    viewModel.navigateTo(AppScreen.HOME)
                },
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
                text = "Find My Earbuds",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.size(42.dp))
        }

        // Safety Warning Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2415)),
            border = BorderStroke(1.dp, Color(0xFFFBBF24))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Do not trigger the acoustic alarm while earbuds are in your ears. The high-volume sound may cause discomfort.",
                    fontSize = 11.sp,
                    color = Color(0xFFFDE68A),
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Radar Visualizer Canvas
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(Color(0xFF16181D)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxRadius = size.width / 2f

                // Static grid concentric circles
                for (step in 1..3) {
                    val r = maxRadius * (step / 3f)
                    drawCircle(
                        color = Color(0xFF2A2D36),
                        radius = r,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )
                }

                // Crosshairs
                drawLine(
                    color = Color(0xFF242730),
                    start = Offset(0f, center.y),
                    end = Offset(size.width, center.y),
                    strokeWidth = 1f
                )
                drawLine(
                    color = Color(0xFF242730),
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, size.height),
                    strokeWidth = 1f
                )

                // Animated pulsing ring
                val activeRadius = maxRadius * pulseProgress
                val alpha = (1f - pulseProgress).coerceIn(0f, 1f)
                drawCircle(
                    color = DarkAccent.copy(alpha = alpha * 0.7f),
                    radius = activeRadius,
                    center = center,
                    style = Stroke(width = 3f)
                )
            }

            // Radar Center Icon
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(DarkAccent.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = null,
                    tint = DarkAccentLight,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Proximity Status Text
        Text(
            text = "PROXIMITY RADAR: NEARBY (~2 METERS)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF82C9A5),
            letterSpacing = 1.sp
        )
        Text(
            text = "Signal: -62 dBm • Connected via BLE",
            fontSize = 12.sp,
            color = Color(0xFF9EA0A6)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Acoustic Locator Chirp Control Buttons
        Text(
            text = "Play Acoustic Locator Chirp",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Plays a high-pitch sweeping beacon tone that ramps up in volume to help you find lost earbuds.",
            fontSize = 11.sp,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { viewModel.startLocatorChirp("LEFT") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("chirp_left_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2024)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF383C46))
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp), tint = DarkAccentLight)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Left Earbud", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = { viewModel.startLocatorChirp("RIGHT") },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("chirp_right_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2024)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF383C46))
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp), tint = DarkAccentLight)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Right Earbud", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { viewModel.startLocatorChirp("BOTH") },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("chirp_both_button"),
            colors = ButtonDefaults.buttonColors(containerColor = DarkAccent),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Hearing, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Chirp Both Earbuds", fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (isChirping) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { viewModel.stopSoundTool() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("stop_chirp_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Locator Sound", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
