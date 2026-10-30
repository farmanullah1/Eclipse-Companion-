package com.example.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.DiscoveredGattCharacteristic
import com.example.bluetooth.DiscoveredGattService
import com.example.domain.ListeningMode
import com.example.ui.theme.DarkAccentLight
import com.example.ui.theme.DarkSuccess
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiagnosticsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val connectionState by viewModel.bluetoothRepository.connectionState.collectAsState()
    val capabilities by viewModel.bluetoothRepository.capabilities.collectAsState()
    val batteryState by viewModel.bluetoothRepository.batteryState.collectAsState()
    val logs by viewModel.bluetoothRepository.diagnosticsLogs.collectAsState()
    val discoveredServices by viewModel.bluetoothRepository.discoveredServices.collectAsState()
    val routeInfo by viewModel.audioRouteMonitor.routeState.collectAsState()
    val listeningMode by viewModel.bluetoothRepository.currentListeningMode.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .padding(horizontal = 18.dp)
            .testTag("diagnostics_screen")
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
                    text = "Hardware Diagnostics",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box(modifier = Modifier.size(42.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Connection & Identity Summary Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E2024))
                    .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("DEVICE IDENTITY & STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E9199), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    DiagnosticItem("Device Name", connectionState.deviceName ?: "Not Connected")
                    DiagnosticItem("Device Address", connectionState.deviceId?.let { maskAddress(it) } ?: "None")
                    DiagnosticItem("Connection Phase", connectionState.phase.name)
                    DiagnosticItem("Active Media Audio", if (connectionState.isAudioConnected) "A2DP Active" else "Inactive")
                    DiagnosticItem("Audio Route", routeInfo.currentRouteName)
                    DiagnosticItem("Reported ANC Mode", listeningMode?.displayName ?: "Not verified / Standby")
                    DiagnosticItem("Battery Telemetry", "L: ${batteryState.left.valueOrNull()?.let { "$it%" } ?: "N/A"}, R: ${batteryState.right.valueOrNull()?.let { "$it%" } ?: "N/A"}")
                    DiagnosticItem("Case Telemetry", batteryState.case.valueOrNull()?.let { "$it%" } ?: "Unavailable (Lid closed or not in dock)")
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Hardware Command Verification Actions
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E2024))
                    .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.BugReport, contentDescription = null, tint = DarkAccentLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("HARDWARE COMMAND TEST PANEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E9199), letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Sends verified GATT packets directly to connected hardware and captures device telemetry confirmation:",
                        fontSize = 11.sp,
                        color = Color(0xFFB0B3BA)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.selectListeningMode(ListeningMode.ANC) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32353E))
                        ) {
                            Icon(imageVector = Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(14.dp), tint = DarkAccentLight)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ANC ON", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.selectListeningMode(ListeningMode.NORMAL) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32353E))
                        ) {
                            Icon(imageVector = Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ANC OFF", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.selectListeningMode(ListeningMode.TRANSPARENCY) },
                            modifier = Modifier.weight(1f).height(38.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF32353E))
                        ) {
                            Icon(imageVector = Icons.Default.SurroundSound, contentDescription = null, modifier = Modifier.size(14.dp), tint = DarkAccentLight)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ambient", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.refreshBattery() },
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Query Battery (0x2A19)", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.queryFirmwareVersion() },
                            modifier = Modifier.weight(1f).height(36.dp)
                        ) {
                            Text("Query Firmware", fontSize = 11.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Discovered GATT Services & Characteristics Inspector Tree
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E2024))
                    .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DISCOVERED GATT SERVICES (${discoveredServices.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E9199),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (discoveredServices.isNotEmpty()) "Inspected live" else "Waiting for connection",
                            fontSize = 11.sp,
                            color = if (discoveredServices.isNotEmpty()) DarkSuccess else Color(0xFFF1C46A)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (discoveredServices.isEmpty()) {
                        Text(
                            text = "Connect to your Ronin Eclipse earbuds to inspect all exposed GATT services, characteristic handles, write permissions, and notifications.",
                            fontSize = 11.sp,
                            color = Color(0xFF8E9199)
                        )
                    } else {
                        discoveredServices.forEach { service ->
                            GattServiceCard(service = service)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Protocol Capabilities Matrix Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E2024))
                    .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Text("PROTOCOL CAPABILITY CLASSIFICATION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E9199), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    CapabilityRow("Bluetooth Audio (A2DP)", isSupported = connectionState.isAudioConnected, note = "Verified standard Android audio path")
                    CapabilityRow("Battery Reporting (0x180F)", isSupported = capabilities.batteryLeft, note = "Standard Bluetooth SIG Battery service")
                    CapabilityRow("Hardware ANC / Mode Switching", isSupported = capabilities.listeningModes.isNotEmpty(), note = if (capabilities.listeningModes.isNotEmpty()) "GATT write channel active" else "Firmware control channel pending")
                    CapabilityRow("Hardware 10-Band EQ", isSupported = capabilities.hardwareEq, note = if (capabilities.hardwareEq) "Active" else "Saved locally on phone")
                    CapabilityRow("Touch Gesture Customization", isSupported = capabilities.touchMapping, note = if (capabilities.touchMapping) "Active" else "Saved locally in DataStore")
                    CapabilityRow("Hearing Assist (Phone Mic)", isSupported = true, note = "100% local phone DSP pipeline (No cloud)")
                    CapabilityRow("Relaxation Generator", isSupported = true, note = "100% local audio synthesizer")
                    CapabilityRow("Acoustic Cleaning Sweeps", isSupported = true, note = "100% local audio sweeps (15s)")
                    CapabilityRow("OTA Firmware Updates", isSupported = false, note = "Locked: Requires vendor private signing key")
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Live Diagnostic Event Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BLUETOOTH & COMMAND LOGS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8E9199),
                    letterSpacing = 1.sp
                )
                Button(
                    onClick = {
                        val report = buildString {
                            appendLine("=== RONIN ECLIPSE (R-7065) DIAGNOSTIC REPORT ===")
                            appendLine("OS: Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
                            appendLine("Device: ${connectionState.deviceName ?: "Unknown"} (${connectionState.deviceId ?: "None"})")
                            appendLine("Phase: ${connectionState.phase}")
                            appendLine("A2DP Media Audio: ${connectionState.isAudioConnected}")
                            appendLine("Reported Battery: L=${batteryState.left.valueOrNull()}%, R=${batteryState.right.valueOrNull()}%")
                            appendLine("Discovered Services: ${discoveredServices.size}")
                            discoveredServices.forEach { s ->
                                appendLine(" - Service: ${s.name} (${s.uuid})")
                                s.characteristics.forEach { c ->
                                    appendLine("   * Char: ${c.name} (${c.uuid}) [${c.propertiesText}] Val=${c.lastValueHex ?: "null"}")
                                }
                            }
                            appendLine("--- Event Logs ---")
                            logs.forEach { appendLine(it) }
                        }
                        clipboardManager.setText(AnnotatedString(report))
                        viewModel.showNotification("Diagnostic report copied to clipboard")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF26282E)),
                    contentPadding = ButtonDefaults.TextButtonContentPadding,
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = DarkAccentLight)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Report", fontSize = 11.sp, color = DarkAccentLight)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D0E10))
                    .border(1.dp, Color(0xFF222428), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                LazyColumn {
                    items(logs) { log ->
                        Text(
                            text = log,
                            color = Color(0xFFB0B3BA),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun GattServiceCard(service: DiscoveredGattService) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF26282E))
            .clickable { isExpanded = !isExpanded }
            .padding(10.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = service.name, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(text = service.uuid, fontSize = 10.sp, color = Color(0xFF9EA0A6), fontFamily = FontFamily.Monospace)
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color(0xFFB0B3BA),
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    service.characteristics.forEach { char ->
                        GattCharacteristicItem(char)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GattCharacteristicItem(char: DiscoveredGattCharacteristic) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1B1C1E))
            .padding(8.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = char.name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE5EAF0))
                Text(text = "[${char.propertiesText}]", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkAccentLight)
            }
            Text(text = char.uuid, fontSize = 9.sp, color = Color(0xFF8E9199), fontFamily = FontFamily.Monospace)

            if (char.lastValueHex != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Hex: ${char.lastValueHex} ${if (!char.lastValueAscii.isNullOrBlank()) "(\"${char.lastValueAscii}\")" else ""}",
                    fontSize = 10.sp,
                    color = DarkSuccess,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun DiagnosticItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF8E9199))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFF3F3F0))
    }
}

@Composable
private fun CapabilityRow(name: String, isSupported: Boolean, note: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = name, fontSize = 12.sp, color = Color(0xFFD1D5DB), fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isSupported) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (isSupported) DarkSuccess else Color(0xFFF1C46A),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isSupported) "Supported" else "Conditional / Local",
                    color = if (isSupported) DarkSuccess else Color(0xFFF1C46A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(text = note, fontSize = 10.sp, color = Color(0xFF8E9199))
    }
}

private fun maskAddress(addr: String): String {
    val parts = addr.split(":")
    return if (parts.size == 6) {
        "XX:XX:XX:${parts[3]}:${parts[4]}:${parts[5]}"
    } else addr
}
