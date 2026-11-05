package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.ConnectionPhase
import com.example.domain.ListeningMode
import com.example.ui.components.EarbudBatteryRow
import com.example.ui.components.EclipseArtwork
import com.example.ui.components.ListeningModeSelector
import com.example.ui.theme.DarkAccent
import com.example.ui.theme.DarkAccentLight
import com.example.ui.theme.DarkSuccess
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.bluetoothRepository.connectionState.collectAsState()
    val batteryState by viewModel.bluetoothRepository.batteryState.collectAsState()
    val listeningMode by viewModel.bluetoothRepository.currentListeningMode.collectAsState()
    val isModePending by viewModel.isModePending.collectAsState()
    val pendingMode by viewModel.pendingListeningMode.collectAsState()
    val isVirtualDemoActive by viewModel.isVirtualDemoActive.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameInput by remember { mutableStateOf("") }

    val isConnected = connectionState.phase == ConnectionPhase.READY
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .testTag("home_screen")
    ) {
        if (!isConnected) {
            // Top Bar for Devices Screen (Matching Screenshot 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(42.dp))

                Text(
                    text = "Devices",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF222428))
                            .size(42.dp)
                            .testTag("more_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color(0xFFF3F3F0),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1E2024))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add / Scan Device", color = Color.White) },
                            onClick = {
                                showMenu = false
                                viewModel.navigateTo(AppScreen.SCAN)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Diagnostics", color = Color.White) },
                            onClick = {
                                showMenu = false
                                viewModel.navigateTo(AppScreen.DIAGNOSTICS)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Centered Devices Hero (Screenshot 1)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(
                            androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(Color(0x335B5FEF), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    EclipseArtwork(height = 200.dp)
                }

                Text(
                    text = "RONiN ECLIPSE",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.White
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF818CF8))
                    )
                    Text(
                        text = "READY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA8A8B8),
                        letterSpacing = 3.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val last = connectionState.deviceId
                        if (last != null) {
                            viewModel.connectDevice(last)
                        } else {
                            viewModel.navigateTo(AppScreen.SCAN)
                        }
                    },
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(56.dp)
                        .testTag("connect_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5B5FEF),
                        contentColor = Color.White
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "CONNECT",
                            letterSpacing = 4.sp,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "|",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                TextButton(
                    onClick = { viewModel.navigateTo(AppScreen.SCAN) },
                    modifier = Modifier.testTag("add_device_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFFB4B6FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Device",
                        color = Color(0xFFB4B6FF),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        } else {
            // Connected Device Layout (Matching Screenshot 2)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.SCAN) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF222428))
                        .size(42.dp)
                        .testTag("devices_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Air,
                        contentDescription = "Devices",
                        tint = Color(0xFFF3F3F0),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "RONiN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color(0xFFF3F3F0)
                )

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF222428))
                            .size(42.dp)
                            .testTag("more_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color(0xFFF3F3F0),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1E2024))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Refresh Status", color = Color.White) },
                            onClick = {
                                showMenu = false
                                viewModel.refreshBattery()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Diagnostics", color = Color.White) },
                            onClick = {
                                showMenu = false
                                viewModel.navigateTo(AppScreen.DIAGNOSTICS)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Disconnect", color = Color(0xFFF2A09A)) },
                            onClick = {
                                showMenu = false
                                viewModel.disconnectDevice()
                            }
                        )
                    }
                }
            }

            // Header Section with Device Title in Accent, Battery pills, and Earbuds Artwork
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1.1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = connectionState.deviceName ?: "RONiN ECLIPSE",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB4B6FF)
                        )
                        if (isVirtualDemoActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFD97706).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFFFBBF24), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "TEST MODE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    EarbudBatteryRow(batteryState = batteryState)
                }

                Box(
                    modifier = Modifier
                        .weight(0.9f)
                        .height(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    EclipseArtwork(height = 130.dp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Listening Mode Selector Capsule (Noise Reduction / Off / Transparency)
            ListeningModeSelector(
                activeMode = listeningMode,
                pendingMode = pendingMode,
                onSelectMode = { viewModel.selectListeningMode(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2 Big Action Cards Side-by-Side: Touch Settings (rose) and Sound Settings (indigo)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionTile(
                    title = "Touch Settings",
                    subtitle = "Gestures & controls",
                    icon = Icons.Default.TouchApp,
                    containerColor = Color(0xFF3A2830),
                    borderColor = Color(0xFF52333E),
                    iconTint = Color(0xFFF28CA5),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.TOUCH_SETTINGS) }
                )

                ActionTile(
                    title = "Sound Settings",
                    subtitle = "EQ & personal sound",
                    icon = Icons.Default.GraphicEq,
                    containerColor = Color(0xFF2B2B4D),
                    borderColor = Color(0xFF3B3B66),
                    iconTint = Color(0xFF818CF8),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.navigateTo(AppScreen.SOUND_SETTINGS) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vertical Feature Items List (Matching Screenshot 2)
            FeatureRowItem(
                icon = Icons.Default.Radar,
                title = "Find My Earbuds",
                onClick = { viewModel.navigateTo(AppScreen.FIND_MY_EARBUDS) }
            )

            FeatureRowItem(
                icon = Icons.Default.Verified,
                title = "Ear Tip Fit Test",
                onClick = { viewModel.navigateTo(AppScreen.FIT_TEST) }
            )

            FeatureRowItem(
                icon = Icons.Default.HealthAndSafety,
                title = "Hearing Health & Sound Dose",
                onClick = { viewModel.navigateTo(AppScreen.HEARING_HEALTH) }
            )

            FeatureRowItem(
                icon = Icons.Default.SurroundSound,
                title = "Spatial Soundstage Studio",
                onClick = { viewModel.navigateTo(AppScreen.SPATIAL_AUDIO) }
            )

            FeatureRowItem(
                icon = Icons.Default.Edit,
                title = "Name Change",
                onClick = {
                    renameInput = connectionState.deviceName ?: "Ronin Eclipse"
                    showRenameDialog = true
                }
            )

            FeatureRowItem(
                icon = Icons.Default.Hearing,
                title = "Hearing Assist",
                onClick = { viewModel.navigateTo(AppScreen.HEARING_ASSIST) }
            )

            FeatureRowItem(
                icon = Icons.Default.Spa,
                title = "Relaxation",
                onClick = { viewModel.navigateTo(AppScreen.RELAXATION) }
            )

            FeatureRowItem(
                icon = Icons.Default.CleaningServices,
                title = "Clean Dust",
                onClick = { viewModel.openSoundTool(com.example.audio.ToolMode.CLEAN_DUST) }
            )

            FeatureRowItem(
                icon = Icons.Default.Opacity,
                title = "Drain Moisture",
                onClick = { viewModel.openSoundTool(com.example.audio.ToolMode.DRAIN_MOISTURE) }
            )

            FeatureRowItem(
                icon = Icons.Default.Tune,
                title = "Device Info & Diagnostics",
                onClick = { viewModel.navigateTo(AppScreen.DIAGNOSTICS) }
            )

            FeatureRowItem(
                icon = Icons.Default.HelpOutline,
                title = "Help & Offline Manual",
                onClick = { viewModel.navigateTo(AppScreen.HELP) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Earbuds", color = Color.White) },
            text = {
                Column {
                    Text(
                        "Enter a custom display alias for your Ronin Eclipse earbuds:",
                        color = Color(0xFFB0B3BA),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRenameDialog = false
                        viewModel.renameEarbuds(renameInput)
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = Color(0xFFB0B3BA))
                }
            },
            containerColor = Color(0xFF1E2024)
        )
    }
}

@Composable
private fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color = Color(0xFF1E2024),
    borderColor: Color = Color(0xFF2C2F36),
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconTint.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF292C33)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFB0B3BA),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF3F3F0)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF8E9199)
                )
            }
        }
    }
}

@Composable
private fun FeatureRowItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E2024))
            .border(1.dp, Color(0xFF2A2D35), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFFB8CBDC),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE5E7EB)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(13.dp)
            )
        }
    }
}
