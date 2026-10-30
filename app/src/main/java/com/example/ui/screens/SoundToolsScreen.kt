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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.audio.ToolMode
import com.example.ui.theme.DarkAccentLight
import com.example.ui.theme.DarkError
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun SoundToolsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.soundToolsEngine.currentMode.collectAsState()
    val progressSec by viewModel.soundToolsEngine.progressSeconds.collectAsState()
    val totalSec by viewModel.soundToolsEngine.totalDurationSeconds.collectAsState()
    val activeTab by viewModel.selectedSoundTool.collectAsState()
    val isRunning = currentMode != ToolMode.IDLE
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .testTag("sound_tools_screen"),
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
                text = "Acoustic Maintenance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tool Mode Tab Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E2024))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activeTab == ToolMode.CLEAN_DUST) Color(0xFF5D5FEF) else Color.Transparent)
                    .clickable {
                        if (!isRunning) viewModel.selectedSoundTool.value = ToolMode.CLEAN_DUST
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CleaningServices,
                        contentDescription = null,
                        tint = if (activeTab == ToolMode.CLEAN_DUST) Color.White else Color(0xFF8E9199),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Clean Dust",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == ToolMode.CLEAN_DUST) Color.White else Color(0xFF8E9199)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (activeTab == ToolMode.DRAIN_MOISTURE) Color(0xFF5D5FEF) else Color.Transparent)
                    .clickable {
                        if (!isRunning) viewModel.selectedSoundTool.value = ToolMode.DRAIN_MOISTURE
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Opacity,
                        contentDescription = null,
                        tint = if (activeTab == ToolMode.DRAIN_MOISTURE) Color.White else Color(0xFF8E9199),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Drain Moisture",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == ToolMode.DRAIN_MOISTURE) Color.White else Color(0xFF8E9199)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Large Visual Pulse Orb
        val orbBorderColor by animateColorAsState(
            targetValue = if (isRunning) Color(0xFF5D5FEF) else Color(0xFF2C2F36),
            label = "orb_border"
        )

        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E2129))
                .border(2.dp, orbBorderColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (activeTab == ToolMode.CLEAN_DUST) Icons.Default.CleaningServices else Icons.Default.Opacity,
                    contentDescription = null,
                    tint = if (isRunning) DarkAccentLight else Color(0xFF8E9199),
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                val remSec = maxOf(0, totalSec - progressSec)
                Text(
                    text = if (isRunning) "${remSec}s remaining" else "Ready",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isRunning) {
            LinearProgressIndicator(
                progress = { (progressSec.toFloat() / maxOf(1, totalSec)).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF5D5FEF),
                trackColor = Color(0xFF26282E)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Instructions Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E2024))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = DarkAccentLight,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (activeTab == ToolMode.CLEAN_DUST) "How Clean Dust Works" else "How Drain Moisture Works",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (activeTab == ToolMode.CLEAN_DUST) {
                            "Removes dust particles by generating high-energy 350Hz–650Hz resonant acoustic sweeps through the 10mm dynamic driver diaphragm. Place earbuds speaker grille facing downward onto a soft paper tissue."
                        } else {
                            "Generates 145Hz hydraulic kinetic displacement sound pulses designed to safely expel moisture or sweat droplets from the acoustic sound canal nozzle. Shake gently afterward."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFFB0B3BA),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Start / Stop Button
        Button(
            onClick = {
                if (isRunning) {
                    viewModel.stopSoundTool()
                } else {
                    if (activeTab == ToolMode.CLEAN_DUST) {
                        viewModel.startCleanDust()
                    } else {
                        viewModel.startDrainMoisture()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(27.dp))
                .testTag("sound_tool_action_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) DarkError else Color(0xFF5D5FEF)
            )
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Stop else if (activeTab == ToolMode.CLEAN_DUST) Icons.Default.CleaningServices else Icons.Default.Opacity,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isRunning) "STOP PULSE" else if (activeTab == ToolMode.CLEAN_DUST) "START CLEAN DUST (15s)" else "START DRAIN MOISTURE (15s)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
