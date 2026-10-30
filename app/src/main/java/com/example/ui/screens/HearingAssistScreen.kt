package com.example.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.audio.HearingAssistPhase
import com.example.audio.HearingAssistSettings
import com.example.audio.RumbleReduction
import com.example.audio.SpeechClarity
import com.example.ui.theme.DarkAccent
import com.example.ui.theme.DarkAccentLight
import com.example.ui.theme.DarkError
import com.example.ui.theme.DarkSuccess
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun HearingAssistScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val phase by viewModel.hearingAssistEngine.phase.collectAsState()
    val settings by viewModel.hearingAssistEngine.settings.collectAsState()
    val routeInfo by viewModel.audioRouteMonitor.routeState.collectAsState()

    var inputGain by remember { mutableFloatStateOf(settings.inputGain) }
    var outputGain by remember { mutableFloatStateOf(settings.outputGain) }
    var speechClarity by remember { mutableStateOf(settings.speechClarity) }
    var rumbleReduction by remember { mutableStateOf(settings.rumbleReduction) }
    var limiterEnabled by remember { mutableStateOf(settings.limiterEnabled) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startHearingAssist()
        } else {
            viewModel.showNotification("Microphone permission is required for Hearing Assist", isError = true)
        }
    }

    val isRunning = phase == HearingAssistPhase.RUNNING
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .testTag("hearing_assist_screen")
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
                text = "Hearing Assist",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Explanation Header
        Text(
            text = "Captures speech via this phone's microphone with low-latency clarity filtering and routes directly to your connected earbuds.",
            fontSize = 12.sp,
            color = Color(0xFF9EA0A6),
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Output Route Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E2024))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = if (routeInfo.isBluetoothHeadsetConnected) DarkSuccess else Color(0xFFF1C46A),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Audio Output Route", fontSize = 11.sp, color = Color(0xFF8E9199))
                        Text(
                            text = routeInfo.currentRouteName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (routeInfo.isBluetoothHeadsetConnected) DarkSuccess.copy(alpha = 0.2f) else Color(0xFF9A6700).copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (routeInfo.isBluetoothHeadsetConnected) "Optimal" else "Phone Route",
                        color = if (routeInfo.isBluetoothHeadsetConnected) DarkSuccess else Color(0xFFF1C46A),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Primary Start / Stop Button
        val buttonBgColor by animateColorAsState(
            targetValue = if (isRunning) DarkError else Color(0xFF5D5FEF),
            label = "assist_btn"
        )

        Button(
            onClick = {
                if (isRunning) {
                    viewModel.stopHearingAssist()
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(RoundedCornerShape(29.dp))
                .testTag("hearing_assist_toggle_button"),
            colors = ButtonDefaults.buttonColors(containerColor = buttonBgColor)
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isRunning) "STOP ASSIST NOW" else "START HEARING ASSIST",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Controls Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E2024))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Acoustic Tuning",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Output Level (Safe conservative start)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Output Level", fontSize = 13.sp, color = Color(0xFFD1D5DB))
                    Text("${(outputGain * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkAccentLight)
                }
                Slider(
                    value = outputGain,
                    onValueChange = {
                        outputGain = it
                        viewModel.updateHearingAssistSettings(
                            HearingAssistSettings(inputGain, outputGain, speechClarity, rumbleReduction, limiterEnabled)
                        )
                    },
                    valueRange = HearingAssistSettings.MIN_OUTPUT_GAIN..HearingAssistSettings.MAX_OUTPUT_GAIN,
                    colors = SliderDefaults.colors(
                        thumbColor = DarkAccentLight,
                        activeTrackColor = Color(0xFF5D5FEF)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input Gain
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Microphone Sensitivity", fontSize = 13.sp, color = Color(0xFFD1D5DB))
                    Text("${String.format("%.1f", inputGain)}x", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DarkAccentLight)
                }
                Slider(
                    value = inputGain,
                    onValueChange = {
                        inputGain = it
                        viewModel.updateHearingAssistSettings(
                            HearingAssistSettings(inputGain, outputGain, speechClarity, rumbleReduction, limiterEnabled)
                        )
                    },
                    valueRange = HearingAssistSettings.MIN_INPUT_GAIN..HearingAssistSettings.MAX_INPUT_GAIN,
                    colors = SliderDefaults.colors(
                        thumbColor = DarkAccentLight,
                        activeTrackColor = Color(0xFF5D5FEF)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Speech Clarity Selector
                Text("Speech Clarity Boost", fontSize = 13.sp, color = Color(0xFFD1D5DB))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SpeechClarity.entries.forEach { mode ->
                        val isSel = speechClarity == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) Color(0xFF5D5FEF) else Color(0xFF26282E))
                                .clickable {
                                    speechClarity = mode
                                    viewModel.updateHearingAssistSettings(
                                        HearingAssistSettings(inputGain, outputGain, speechClarity, rumbleReduction, limiterEnabled)
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.displayName,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else Color(0xFF8E9199)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rumble Reduction Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Low-Frequency Rumble Filter", fontSize = 13.sp, color = Color(0xFFD1D5DB))
                        Text("Filters out air conditioner / ambient hum", fontSize = 10.sp, color = Color(0xFF8E9199))
                    }
                    Switch(
                        checked = rumbleReduction != RumbleReduction.OFF,
                        onCheckedChange = { checked ->
                            rumbleReduction = if (checked) RumbleReduction.MILD else RumbleReduction.OFF
                            viewModel.updateHearingAssistSettings(
                                HearingAssistSettings(inputGain, outputGain, speechClarity, rumbleReduction, limiterEnabled)
                            )
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF5D5FEF))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Soft Peak Limiter Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Sudden Peak Limiter", fontSize = 13.sp, color = Color(0xFFD1D5DB))
                        Text("Protects hearing from clatter or loud bangs", fontSize = 10.sp, color = Color(0xFF8E9199))
                    }
                    Switch(
                        checked = limiterEnabled,
                        onCheckedChange = { checked ->
                            limiterEnabled = checked
                            viewModel.updateHearingAssistSettings(
                                HearingAssistSettings(inputGain, outputGain, speechClarity, rumbleReduction, limiterEnabled)
                            )
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF5D5FEF))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Safety & Feedback Warning Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF28241D))
                .border(1.dp, Color(0xFF5A4822), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = Color(0xFFF1C46A),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Acoustic Feedback Notice: Position the phone near the speaker and wear earbuds properly. If any high-pitched squeal occurs, tap Stop immediately and lower the volume.",
                    fontSize = 11.sp,
                    color = Color(0xFFF5E4BE),
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Privacy Guarantee Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF1E2024))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = DarkSuccess,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Zero audio is recorded or stored. Processing occurs entirely in local RAM.",
                    fontSize = 11.sp,
                    color = Color(0xFF9EA0A6)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
