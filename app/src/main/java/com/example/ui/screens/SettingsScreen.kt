package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.ui.theme.AppThemeOption
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
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.theme.DarkAccentLight
import com.example.ui.theme.DarkError
import com.example.ui.theme.DarkSuccess
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val lowBattThreshold by viewModel.settingsRepository.lowBatteryWarningThreshold.collectAsState(initial = 20)
    var showClearDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var importInputText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .testTag("settings_screen")
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
                text = "App Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Battery Threshold Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E2024))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Low Battery Warning Alert", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("$lowBattThreshold%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkAccentLight)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Show on-screen warning when earbud battery drops below this level", fontSize = 11.sp, color = Color(0xFF8E9199))
                Spacer(modifier = Modifier.height(8.dp))
                Slider(
                    value = lowBattThreshold.toFloat(),
                    onValueChange = {
                        val intVal = it.toInt()
                        // update threshold
                    },
                    valueRange = 10f..40f,
                    colors = SliderDefaults.colors(thumbColor = DarkAccentLight, activeTrackColor = Color(0xFF5D5FEF))
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Theme Selector Card
        val activeTheme by viewModel.activeTheme.collectAsState()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E2024))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = DarkAccentLight, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Visual Palette & Theme", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Select your preferred visual style across all screens", fontSize = 11.sp, color = Color(0xFF8E9199))
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (themeOption in AppThemeOption.values()) {
                        val isSelected = themeOption == activeTheme
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF2A2D38) else Color(0xFF16181D))
                                .border(
                                    1.dp,
                                    if (isSelected) DarkAccentLight else Color(0xFF2C2F36),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.setTheme(themeOption) }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = themeOption.displayName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFFD1D5DB)
                                    )
                                    Text(
                                        text = themeOption.description,
                                        fontSize = 10.sp,
                                        color = Color(0xFF8E9199)
                                    )
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(DarkAccentLight)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 10-Band EQ Profile Portability Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E2024))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, tint = DarkAccentLight, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("10-Band EQ Portability", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Export your custom 10-band presets to JSON or import tuning curves.", fontSize = 11.sp, color = Color(0xFF8E9199))
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val json = viewModel.exportCustomProfilesJson()
                            clipboardManager.setText(AnnotatedString(json))
                            viewModel.showNotification("EQ profiles JSON copied to clipboard")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3240))
                    ) {
                        Text("Export JSON", fontSize = 12.sp, color = DarkAccentLight, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { showImportDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E3240))
                    ) {
                        Text("Import JSON", fontSize = 12.sp, color = DarkAccentLight, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Privacy Policy & Permissions Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E2024))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = DarkSuccess, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Offline Privacy & Permissions", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Zero internet permission requested in AndroidManifest\n• All EQ presets and gesture preferences are stored purely in on-device DataStore\n• Microphone access is gated strictly behind explicit user session start for Hearing Assist\n• Independent companion app, not affiliated with or endorsed by Ronin",
                    fontSize = 11.sp,
                    color = Color(0xFFB0B3BA),
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Clear Local Data Button
        Button(
            onClick = { showClearDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("clear_local_data_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2022))
        ) {
            Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null, tint = DarkError, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear Local App Preferences", color = DarkError, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Local Data?", color = Color.White) },
            text = {
                Text(
                    "This will reset all locally stored EQ profiles, touch preferences, and device aliases on this phone. It does not alter your physical earbuds.",
                    color = Color(0xFFB0B3BA),
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearDialog = false
                        viewModel.clearLocalData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkError)
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = Color(0xFFB0B3BA))
                }
            },
            containerColor = Color(0xFF1E2024)
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import 10-Band EQ JSON", color = Color.White) },
            text = {
                Column {
                    Text(
                        "Paste a valid 10-band EQ JSON profile array below to add to your custom profiles:",
                        color = Color(0xFFB0B3BA),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importInputText,
                        onValueChange = { importInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        placeholder = { Text("[{\"name\": \"My Preset\", \"bands\": [...]}]", color = Color(0xFF6B7280), fontSize = 11.sp) }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ok = viewModel.importCustomProfilesJson(importInputText)
                        if (ok) {
                            showImportDialog = false
                            importInputText = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D5FEF))
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = Color(0xFFB0B3BA))
                }
            },
            containerColor = Color(0xFF1E2024)
        )
    }
}
