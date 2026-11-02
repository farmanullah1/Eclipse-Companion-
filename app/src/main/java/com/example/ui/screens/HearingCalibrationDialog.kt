package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkAccent
import com.example.ui.theme.DarkAccentLight

/**
 * Step-by-step Interactive Hearing Calibration Wizard.
 * Plays calibrated sine tones across 6 key audiometric frequencies (250Hz, 500Hz, 1kHz, 2kHz, 4kHz, 8kHz).
 * User detects their hearing threshold, generating a personalized inverted compensation curve.
 */
@Composable
fun HearingCalibrationDialog(
    onDismiss: () -> Unit,
    onPlayTone: (freqHz: Double, amplitude: Float) -> Unit,
    onStopTone: () -> Unit,
    onApplyCompensation: (testResults: Map<Int, Float>) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val testFrequencies = remember { listOf(250, 500, 1000, 2000, 4000, 8000) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var toneAmplitude by remember { mutableFloatStateOf(0.12f) } // 0.02f to 0.40f
    var isPlayingTone by remember { mutableStateOf(false) }

    // Map of frequency to detected threshold amplitude
    val detectedThresholds = remember { mutableStateMapOf<Int, Float>() }

    val currentFrequency = testFrequencies[currentStepIndex]
    val isLastStep = currentStepIndex == testFrequencies.lastIndex

    // Ensure audio tone stops when dialog leaves composition
    DisposableEffect(Unit) {
        onDispose {
            onStopTone()
        }
    }

    AlertDialog(
        onDismissRequest = {
            onStopTone()
            onDismiss()
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DarkAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = null,
                        tint = DarkAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Ear Calibration Wizard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Frequency ${currentStepIndex + 1} of ${testFrequencies.size}",
                        color = DarkAccentLight,
                        fontSize = 12.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress Bar
                LinearProgressIndicator(
                    progress = { (currentStepIndex + 1).toFloat() / testFrequencies.size },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = DarkAccent,
                    trackColor = Color(0xFF2C2F36)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Frequency Focus Display Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF14161A)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0xFF2C2F36))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (currentFrequency >= 1000) "${currentFrequency / 1000} kHz" else "$currentFrequency Hz",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = getFrequencyDescription(currentFrequency),
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Play/Test Tone Button
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                if (isPlayingTone) {
                                    onStopTone()
                                    isPlayingTone = false
                                } else {
                                    onPlayTone(currentFrequency.toDouble(), toneAmplitude)
                                    isPlayingTone = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPlayingTone) Color(0xFFEF4444) else DarkAccent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("hearing_test_play_button")
                        ) {
                            Icon(
                                imageVector = if (isPlayingTone) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isPlayingTone) "Stop Tone" else "Play Test Tone",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tone Amplitude Threshold Adjustment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detection Level",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD1D5DB)
                    )
                    Text(
                        text = "${(toneAmplitude * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkAccentLight
                    )
                }

                Slider(
                    value = toneAmplitude,
                    onValueChange = {
                        toneAmplitude = it
                        if (isPlayingTone) {
                            onPlayTone(currentFrequency.toDouble(), it)
                        }
                    },
                    valueRange = 0.02f..0.40f,
                    colors = SliderDefaults.colors(
                        thumbColor = DarkAccentLight,
                        activeTrackColor = DarkAccent,
                        inactiveTrackColor = Color(0xFF2C2F36)
                    ),
                    modifier = Modifier.testTag("hearing_level_slider")
                )

                Text(
                    text = "Adjust until the tone is just barely audible in your earbuds, then confirm.",
                    fontSize = 11.sp,
                    color = Color(0xFF8E9199),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStopTone()
                    isPlayingTone = false
                    // Record detected threshold for this band
                    detectedThresholds[currentFrequency] = toneAmplitude

                    if (isLastStep) {
                        onApplyCompensation(detectedThresholds.toMap())
                        onDismiss()
                    } else {
                        currentStepIndex++
                        // Reset starting amplitude for next band
                        toneAmplitude = 0.12f
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarkAccent),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("hearing_next_button")
            ) {
                if (isLastStep) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply Calibration")
                } else {
                    Text("I Hear It, Next")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onStopTone()
                    onDismiss()
                }
            ) {
                Text("Cancel", color = Color(0xFF9CA3AF))
            }
        },
        containerColor = Color(0xFF1E2024),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.testTag("hearing_calibration_dialog")
    )
}

private fun getFrequencyDescription(freqHz: Int): String {
    return when (freqHz) {
        250 -> "Low bass & vocal fundamental warmth"
        500 -> "Mid-bass & human speech vowel core"
        1000 -> "Speech clarity & mid-range focus"
        2000 -> "Consonant intelligibility & presence"
        4000 -> "High speech harmonics & articulation"
        8000 -> "High frequency shimmer & air brilliance"
        else -> "Acoustic frequency band"
    }
}
