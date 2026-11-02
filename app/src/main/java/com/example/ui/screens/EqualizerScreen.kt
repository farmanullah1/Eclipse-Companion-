package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.EqBand
import com.example.domain.EqPresetCategory
import com.example.domain.EqProfile
import com.example.ui.theme.DarkAccent
import com.example.ui.theme.DarkAccentLight
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 10-Band Equalizer Screen with real-time spectrum, audio enhancement controls,
 * A/B bypass, preset import/export, and landscape studio mixer.
 */
@Composable
fun EqualizerScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeProfile by viewModel.activeEqProfile.collectAsState()
    val savedProfiles by viewModel.savedEqProfiles.collectAsState()
    val isAbBypassActive by viewModel.isAbBypassActive.collectAsState()
    val bassBoostEnabled by viewModel.bassBoostEnabled.collectAsState()
    val bassBoostLevel by viewModel.bassBoostLevel.collectAsState()
    val virtualizerEnabled by viewModel.virtualizerEnabled.collectAsState()
    val virtualizerLevel by viewModel.virtualizerLevel.collectAsState()

    var showHearingWizard by remember { mutableStateOf(false) }

    EqualizerScreenContent(
        activeProfile = activeProfile,
        savedProfiles = savedProfiles,
        isAbBypassActive = isAbBypassActive,
        bassBoostEnabled = bassBoostEnabled,
        bassBoostLevel = bassBoostLevel,
        virtualizerEnabled = virtualizerEnabled,
        virtualizerLevel = virtualizerLevel,
        onBandGainChanged = { centerHz, gain ->
            viewModel.updateActiveEqBand(centerHz, gain)
        },
        onResetFlat = {
            viewModel.resetActiveEqProfile()
        },
        onToggleAbBypass = {
            viewModel.toggleAbBypass()
        },
        onSetBassBoost = { enabled, level ->
            viewModel.setBassBoost(enabled, level)
        },
        onSetVirtualizer = { enabled, level ->
            viewModel.setVirtualizer(enabled, level)
        },
        onSelectProfile = { profile ->
            viewModel.selectEqProfile(profile)
        },
        onSaveCustomProfile = { name ->
            viewModel.saveCustomEqProfile(name)
        },
        onExportPresets = {
            viewModel.exportCustomProfilesJson()
        },
        onImportPresets = { json ->
            viewModel.importCustomProfilesJson(json)
        },
        onOpenHearingWizard = {
            showHearingWizard = true
        },
        onBack = {
            viewModel.navigateTo(AppScreen.HOME)
        },
        modifier = modifier
    )

    if (showHearingWizard) {
        HearingCalibrationDialog(
            onDismiss = { showHearingWizard = false },
            onPlayTone = { freq, amp -> viewModel.playHearingTone(freq, amp) },
            onStopTone = { viewModel.stopHearingTone() },
            onApplyCompensation = { results -> viewModel.applyHearingTestCompensation(results) }
        )
    }
}

/**
 * State-hoisted Equalizer screen content supporting both Portrait and Landscape Studio Mixer.
 */
@Composable
fun EqualizerScreenContent(
    activeProfile: EqProfile,
    savedProfiles: List<EqProfile> = emptyList(),
    isAbBypassActive: Boolean = false,
    bassBoostEnabled: Boolean = false,
    bassBoostLevel: Int = 50,
    virtualizerEnabled: Boolean = false,
    virtualizerLevel: Int = 50,
    onBandGainChanged: (centerHz: Int, gainDb: Float) -> Unit,
    onResetFlat: () -> Unit,
    onToggleAbBypass: () -> Unit,
    onSetBassBoost: (Boolean, Int) -> Unit,
    onSetVirtualizer: (Boolean, Int) -> Unit,
    onSelectProfile: ((EqProfile) -> Unit)? = null,
    onSaveCustomProfile: ((name: String) -> Unit)? = null,
    onExportPresets: (() -> String)? = null,
    onImportPresets: ((String) -> Boolean)? = null,
    onOpenHearingWizard: (() -> Unit)? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val haptic = LocalHapticFeedback.current
    val clipboardManager = LocalClipboardManager.current

    var showSaveDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var customPresetName by remember { mutableStateOf("") }
    var exportedJsonText by remember { mutableStateOf("") }
    var importJsonText by remember { mutableStateOf("") }

    // Live organic spectrum levels (10 bands) reacting to EQ gain configuration
    val spectrumLevels = remember { mutableStateListOf(0.2f, 0.25f, 0.3f, 0.35f, 0.4f, 0.38f, 0.32f, 0.28f, 0.24f, 0.2f) }

    LaunchedEffect(activeProfile.bands) {
        while (true) {
            delay(45)
            for (i in 0 until minOf(10, activeProfile.bands.size)) {
                val bandGain = activeProfile.bands[i].gainDb
                // Base level influenced by gain (-12..+12 maps to energy baseline)
                val base = ((bandGain + 12f) / 24f) * 0.45f + 0.15f
                val jitter = (Random.nextFloat() * 0.28f)
                val target = (base + jitter).coerceIn(0.08f, 0.98f)
                // Smooth damping towards target
                spectrumLevels[i] = spectrumLevels[i] * 0.6f + target * 0.4f
            }
        }
    }

    if (isLandscape) {
        // Landscape Studio Mixer Layout (Two-Pane Hardware Console View)
        Row(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF121314))
                .padding(12.dp)
                .testTag("landscape_studio_mixer")
        ) {
            // Left Pane (Visualizer & Controls)
            Column(
                modifier = Modifier
                    .weight(0.38f)
                    .fillMaxHeight()
                    .padding(end = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Bar with Back, A/B toggle & Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF222428))
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                    Text(
                        text = "STUDIO CONSOLE",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onResetFlat()
                        },
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF222428))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset Flat", tint = Color(0xFFB0B3BA))
                    }
                }

                // Spectrum & Curve Visualizer
                Card(
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C20)),
                    border = BorderStroke(1.dp, Color(0xFF2C2F36))
                ) {
                    FrequencyResponseSpline(bands = activeProfile.bands, spectrumLevels = spectrumLevels)
                }

                // A/B Comparison Chip
                ABComparePill(
                    isAbBypassActive = isAbBypassActive,
                    onToggle = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleAbBypass()
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Compact Audio Enhancements Card
                AudioEnhancementsCard(
                    bassBoostEnabled = bassBoostEnabled,
                    bassBoostLevel = bassBoostLevel,
                    virtualizerEnabled = virtualizerEnabled,
                    virtualizerLevel = virtualizerLevel,
                    onSetBassBoost = onSetBassBoost,
                    onSetVirtualizer = onSetVirtualizer,
                    compact = true
                )

                // Hearing Calibration Action
                if (onOpenHearingWizard != null) {
                    OutlinedButton(
                        onClick = onOpenHearingWizard,
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, DarkAccent)
                    ) {
                        Icon(Icons.Default.Hearing, contentDescription = null, modifier = Modifier.size(14.dp), tint = DarkAccentLight)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calibrate My Ears", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            // Right Pane (10 Hardware Faders with Peak Meters)
            Card(
                modifier = Modifier
                    .weight(0.62f)
                    .fillMaxHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2024)),
                border = BorderStroke(1.dp, Color(0xFF2C2F36))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("10-CHANNEL HARDWARE FADERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E9199))
                        Text(activeProfile.name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkAccentLight)
                    }

                    TenBandSliderLayout(
                        bands = activeProfile.bands,
                        spectrumLevels = spectrumLevels,
                        onGainChanged = onBandGainChanged,
                        isLandscape = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    PresetControlRow(
                        onSavePreset = {
                            customPresetName = "Custom Studio EQ"
                            showSaveDialog = true
                        },
                        onExport = {
                            if (onExportPresets != null) {
                                exportedJsonText = onExportPresets()
                                showExportDialog = true
                            }
                        },
                        onImport = {
                            importJsonText = ""
                            showImportDialog = true
                        }
                    )
                }
            }
        }
    } else {
        // Portrait Layout
        val scrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF121314))
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("equalizer_screen")
        ) {
            // App Bar / Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222428))
                            .testTag("eq_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(42.dp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = DarkAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "10-Band Equalizer",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = if (isAbBypassActive) "A/B: Flat 0 dB Reference" else activeProfile.name,
                        fontSize = 12.sp,
                        color = if (isAbBypassActive) Color(0xFFFBBF24) else DarkAccentLight,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onResetFlat()
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF222428))
                        .testTag("eq_reset_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset EQ to 0 dB Flat",
                        tint = Color(0xFFB0B3BA)
                    )
                }
            }

            // Real-time Frequency Response Curve & Spectrum Canvas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .testTag("eq_curve_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C20)),
                border = BorderStroke(1.dp, Color(0xFF2C2F36))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    FrequencyResponseSpline(bands = activeProfile.bands, spectrumLevels = spectrumLevels)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // A/B Comparison Bypass Row
            ABComparePill(
                isAbBypassActive = isAbBypassActive,
                onToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleAbBypass()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main 10-Band Slider Layout Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ten_band_slider_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2024)),
                border = BorderStroke(1.dp, Color(0xFF2C2F36))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with range info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "10 FREQUENCY BANDS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF8E9199),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "±12 dB • 0.5 dB Steps",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                    }

                    // 10 Vertical Sliders with peak meter bars
                    TenBandSliderLayout(
                        bands = activeProfile.bands,
                        spectrumLevels = spectrumLevels,
                        onGainChanged = onBandGainChanged,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Actions: Save, Export, Import
                    PresetControlRow(
                        onSavePreset = {
                            customPresetName = "Custom Preset"
                            showSaveDialog = true
                        },
                        onExport = {
                            if (onExportPresets != null) {
                                exportedJsonText = onExportPresets()
                                showExportDialog = true
                            }
                        },
                        onImport = {
                            importJsonText = ""
                            showImportDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Audio Enhancements Card (Bass Boost & Spatial Virtualizer)
            AudioEnhancementsCard(
                bassBoostEnabled = bassBoostEnabled,
                bassBoostLevel = bassBoostLevel,
                virtualizerEnabled = virtualizerEnabled,
                virtualizerLevel = virtualizerLevel,
                onSetBassBoost = onSetBassBoost,
                onSetVirtualizer = onSetVirtualizer,
                compact = false,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Hearing Calibration Wizard Launch Banner
            if (onOpenHearingWizard != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onOpenHearingWizard()
                        }
                        .testTag("hearing_calibration_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2024)),
                    border = BorderStroke(1.dp, DarkAccent.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(DarkAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Hearing, contentDescription = null, tint = DarkAccentLight, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Ear Calibration Wizard",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Tailor EQ to your unique hearing frequency threshold",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                        Icon(Icons.Default.Tune, contentDescription = null, tint = DarkAccentLight)
                    }
                }
            }

            // Quick Preset Selector Chips
            if (savedProfiles.isNotEmpty() && onSelectProfile != null) {
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Preset Curves",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(savedProfiles, key = { it.id }) { preset ->
                        val isSelected = preset.id == activeProfile.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSelectProfile(preset)
                            },
                            label = {
                                Text(
                                    text = preset.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DarkAccent,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF1E2024),
                                labelColor = Color(0xFFD1D5DB)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFF2C2F36),
                                selectedBorderColor = DarkAccent
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Save Preset Dialog
    if (showSaveDialog && onSaveCustomProfile != null) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "Save Custom Preset",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a name for this customized 10-band curve:",
                        color = Color(0xFFB0B3BA),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customPresetName,
                        onValueChange = { customPresetName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkAccent,
                            unfocusedBorderColor = Color(0xFF383C46),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = customPresetName.trim().ifEmpty { "My Custom EQ" }
                        onSaveCustomProfile(name)
                        customPresetName = ""
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkAccent)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = Color(0xFFB0B3BA))
                }
            },
            containerColor = Color(0xFF1E2024),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Export Presets Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = {
                Text("Export Presets (JSON)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        "Your custom EQ profiles formatted as portable JSON. You can copy this text to backup or share your curves:",
                        color = Color(0xFFB0B3BA),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF14161A))
                            .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(8.dp))
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Text(
                            text = exportedJsonText.ifEmpty { "No custom presets created yet." },
                            color = DarkAccentLight,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(exportedJsonText))
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showExportDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkAccent)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Close", color = Color(0xFFB0B3BA))
                }
            },
            containerColor = Color(0xFF1E2024),
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Import Presets Dialog
    if (showImportDialog && onImportPresets != null) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Text("Import Presets (JSON)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column {
                    Text(
                        "Paste a previously exported EQ preset JSON payload below:",
                        color = Color(0xFFB0B3BA),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DarkAccent,
                            unfocusedBorderColor = Color(0xFF383C46),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        placeholder = {
                            Text("[ { \"name\": \"Bass Monster\", ... } ]", color = Color(0xFF6B7280), fontSize = 11.sp)
                        }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val success = onImportPresets(importJsonText.trim())
                        if (success) {
                            showImportDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkAccent)
                ) {
                    Text("Import Curves")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel", color = Color(0xFFB0B3BA))
                }
            },
            containerColor = Color(0xFF1E2024),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

/**
 * A/B Comparison Toggle Component
 */
@Composable
private fun ABComparePill(
    isAbBypassActive: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { onToggle() }
            .testTag("ab_compare_pill"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAbBypassActive) Color(0xFF2E2415) else Color(0xFF1E2024)
        ),
        border = BorderStroke(
            1.dp,
            if (isAbBypassActive) Color(0xFFFBBF24) else Color(0xFF2C2F36)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isAbBypassActive) Color(0xFFFBBF24) else DarkAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isAbBypassActive) "B" else "A",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isAbBypassActive) "A/B Comparison: Reference Flat (0 dB)" else "A/B Comparison: Active EQ Curve",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isAbBypassActive) "Tap to return to custom curve" else "Tap to instantly compare against neutral sound",
                        fontSize = 10.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
            Text(
                text = if (isAbBypassActive) "FLAT" else "EQ ON",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isAbBypassActive) Color(0xFFFBBF24) else DarkAccentLight
            )
        }
    }
}

/**
 * Audio Enhancements Card (Bass Boost & Spatial Virtualizer)
 */
@Composable
private fun AudioEnhancementsCard(
    bassBoostEnabled: Boolean,
    bassBoostLevel: Int,
    virtualizerEnabled: Boolean,
    virtualizerLevel: Int,
    onSetBassBoost: (Boolean, Int) -> Unit,
    onSetVirtualizer: (Boolean, Int) -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("audio_enhancements_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2024)),
        border = BorderStroke(1.dp, Color(0xFF2C2F36))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SurroundSound, contentDescription = null, tint = DarkAccent, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACOUSTIC ENHANCEMENTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E9199),
                        letterSpacing = 1.sp
                    )
                }
            }

            // Bass Boost Row
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dynamic Bass Boost", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Deep resonant sub-bass enhancement", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                    }
                    Switch(
                        checked = bassBoostEnabled,
                        onCheckedChange = { onSetBassBoost(it, bassBoostLevel) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DarkAccent,
                            uncheckedThumbColor = Color(0xFF6B7280),
                            uncheckedTrackColor = Color(0xFF2A2D35)
                        ),
                        modifier = Modifier.testTag("bass_boost_switch")
                    )
                }

                if (bassBoostEnabled && !compact) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = bassBoostLevel.toFloat(),
                            onValueChange = { onSetBassBoost(true, it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = DarkAccentLight, activeTrackColor = DarkAccent),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$bassBoostLevel%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkAccentLight)
                    }
                }
            }

            // Spatial Virtualizer Row
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Spatial Virtualizer", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Wide soundstage 3D acoustic field", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                    }
                    Switch(
                        checked = virtualizerEnabled,
                        onCheckedChange = { onSetVirtualizer(it, virtualizerLevel) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DarkAccent,
                            uncheckedThumbColor = Color(0xFF6B7280),
                            uncheckedTrackColor = Color(0xFF2A2D35)
                        ),
                        modifier = Modifier.testTag("virtualizer_switch")
                    )
                }

                if (virtualizerEnabled && !compact) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = virtualizerLevel.toFloat(),
                            onValueChange = { onSetVirtualizer(true, it.toInt()) },
                            valueRange = 0f..100f,
                            colors = SliderDefaults.colors(thumbColor = DarkAccentLight, activeTrackColor = DarkAccent),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("$virtualizerLevel%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkAccentLight)
                    }
                }
            }
        }
    }
}

/**
 * Bottom Actions for Preset saving, exporting and importing
 */
@Composable
private fun PresetControlRow(
    onSavePreset: () -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onSavePreset,
            modifier = Modifier
                .weight(1.3f)
                .height(38.dp)
                .testTag("save_preset_button"),
            colors = ButtonDefaults.buttonColors(containerColor = DarkAccent),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Save Preset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onExport,
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .testTag("export_preset_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFF383C46)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFFB0B3BA))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Export", fontSize = 11.sp)
        }

        OutlinedButton(
            onClick = onImport,
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .testTag("import_preset_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFF383C46)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color(0xFFB0B3BA))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Import", fontSize = 11.sp)
        }
    }
}

/**
 * 10-Band Slider Layout Arranging the 10 ISO Standard Bands:
 * 32Hz, 63Hz, 125Hz, 250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz, 16kHz
 */
@Composable
fun TenBandSliderLayout(
    bands: List<EqBand>,
    spectrumLevels: List<Float> = emptyList(),
    onGainChanged: (centerHz: Int, gainDb: Float) -> Unit,
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val totalWidth = maxWidth
        val sliderWidth: Dp = (totalWidth / 10).coerceIn(28.dp, 44.dp)
        val containerHeight = if (isLandscape) 180.dp else 235.dp

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(containerHeight),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            bands.forEachIndexed { index, band ->
                val energy = if (index < spectrumLevels.size) spectrumLevels[index] else 0.2f
                EqBandVerticalSlider(
                    band = band,
                    spectrumEnergy = energy,
                    isLandscape = isLandscape,
                    onGainChanged = { newGain ->
                        onGainChanged(band.centerHz, newGain)
                    },
                    modifier = Modifier.width(sliderWidth)
                )
            }
        }
    }
}

/**
 * Individual Touch-Draggable Vertical Slider for a single EQ Band with peak meter indicator
 */
@Composable
fun EqBandVerticalSlider(
    band: EqBand,
    spectrumEnergy: Float = 0.2f,
    isLandscape: Boolean = false,
    onGainChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var componentHeightPx by remember { mutableFloatStateOf(if (isLandscape) 90f else 130f) }
    val onGainUpdated by rememberUpdatedState(onGainChanged)

    val bandDescription = "${band.displayFrequency} band"
    val gainDescription = String.format("%.1f dB", band.gainDb)

    Column(
        modifier = modifier
            .semantics {
                contentDescription = bandDescription
                stateDescription = gainDescription
                customActions = listOf(
                    CustomAccessibilityAction("Increase gain by 0.5 dB") {
                        val next = (band.gainDb + 0.5f).coerceIn(EqProfile.MIN_GAIN_DB, EqProfile.MAX_GAIN_DB)
                        onGainUpdated(next)
                        true
                    },
                    CustomAccessibilityAction("Decrease gain by 0.5 dB") {
                        val next = (band.gainDb - 0.5f).coerceIn(EqProfile.MIN_GAIN_DB, EqProfile.MAX_GAIN_DB)
                        onGainUpdated(next)
                        true
                    },
                    CustomAccessibilityAction("Reset to 0 dB") {
                        onGainUpdated(0.0f)
                        true
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Step Up Button (+0.5 dB)
        Box(
            modifier = Modifier
                .size(if (isLandscape) 18.dp else 20.dp)
                .clip(CircleShape)
                .background(Color(0xFF26282E))
                .clickable(role = Role.Button) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val next = (band.gainDb + 0.5f).coerceIn(EqProfile.MIN_GAIN_DB, EqProfile.MAX_GAIN_DB)
                    onGainUpdated(next)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase ${band.displayFrequency} gain",
                tint = Color(0xFFB0B3BA),
                modifier = Modifier.size(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(1.dp))

        // dB Gain Value Readout
        val gainText = when {
            band.gainDb > 0 -> "+${String.format("%.1f", band.gainDb)}"
            band.gainDb < 0 -> String.format("%.1f", band.gainDb)
            else -> "0.0"
        }

        Text(
            text = gainText,
            fontSize = if (isLandscape) 8.sp else 9.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                band.gainDb > 0 -> DarkAccentLight
                band.gainDb < 0 -> Color(0xFFF2A09A)
                else -> Color(0xFF8E9199)
            },
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(1.dp))

        // Vertical Track with Draggable Thumb & Peak Meter
        val trackHeightDp = if (isLandscape) 90.dp else 125.dp

        Box(
            modifier = Modifier
                .width(26.dp)
                .height(trackHeightDp)
                .onSizeChanged { componentHeightPx = it.height.toFloat() }
                .pointerInput(band.centerHz) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        val height = if (componentHeightPx > 0) componentHeightPx else 130f
                        val dbPerPx = 24.0f / height
                        val deltaDb = -dragAmount * dbPerPx
                        val newGain = (band.gainDb + deltaDb).coerceIn(EqProfile.MIN_GAIN_DB, EqProfile.MAX_GAIN_DB)
                        val rounded = Math.round(newGain * 2) / 2.0f
                        if (rounded == 0.0f && band.gainDb != 0.0f) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                        onGainUpdated(rounded)
                    }
                }
                .pointerInput(band.centerHz) {
                    detectTapGestures(
                        onDoubleTap = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onGainUpdated(0.0f)
                        },
                        onTap = { offset ->
                            val height = if (componentHeightPx > 0) componentHeightPx else 130f
                            val fraction = (offset.y / height).coerceIn(0f, 1f)
                            val clickedGain = 12.0f - (fraction * 24.0f)
                            val rounded = Math.round(clickedGain * 2) / 2.0f
                            onGainUpdated(rounded)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Track Background Line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF2A2D35))
            )

            // Dynamic peak meter glow bar inside track responding to real-time spectrum energy
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .width(3.dp)
                    .fillMaxHeight(fraction = spectrumEnergy.coerceIn(0.05f, 0.95f))
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                if (spectrumEnergy > 0.8f) Color(0xFFEF4444) else Color(0xFF00D2FF),
                                Color(0xFF5D5FEF).copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            // Center 0 dB Reference Tick
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(2.dp)
                    .background(Color(0xFF4A4E58))
            )

            // Dynamic Thumb Indicator Node
            val normalizedFraction = ((12.0f - band.gainDb) / 24.0f).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                val thumbYOffset = (trackHeightDp - 14.dp) * normalizedFraction
                Box(
                    modifier = Modifier
                        .padding(top = thumbYOffset)
                        .size(if (isLandscape) 16.dp else 18.dp)
                        .clip(CircleShape)
                        .background(if (band.gainDb != 0f) DarkAccent else Color(0xFF6B7280))
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }

        Spacer(modifier = Modifier.height(1.dp))

        // Step Down Button (-0.5 dB)
        Box(
            modifier = Modifier
                .size(if (isLandscape) 18.dp else 20.dp)
                .clip(CircleShape)
                .background(Color(0xFF26282E))
                .clickable(role = Role.Button) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val next = (band.gainDb - 0.5f).coerceIn(EqProfile.MIN_GAIN_DB, EqProfile.MAX_GAIN_DB)
                    onGainUpdated(next)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Remove,
                contentDescription = "Decrease ${band.displayFrequency} gain",
                tint = Color(0xFFB0B3BA),
                modifier = Modifier.size(10.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Center Frequency Label (32, 63, 125, etc.)
        Text(
            text = band.displayFrequency,
            fontSize = if (isLandscape) 9.sp else 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1D5DB)
        )
    }
}

/**
 * Real-time Frequency Response Spline Curve Canvas with live FFT Spectrum Bars
 */
@Composable
fun FrequencyResponseSpline(
    bands: List<EqBand>,
    spectrumLevels: List<Float> = emptyList(),
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        // Grid lines: +6dB, 0dB (baseline), -6dB
        val line6Plus = height * (6f / 24f)
        val line6Minus = height * (18f / 24f)

        drawLine(Color(0xFF26282E), Offset(0f, line6Plus), Offset(width, line6Plus), strokeWidth = 1f)
        drawLine(Color(0xFF383C46), Offset(0f, midY), Offset(width, midY), strokeWidth = 1.5f)
        drawLine(Color(0xFF26282E), Offset(0f, line6Minus), Offset(width, line6Minus), strokeWidth = 1f)

        if (bands.isEmpty()) return@Canvas

        val stepX = width / (bands.size - 1)

        // Draw animated live spectrum analyzer bars in the background
        if (spectrumLevels.isNotEmpty()) {
            val barWidth = (width / bands.size) * 0.55f
            bands.forEachIndexed { i, _ ->
                val level = if (i < spectrumLevels.size) spectrumLevels[i] else 0.2f
                val barHeight = level * (height - 12f)
                val x = (i * stepX) - (barWidth / 2f)
                val y = height - barHeight - 4f

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x5500D2FF),
                            Color(0x155D5FEF)
                        ),
                        startY = y,
                        endY = height
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }

        val points = bands.mapIndexed { index, band ->
            val normY = (12f - band.gainDb) / 24f
            val y = (normY * (height - 16f)) + 8f
            Offset(index * stepX, y)
        }

        // Build smooth cubic bezier curve
        val path = Path()
        val fillPath = Path()

        path.moveTo(points.first().x, points.first().y)
        fillPath.moveTo(points.first().x, points.first().y)

        for (i in 0 until points.size - 1) {
            val p0 = points[i]
            val p1 = points[i + 1]
            val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
            val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)
            path.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
            fillPath.cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p1.x, p1.y)
        }

        fillPath.lineTo(width, midY)
        fillPath.lineTo(0f, midY)
        fillPath.close()

        // Translucent gradient underneath curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0x335D5FEF),
                    Color(0x055D5FEF)
                )
            )
        )

        // Curve stroke
        drawPath(
            path = path,
            color = Color(0xFF5D5FEF),
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )

        // Band nodes
        points.forEach { pt ->
            drawCircle(Color(0xFF1A1C20), radius = 4f, center = pt)
            drawCircle(Color(0xFFB4B6FF), radius = 2.5f, center = pt)
        }
    }
}
