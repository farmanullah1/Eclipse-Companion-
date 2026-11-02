package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkAccentLight
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun TipsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val tips = listOf(
        "Acoustic Fit & Ear Tips" to "Try each silicone ear-tip size. An airtight seal is crucial for deep bass extension and optimal active noise cancellation performance.",
        "Listening Safely" to "Maintain playback at moderate volumes. If your ears fatigue or ring, remove the earbuds and rest your hearing.",
        "Awareness Near Traffic" to "Transparency mode allows ambient sound pass-through, but stay vigilant near traffic and hazards. Do not rely exclusively on transparency mode for street safety.",
        "Cleaning & Maintenance" to "Wipe earbud contacts and nozzle mesh with a clean, dry microfiber cloth. Use the in-app Clean Dust and Drain Moisture acoustic tools to vibrate loose particles.",
        "Battery Preservation" to "Avoid leaving the earbuds or case discharged at 0% for prolonged periods. Store in a cool, dry environment away from heat and direct sunlight.",
        "Fast Pairing Tips" to "To enter pairing mode, place both earbuds in the case with the lid open. Tap 'Devices' > 'Add Device' in this companion app to quickly discover and pair."
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .testTag("tips_screen")
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
                text = "Tips & Advice",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        tips.forEach { (title, content) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1E2024))
                    .border(1.dp, Color(0xFF2C2F36), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkAccentLight
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = content,
                        fontSize = 12.sp,
                        color = Color(0xFFD1D5DB),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
