package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.Compatibility
import com.example.domain.ConnectionPhase
import com.example.ui.components.EclipseArtwork
import com.example.ui.theme.DarkAccent
import com.example.ui.theme.DarkAccentLight
import com.example.ui.theme.DarkSuccess
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun DevicesScanScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.bluetoothRepository.connectionState.collectAsState()
    val nearbyDevices by viewModel.bluetoothRepository.nearbyDevices.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.startBleScan()
        } else {
            viewModel.showNotification("Bluetooth scanning permission required to find earbuds", isError = true)
        }
    }

    val isScanning = connectionState.phase == ConnectionPhase.SCANNING
    val isConnecting = connectionState.phase == ConnectionPhase.CONNECTING ||
            connectionState.phase == ConnectionPhase.DISCOVERING_SERVICES
    val isConnected = connectionState.phase == ConnectionPhase.READY

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .padding(horizontal = 20.dp)
            .testTag("devices_scan_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Bar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.HOME) },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Devices",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.DIAGNOSTICS) },
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White
                    )
                }
            }
        }

        // Center Product Graphic (Matching Screenshot 1)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            EclipseArtwork(
                modifier = Modifier.fillMaxWidth(0.9f),
                height = 240.dp
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "RONiN ECLIPSE",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) DarkSuccess else Color(0xFF818CF8))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isConnected) "CONNECTED" else if (isConnecting) "CONNECTING..." else "READY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF9EA0A6),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Main Connect Pill Button (Screenshot 1: Glowing Blue/Purple button with "CONNECT >")
            Button(
                onClick = {
                    val target = nearbyDevices.firstOrNull { it.compatibility == Compatibility.VERIFIED_ECLIPSE }
                        ?: nearbyDevices.firstOrNull()
                    if (target != null) {
                        viewModel.connectDevice(target.id)
                    } else {
                        // Request permissions and start scan
                        requestScanPermissions(permissionLauncher)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .testTag("connect_primary_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5D5FEF)
                )
            ) {
                if (isConnecting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("CONNECTING...", fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                } else {
                    Text(
                        text = if (isConnected) "DISCONNECT" else "CONNECT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary "+ Add Device" Button
            TextButton(
                onClick = {
                    requestScanPermissions(permissionLauncher)
                },
                modifier = Modifier.testTag("add_device_button")
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = DarkAccentLight,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scanning nearby...", color = DarkAccentLight, fontSize = 14.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFFB0B3BA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Device",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB0B3BA)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dedicated Emulator Test Mode Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("emulator_test_mode_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2024)),
                border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFBBF24).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "EMULATOR TEST MODE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24),
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Running in the browser emulator? Connect a virtual Ronin Eclipse to test battery indicators, ANC listening modes, touch controls, and equalizer presets without physical hardware.",
                        fontSize = 12.sp,
                        color = Color(0xFFB0B3BA),
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            viewModel.connectVirtualDemoDevice()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("connect_virtual_device_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Connect Virtual Ronin Eclipse",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Nearby and Paired Devices List Header
        if (nearbyDevices.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "DETECTED EARBUDS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E9199),
                        letterSpacing = 1.sp
                    )
                    if (isScanning) {
                        IconButton(onClick = { viewModel.stopBleScan() }) {
                            Text("Stop", color = Color(0xFF818CF8), fontSize = 12.sp)
                        }
                    }
                }
            }

            items(nearbyDevices) { device ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E2024))
                        .border(1.dp, Color(0xFF2E313A), RoundedCornerShape(16.dp))
                        .clickable { viewModel.connectDevice(device.id) }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2A2D35)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = if (device.compatibility == Compatibility.VERIFIED_ECLIPSE) DarkSuccess else Color(0xFF818CF8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = device.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (device.isSystemPaired) "Paired Audio Device"
                                    else if (device.rssi != null) "Signal: ${device.rssi} dBm"
                                    else "Available",
                                    fontSize = 11.sp,
                                    color = Color(0xFF8E9199)
                                )
                            }
                        }

                        if (device.compatibility == Compatibility.VERIFIED_ECLIPSE) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(DarkSuccess.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Eclipse Match", color = DarkSuccess, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

private fun requestScanPermissions(
    launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN
        )
    }
    launcher.launch(perms)
}
