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
import androidx.compose.material.icons.filled.Info
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
fun ProductsScreen(
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
            .testTag("products_screen")
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
                text = "Product Details",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Box(modifier = Modifier.size(42.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        ProductCard(
            title = "Ronin Eclipse R-7065",
            body = "• Bluetooth 5.3 wireless chipset with low-latency audio transmission\n" +
                    "• Active Noise Cancelling (ANC) with ambient Transparency Mode\n" +
                    "• Quad-microphone Environmental Noise Cancellation (ENC) for voice calls\n" +
                    "• Custom 10 mm dynamic composite diaphragm drivers\n" +
                    "• Compact magnetic charging case with visual LED level indicators"
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProductCard(
            title = "Battery & Power Specifications",
            body = "• Earbud Battery Capacity: ~60 mAh per earbud\n" +
                    "• Charging Case Battery Capacity: ~700 mAh\n" +
                    "• Charging Interface: USB-C port\n" +
                    "• Note: Case battery reporting is active while earbuds are seated in the cradle. No multipoint (two-device) connection is supported on this model."
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProductCard(
            title = "ANC vs. Transparency vs. ENC",
            body = "• Noise Reduction (ANC): Inverts outside ambient drone before it reaches your ear drums.\n" +
                    "• Transparency Mode: Amplifies surrounding environmental acoustics through external microphones.\n" +
                    "• Off Mode: Pure passive acoustic isolation with zero digital filtering.\n" +
                    "• Call ENC: Isolates speech during calls and is completely independent of ANC."
        )

        Spacer(modifier = Modifier.height(12.dp))

        ProductCard(
            title = "Care & Physical Manual",
            body = "Keep the earbuds and charging pins clean with a soft dry cloth. Refer to the printed documentation provided in the original retail box for warranty terms and customer service."
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun ProductCard(
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
