package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.EqBand
import com.example.domain.EqPresetCategory
import com.example.domain.EqProfile
import com.example.ui.theme.DarkAccentLight
import com.example.ui.theme.DarkError
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun SoundSettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeProfile by viewModel.activeEqProfile.collectAsState()
    val allProfiles by viewModel.savedEqProfiles.collectAsState()

    val moodPresets = allProfiles.filter { it.category == EqPresetCategory.MOOD }
    val artistPresets = allProfiles.filter { it.category == EqPresetCategory.ARTISTS }
    val customPresets = allProfiles.filter { it.category == EqPresetCategory.CUSTOM }

    var showSaveDialog by remember { mutableStateOf(false) }
    var newPresetName by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
            .testTag("sound_settings_screen")
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 10.dp),
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Audio Presets",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = activeProfile.name,
                    fontSize = 11.sp,
                    color = DarkAccentLight,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(
                onClick = { viewModel.resetActiveEqProfile() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF222428))
                    .size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset EQ to Flat",
                    tint = Color(0xFFB0B3BA)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Equalizer",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Real-Time Frequency Response Curve Visualizer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1C20))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            EqCurveGraph(bands = activeProfile.bands)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 10-Band Sliders Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E2024))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(20.dp))
                .padding(vertical = 14.dp, horizontal = 4.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Header with Zero dB guide reference line indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "INDEPENDENT FREQUENCY GAIN (32Hz – 16kHz)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8E9199),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "±12 dB Range",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6B7280)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // The 10 Frequency Sliders
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    activeProfile.bands.forEach { band ->
                        CustomVerticalEqSlider(
                            band = band,
                            onGainChanged = { newGain ->
                                viewModel.updateActiveEqBand(band.centerHz, newGain)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick Trim & Save Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { viewModel.resetActiveEqProfile() },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2D35)),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Text("Flat (0 dB)", fontSize = 11.sp, color = Color.White)
                    }

                    Button(
                        onClick = {
                            newPresetName = "Custom Preset ${customPresets.size + 1}"
                            showSaveDialog = true
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D5FEF)),
                        contentPadding = ButtonDefaults.TextButtonContentPadding
                    ) {
                        Icon(imageVector = Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Preset", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Presets Heading
        Text(
            text = "Presets",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Mood Category
        Text(
            text = "Mood",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF9EA0A6)
        )
        Spacer(modifier = Modifier.height(6.dp))
        PresetGrid(
            presets = moodPresets,
            selectedId = activeProfile.id,
            onSelect = { viewModel.selectEqProfile(it) },
            onDelete = null
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Artists Category
        Text(
            text = "Artists",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF9EA0A6)
        )
        Spacer(modifier = Modifier.height(6.dp))
        PresetGrid(
            presets = artistPresets,
            selectedId = activeProfile.id,
            onSelect = { viewModel.selectEqProfile(it) },
            onDelete = null
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Category
        Text(
            text = "Custom",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF9EA0A6)
        )
        Spacer(modifier = Modifier.height(6.dp))
        PresetGrid(
            presets = customPresets,
            selectedId = activeProfile.id,
            onSelect = { viewModel.selectEqProfile(it) },
            onDelete = { profileId ->
                viewModel.deleteCustomEqProfile(profileId)
            }
        )

        Spacer(modifier = Modifier.height(36.dp))
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Custom Preset", color = Color.White) },
            text = {
                Column {
                    Text("Enter a name for this 10-band tuning curve:", color = Color(0xFFB0B3BA), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPresetName,
                        onValueChange = { newPresetName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = newPresetName.trim().ifEmpty { "My Custom EQ" }
                        viewModel.saveCustomEqProfile(name)
                        newPresetName = ""
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D5FEF))
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = Color(0xFFB0B3BA))
                }
            },
            containerColor = Color(0xFF1E2024)
        )
    }
}

/**
 * Custom Touch-Draggable Vertical Slider for each EQ Band
 */
@Composable
private fun CustomVerticalEqSlider(
    band: EqBand,
    onGainChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var componentHeightPx by remember { mutableFloatStateOf(160f) }

    Column(
        modifier = modifier.width(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Step Up Button (+0.5 dB)
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFF26282E))
                .clickable {
                    val next = (band.gainDb + 0.5f).coerceIn(EqProfile.MIN_GAIN_DB, EqProfile.MAX_GAIN_DB)
                    onGainChanged(next)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", tint = Color(0xFFB0B3BA), modifier = Modifier.size(12.dp))
        }

        // dB Gain Value Readout
        val gainText = if (band.gainDb > 0) "+${String.format("%.1f", band.gainDb)}"
        else if (band.gainDb < 0) String.format("%.1f", band.gainDb)
        else "0.0"

        Text(
            text = gainText,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (band.gainDb > 0) DarkAccentLight else if (band.gainDb < 0) Color(0xFFF2A09A) else Color(0xFF8E9199)
        )

        // Vertical Track with Draggable Thumb
        Box(
            modifier = Modifier
                .width(28.dp)
                .height(130.dp)
                .onSizeChanged { componentHeightPx = it.height.toFloat() }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        val height = if (componentHeightPx > 0) componentHeightPx else 130f
                        // Dragging upwards decreases Y, so it should INCREASE gain
                        val dbPerPx = 24.0f / height
                        val deltaDb = -dragAmount * dbPerPx
                        val newGain = (band.gainDb + deltaDb).coerceIn(EqProfile.MIN_GAIN_DB, EqProfile.MAX_GAIN_DB)
                        // Round to nearest 0.5 dB
                        val rounded = Math.round(newGain * 2) / 2.0f
                        onGainChanged(rounded)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            // Quick reset band to 0 dB
                            onGainChanged(0.0f)
                        },
                        onTap = { offset ->
                            val height = if (componentHeightPx > 0) componentHeightPx else 130f
                            val fraction = (offset.y / height).coerceIn(0f, 1f)
                            // top (0) is +12, bottom (1) is -12
                            val clickedGain = 12.0f - (fraction * 24.0f)
                            val rounded = Math.round(clickedGain * 2) / 2.0f
                            onGainChanged(rounded)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Track Background
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF2A2D35))
            )

            // Center 0 dB Reference Tick
            Box(
                modifier = Modifier
                    .width(14.dp)
                    .height(2.dp)
                    .background(Color(0xFF4A4E58))
            )

            // Dynamic Thumb Node calculated along vertical height
            val normalizedFraction = ((12.0f - band.gainDb) / 24.0f).coerceIn(0f, 1f)
            // Offset thumb along height
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                val thumbYOffset = (122.dp * normalizedFraction)
                Box(
                    modifier = Modifier
                        .padding(top = thumbYOffset)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (band.gainDb != 0f) Color(0xFF5D5FEF) else Color(0xFF6B7280))
                        .border(2.dp, Color.White, CircleShape)
                )
            }
        }

        // Step Down Button (-0.5 dB)
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFF26282E))
                .clickable {
                    val next = (band.gainDb - 0.5f).coerceIn(EqProfile.MIN_GAIN_DB, EqProfile.MAX_GAIN_DB)
                    onGainChanged(next)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", tint = Color(0xFFB0B3BA), modifier = Modifier.size(12.dp))
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Center Frequency Label (31, 63, 125, etc.)
        Text(
            text = band.displayFrequency,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1D5DB)
        )
    }
}

/**
 * Real-time Frequency Response Spline Curve Canvas
 */
@Composable
private fun EqCurveGraph(
    bands: List<EqBand>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val midY = height / 2f

        // Draw grid lines: +6dB, 0dB, -6dB
        val line6Plus = height * (6f / 24f)
        val line6Minus = height * (18f / 24f)

        drawLine(Color(0xFF26282E), Offset(0f, line6Plus), Offset(width, line6Plus), strokeWidth = 1f)
        drawLine(Color(0xFF383C46), Offset(0f, midY), Offset(width, midY), strokeWidth = 1.5f)
        drawLine(Color(0xFF26282E), Offset(0f, line6Minus), Offset(width, line6Minus), strokeWidth = 1f)

        if (bands.isEmpty()) return@Canvas

        val stepX = width / (bands.size - 1)
        val points = bands.mapIndexed { index, band ->
            val normY = (12f - band.gainDb) / 24f
            val y = (normY * (height - 16f)) + 8f
            Offset(index * stepX, y)
        }

        // Build smooth cubic bezier spline
        val path = Path()
        val fillPath = Path()

        path.moveTo(points.first().x, points.first().y)
        fillPath.moveTo(points.first().x, height)
        fillPath.lineTo(points.first().x, points.first().y)

        for (i in 0 until points.size - 1) {
            val p0 = if (i > 0) points[i - 1] else points[i]
            val p1 = points[i]
            val p2 = points[i + 1]
            val p3 = if (i + 2 < points.size) points[i + 2] else p2

            val cp1x = p1.x + (p2.x - p0.x) * 0.25f
            val cp1y = p1.y + (p2.y - p0.y) * 0.25f
            val cp2x = p2.x - (p3.x - p1.x) * 0.25f
            val cp2y = p2.y - (p3.y - p1.y) * 0.25f

            path.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
            fillPath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
        }

        fillPath.lineTo(points.last().x, height)
        fillPath.close()

        // Gradient under curve
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x405D5FEF), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Stroke line
        drawPath(
            path = path,
            color = Color(0xFF818CF8),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw frequency node dots
        points.forEach { pt ->
            drawCircle(Color.White, radius = 3.dp.toPx(), center = pt)
        }
    }
}

@Composable
private fun PresetGrid(
    presets: List<EqProfile>,
    selectedId: String,
    onSelect: (EqProfile) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    val chunked = presets.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in chunked) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (preset in row) {
                    PresetTile(
                        profile = preset,
                        isSelected = preset.id == selectedId,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelect(preset) },
                        onDelete = if (preset.isUserEditable && onDelete != null) { { onDelete(preset.id) } } else null
                    )
                }
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PresetTile(
    profile: EqProfile,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF5D5FEF) else Color(0xFF1E2024),
        label = "preset_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color(0xFFD1D5DB),
        label = "preset_text"
    )

    Box(
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                1.dp,
                if (isSelected) Color(0xFF818CF8) else Color(0xFF2C2F36),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Wave bar graphic
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier.height(14.dp)
                ) {
                    profile.bands.forEach { band ->
                        val barHeight = ((band.gainDb + 12f) / 24f * 12f + 2f).coerceIn(2f, 14f)
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(barHeight.dp)
                                .background(if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF6B7280))
                        )
                    }
                }

                if (onDelete != null) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Preset",
                        tint = if (isSelected) Color.White else Color(0xFF8E9199),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onDelete() }
                    )
                }
            }

            Text(
                text = profile.name,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                maxLines = 1
            )
        }
    }
}
