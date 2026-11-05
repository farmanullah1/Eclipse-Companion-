package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.theme.DarkAccent
import com.example.ui.theme.DarkAccentLight
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun SpatialAudioScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val spatialEnabled by viewModel.spatialAudioEnabled.collectAsState()
    val roomSize by viewModel.spatialRoomSize.collectAsState()
    val stageWidth by viewModel.spatialWidth.collectAsState()
    val warmth by viewModel.spatialWarmth.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .testTag("spatial_audio_screen"),
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
                text = "Spatial Soundstage",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3D Soundstage Visualizer Canvas
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(Color(0xFF16181D)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = size.width / 2.5f

                // Head icon reference in center
                drawCircle(color = Color(0xFF2E313A), radius = 24.dp.toPx(), center = center)

                // Virtual speaker nodes Left & Right expanding with stageWidth
                val widthOffset = (stageWidth / 100f) * 45.dp.toPx()
                val leftSpeaker = Offset(center.x - widthOffset - 16.dp.toPx(), center.y - 12.dp.toPx())
                val rightSpeaker = Offset(center.x + widthOffset + 16.dp.toPx(), center.y - 12.dp.toPx())

                val activeColor = if (spatialEnabled) DarkAccentLight else Color(0xFF4A4E58)

                drawCircle(color = activeColor, radius = 9.dp.toPx(), center = leftSpeaker)
                drawCircle(color = activeColor, radius = 9.dp.toPx(), center = rightSpeaker)

                // Room boundary arc
                val roomRadius = baseRadius * ((roomSize + 50) / 150f)
                drawCircle(
                    color = if (spatialEnabled) DarkAccent.copy(alpha = 0.4f) else Color(0xFF2A2D35),
                    radius = roomRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            Icon(
                imageVector = Icons.Default.SurroundSound,
                contentDescription = null,
                tint = if (spatialEnabled) DarkAccentLight else Color(0xFF6B7280),
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (spatialEnabled) "3D SOUNDSTAGE ACTIVE" else "SPATIAL AUDIO OFF",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (spatialEnabled) DarkAccentLight else Color(0xFF8E9199),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Master Spatial Audio Switch Card
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Spatial Soundstage Studio",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Expands stereo music into an immersive 3D acoustic room",
                        fontSize = 11.sp,
                        color = Color(0xFF9EA0A6)
                    )
                }

                Switch(
                    checked = spatialEnabled,
                    onCheckedChange = {
                        viewModel.setSpatialAudio(it, roomSize, stageWidth, warmth)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = DarkAccent,
                        uncheckedThumbColor = Color(0xFF6B7280),
                        uncheckedTrackColor = Color(0xFF2A2D35)
                    ),
                    modifier = Modifier.testTag("spatial_audio_switch")
                )
            }
        }

        // Sliders Card (Visible when enabled)
        AnimatedVisibility(visible = spatialEnabled) {
            Column(modifier = Modifier.padding(top = 14.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2024)),
                    border = BorderStroke(1.dp, Color(0xFF2C2F36))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Room Size Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Virtual Room Dimension", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text("$roomSize%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkAccentLight)
                            }
                            Slider(
                                value = roomSize.toFloat(),
                                onValueChange = { viewModel.setSpatialAudio(true, it.toInt(), stageWidth, warmth) },
                                valueRange = 10f..100f,
                                colors = SliderDefaults.colors(thumbColor = DarkAccentLight, activeTrackColor = DarkAccent),
                                modifier = Modifier.testTag("spatial_room_slider")
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Studio Booth", fontSize = 10.sp, color = Color(0xFF6B7280))
                                Text("Live Club", fontSize = 10.sp, color = Color(0xFF6B7280))
                                Text("Concert Hall", fontSize = 10.sp, color = Color(0xFF6B7280))
                            }
                        }

                        // Stereo Width Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Acoustic Stage Width", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text("$stageWidth%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkAccentLight)
                            }
                            Slider(
                                value = stageWidth.toFloat(),
                                onValueChange = { viewModel.setSpatialAudio(true, roomSize, it.toInt(), warmth) },
                                valueRange = 10f..100f,
                                colors = SliderDefaults.colors(thumbColor = DarkAccentLight, activeTrackColor = DarkAccent),
                                modifier = Modifier.testTag("spatial_width_slider")
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Centered", fontSize = 10.sp, color = Color(0xFF6B7280))
                                Text("Wide Stereo", fontSize = 10.sp, color = Color(0xFF6B7280))
                                Text("360° Panorama", fontSize = 10.sp, color = Color(0xFF6B7280))
                            }
                        }

                        // Reverberant Warmth Slider
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Reverberant Warmth", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                Text("$warmth%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkAccentLight)
                            }
                            Slider(
                                value = warmth.toFloat(),
                                onValueChange = { viewModel.setSpatialAudio(true, roomSize, stageWidth, it.toInt()) },
                                valueRange = 0f..100f,
                                colors = SliderDefaults.colors(thumbColor = DarkAccentLight, activeTrackColor = DarkAccent),
                                modifier = Modifier.testTag("spatial_warmth_slider")
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
