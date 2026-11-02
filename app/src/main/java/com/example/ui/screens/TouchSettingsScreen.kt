package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.EarbudSide
import com.example.domain.TouchAction
import com.example.domain.TouchGesture
import com.example.ui.theme.DarkAccentLight
import com.example.ui.theme.DarkSuccess
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchSettingsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val draftSettings by viewModel.draftTouchSettings.collectAsState()
    var selectedSide by remember { mutableStateOf(EarbudSide.LEFT) }

    val activeConfig = if (selectedSide == EarbudSide.LEFT) draftSettings.leftConfig else draftSettings.rightConfig

    // Modal Sheet State for selecting an action for a gesture
    var selectedGestureForEdit by remember { mutableStateOf<TouchGesture?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // Test Area State
    var simulatedEventText by remember { mutableStateOf<String?>(null) }
    var simulatedActionName by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .testTag("touch_settings_screen")
    ) {
        // Top Navigation Bar
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
                text = "Touch Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            IconButton(
                onClick = { viewModel.resetTouchSettingsToDefault() },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFF222428))
                    .size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Defaults",
                    tint = Color(0xFFB0B3BA)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Earbud",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "Choose a side, then map each gesture",
            fontSize = 12.sp,
            color = Color(0xFF8E9199)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Left / Right Tabs Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            EarbudSideCard(
                side = EarbudSide.LEFT,
                isSelected = selectedSide == EarbudSide.LEFT,
                modifier = Modifier.weight(1f),
                onClick = { selectedSide = EarbudSide.LEFT }
            )

            EarbudSideCard(
                side = EarbudSide.RIGHT,
                isSelected = selectedSide == EarbudSide.RIGHT,
                modifier = Modifier.weight(1f),
                onClick = { selectedSide = EarbudSide.RIGHT }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Gestures Configuration Card (Matching Screenshot 3)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E2024))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gestures",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Quick mirror shortcut
                    TextButton(
                        onClick = {
                            val otherSide = if (selectedSide == EarbudSide.LEFT) EarbudSide.RIGHT else EarbudSide.LEFT
                            val otherConfig = if (otherSide == EarbudSide.LEFT) draftSettings.leftConfig else draftSettings.rightConfig
                            TouchGesture.entries.forEach { gesture ->
                                viewModel.updateDraftTouchAction(selectedSide, gesture, otherConfig.getActionFor(gesture))
                            }
                            viewModel.showNotification("Copied mappings from ${otherSide.displayName} earbud")
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp), tint = DarkAccentLight)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mirror Other", fontSize = 11.sp, color = DarkAccentLight)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                GestureRowItem(
                    gesture = TouchGesture.ONE_TAP,
                    badgeLabel = "1x",
                    currentAction = activeConfig.oneTap,
                    onClick = { selectedGestureForEdit = TouchGesture.ONE_TAP }
                )

                Spacer(modifier = Modifier.height(10.dp))

                GestureRowItem(
                    gesture = TouchGesture.TWO_TAP,
                    badgeLabel = "2x",
                    currentAction = activeConfig.twoTap,
                    onClick = { selectedGestureForEdit = TouchGesture.TWO_TAP }
                )

                Spacer(modifier = Modifier.height(10.dp))

                GestureRowItem(
                    gesture = TouchGesture.THREE_TAP,
                    badgeLabel = "3x",
                    currentAction = activeConfig.threeTap,
                    onClick = { selectedGestureForEdit = TouchGesture.THREE_TAP }
                )

                Spacer(modifier = Modifier.height(10.dp))

                GestureRowItem(
                    gesture = TouchGesture.TAP_AND_HOLD,
                    badgeLabel = "Hold",
                    currentAction = activeConfig.tapAndHold,
                    onClick = { selectedGestureForEdit = TouchGesture.TAP_AND_HOLD }
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Interactive Gesture Test Simulator Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF191B1F))
                .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(18.dp))
                .padding(14.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = DarkAccentLight, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gesture Test Simulator", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Text("Interactive", fontSize = 10.sp, color = DarkSuccess, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap, double-tap, triple-tap, or hold inside this test pad to verify response:",
                    fontSize = 11.sp,
                    color = Color(0xFF8E9199),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Interactive Touch Target Pad
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF242730))
                        .border(1.dp, Color(0xFF3B3E4A), RoundedCornerShape(14.dp))
                        .pointerInput(selectedSide, activeConfig) {
                            detectTapGestures(
                                onTap = {
                                    simulatedEventText = "Single Tap on ${selectedSide.displayName}"
                                    simulatedActionName = activeConfig.oneTap.displayName
                                    coroutineScope.launch {
                                        delay(2500)
                                        simulatedEventText = null
                                    }
                                },
                                onDoubleTap = {
                                    simulatedEventText = "Double Tap on ${selectedSide.displayName}"
                                    simulatedActionName = activeConfig.twoTap.displayName
                                    coroutineScope.launch {
                                        delay(2500)
                                        simulatedEventText = null
                                    }
                                },
                                onLongPress = {
                                    simulatedEventText = "Tap & Hold on ${selectedSide.displayName}"
                                    simulatedActionName = activeConfig.tapAndHold.displayName
                                    coroutineScope.launch {
                                        delay(2500)
                                        simulatedEventText = null
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (simulatedEventText != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = simulatedEventText ?: "",
                                fontSize = 11.sp,
                                color = DarkAccentLight,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Action Triggered: ${simulatedActionName ?: ""}",
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    } else {
                        Text(
                            text = "Touch Here to Test (${selectedSide.displayName} Earbud)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB0B3BA)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save & Apply Button
        Button(
            onClick = { viewModel.applyTouchSettings() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .testTag("apply_touch_settings_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5D5FEF))
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Apply Configuration to Earbuds", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Modal Bottom Sheet for selecting a TouchAction
    if (selectedGestureForEdit != null) {
        val gesture = selectedGestureForEdit!!
        val currentAction = activeConfig.getActionFor(gesture)

        ModalBottomSheet(
            onDismissRequest = { selectedGestureForEdit = null },
            sheetState = sheetState,
            containerColor = Color(0xFF1E2024)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Map ${gesture.displayName} (${selectedSide.displayName} Earbud)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Select what occurs when this gesture is detected:",
                    fontSize = 12.sp,
                    color = Color(0xFF8E9199)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TouchAction.entries.forEach { action ->
                        val isSelected = action == currentAction
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF282D40) else Color(0xFF25272C))
                                .border(1.dp, if (isSelected) Color(0xFF5D5FEF) else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.updateDraftTouchAction(selectedSide, gesture, action)
                                    selectedGestureForEdit = null
                                }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E2024)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = getActionIcon(action),
                                            contentDescription = null,
                                            tint = if (isSelected) DarkAccentLight else Color(0xFFB0B3BA),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = action.displayName,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFFD1D5DB)
                                        )
                                        Text(
                                            text = action.description,
                                            fontSize = 11.sp,
                                            color = Color(0xFF8E9199)
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = DarkAccentLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun EarbudSideCard(
    side: EarbudSide,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF5D5FEF) else Color(0xFF2C2F36),
        label = "earbud_side_border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF252834) else Color(0xFF1E2024),
        label = "earbud_side_bg"
    )

    Box(
        modifier = modifier
            .height(130.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2A2D36)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Hearing,
                    contentDescription = null,
                    tint = if (isSelected) Color(0xFFE5C07B) else Color(0xFF8E9199),
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${side.displayName} Earbud",
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF8E9199)
                )
                if (isSelected) {
                    Text("Configuring", fontSize = 10.sp, color = DarkAccentLight, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun GestureRowItem(
    gesture: TouchGesture,
    badgeLabel: String,
    currentAction: TouchAction,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF26282E))
            .border(1.dp, Color(0xFF353842), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF333640)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeLabel,
                        color = DarkAccentLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = gesture.displayName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = currentAction.description,
                        fontSize = 11.sp,
                        color = Color(0xFF8E9199)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E2024))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = currentAction.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkAccentLight
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

private fun getActionIcon(action: TouchAction): ImageVector {
    return when (action) {
        TouchAction.NO_ACTION -> Icons.Default.TouchApp
        TouchAction.ANC_SWITCHING -> Icons.Default.GraphicEq
        TouchAction.PLAY_PAUSE -> Icons.Default.PlayArrow
        TouchAction.NEXT_TRACK -> Icons.Default.FastForward
        TouchAction.PREVIOUS_TRACK -> Icons.Default.FastRewind
        TouchAction.VOLUME_UP -> Icons.Default.VolumeUp
        TouchAction.VOLUME_DOWN -> Icons.Default.VolumeDown
        TouchAction.VOICE_ASSISTANT -> Icons.Default.Mic
        TouchAction.GAMING_MODE -> Icons.Default.SportsEsports
    }
}
