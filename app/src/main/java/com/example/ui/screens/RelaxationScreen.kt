package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.RelaxationSound
import com.example.audio.ToolMode
import com.example.ui.theme.DarkAccentLight
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun RelaxationScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.soundToolsEngine.currentMode.collectAsState()
    val activeSound by viewModel.soundToolsEngine.activeRelaxationSound.collectAsState()
    val progressSec by viewModel.soundToolsEngine.progressSeconds.collectAsState()
    val totalSec by viewModel.soundToolsEngine.totalDurationSeconds.collectAsState()

    var selectedDurationMinutes by remember { mutableIntStateOf(10) }
    val isPlaying = currentMode == ToolMode.RELAXATION
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .testTag("relaxation_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.HOME) },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF222428))
                    .size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Relaxation & Meditation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Large Ambient Orb
        Box(
            modifier = Modifier
                .size(170.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E2129))
                .border(2.dp, if (isPlaying) DarkAccentLight else Color(0xFF2A2D36), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Spa,
                    contentDescription = null,
                    tint = if (isPlaying) DarkAccentLight else Color(0xFF8E9199),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                val remSec = maxOf(0, totalSec - progressSec)
                Text(
                    text = String.format("%02d:%02d", remSec / 60, remSec % 60),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isPlaying) {
            LinearProgressIndicator(
                progress = { (progressSec.toFloat() / maxOf(1, totalSec)).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF5D5FEF),
                trackColor = Color(0xFF26282E)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Sounds Grid
        Text(
            text = "SELECT SOUND PROFILE",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8E9199),
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RelaxationSound.entries.forEach { sound ->
                val isSelected = activeSound == sound
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) Color(0xFF252834) else Color(0xFF1E2024))
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF5D5FEF) else Color(0xFF2C2F36),
                            RoundedCornerShape(14.dp)
                        )
                        .clickable { viewModel.startRelaxation(sound) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sound.displayName,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFFD1D5DB)
                        )
                        if (isSelected && isPlaying) {
                            Text("Playing", color = DarkAccentLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Timer Row
        Text(
            text = "SESSION DURATION",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8E9199),
            letterSpacing = 1.sp,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(5, 10, 15, 30).forEach { mins ->
                val isSel = selectedDurationMinutes == mins
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSel) Color(0xFF5D5FEF) else Color(0xFF1E2024))
                        .clickable {
                            selectedDurationMinutes = mins
                            if (isPlaying) {
                                viewModel.startRelaxation(activeSound)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${mins}m",
                        fontSize = 13.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSel) Color.White else Color(0xFFB0B3BA)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Play / Pause Button
        Button(
            onClick = {
                if (isPlaying) viewModel.stopSoundTool()
                else viewModel.startRelaxation(activeSound)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .testTag("relaxation_play_toggle_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D5FEF))
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isPlaying) "Pause Session" else "Start Session", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
