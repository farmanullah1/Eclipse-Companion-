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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Security
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
import com.example.ui.theme.DarkSuccess
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel

@Composable
fun HelpScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121314))
            .verticalScroll(scrollState)
            .padding(horizontal = 18.dp)
            .testTag("help_screen")
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
                text = "Help & Offline Manual",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        HelpCard(
            title = "Ronin Eclipse (R-7065) Hardware",
            body = "• Drivers: 10 mm Dynamic Composite Diaphragm\n• Bluetooth: v5.3 (A2DP, AVRCP, HFP, BLE)\n• Active Noise Cancellation (ANC) up to -30dB\n• Quad-microphone Environmental Noise Cancellation (ENC) for voice calls\n• Battery: 60 mAh per earbud / 700 mAh charging case\n• Color: Snow White with electroplated gold accents"
        )

        Spacer(modifier = Modifier.height(12.dp))

        HelpCard(
            title = "Understanding ANC vs ENC vs Transparency",
            body = "• ANC (Active Noise Cancellation): Generates anti-phase soundwaves to cancel out continuous low-frequency drone (traffic, jet engines, AC fans) for you, the listener.\n• ENC (Environmental Noise Cancellation): Operates on the 4 call microphones to filter out background noise so the person on your phone call hears only your voice clearly.\n• Transparency Mode: Uses external microphones to feed ambient sounds into your ears so you can hear traffic and conversations without removing your earbuds."
        )

        Spacer(modifier = Modifier.height(12.dp))

        HelpCard(
            title = "Pairing & Reconnection",
            body = "1. Place both earbuds into the charging case and ensure both charging pins connect.\n2. Open the charging case lid. The LED indicator will pulse, indicating discovery mode.\n3. In this app, tap 'Devices' > 'Add Device' to scan and pair directly.\n4. If connection fails, toggle phone Bluetooth OFF and back ON in Android Settings."
        )

        Spacer(modifier = Modifier.height(12.dp))

        HelpCard(
            title = "Why is Case Battery sometimes unavailable?",
            body = "The charging case communicates its battery telemetry to the phone only when at least one earbud is seated in the cradle with the case lid open. When the case lid is closed or earbuds are in your ears, the case enters ultra-low power standby."
        )

        Spacer(modifier = Modifier.height(12.dp))

        HelpCard(
            title = "Hearing Assist Safety Checklist",
            body = "• Position the phone on a table pointing toward the speaker.\n• Start with conservative output level (15% default).\n• If high-pitched acoustic squeal or feedback occurs, immediately tap 'Stop Assist Now' and move the phone away from your earbuds."
        )

        Spacer(modifier = Modifier.height(12.dp))

        HelpCard(
            title = "Zero-Cloud Privacy Guarantee",
            body = "This companion application runs completely offline without accounts, servers, analytics, or remote tracking. Zero microphone audio is ever saved, recorded, or transmitted."
        )

        Spacer(modifier = Modifier.height(12.dp))

        HelpCard(
            title = "What do Clean Dust and Drain Moisture do?",
            body = "They play a fixed tone from your phone through the earbuds for 30 to 60 seconds. The vibration may help move water or dust out of the speaker opening. Take the earbuds out of your ears first. It is not guaranteed and does not replace gentle cleaning."
        )

        Spacer(modifier = Modifier.height(12.dp))

        HelpCard(
            title = "Why does the mode say Off instead of Normal?",
            body = "The official app calls the no-processing mode Off. It is the same state: no noise cancelling and no transparency."
        )

        Spacer(modifier = Modifier.height(12.dp))

        HelpCard(
            title = "Why is there no case battery on Home?",
            body = "The official app shows only left and right battery. The case value appears only if a verified profile reports it."
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun HelpCard(
    title: String,
    body: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
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
                text = body,
                fontSize = 12.sp,
                color = Color(0xFFD1D5DB),
                lineHeight = 18.sp
            )
        }
    }
}
