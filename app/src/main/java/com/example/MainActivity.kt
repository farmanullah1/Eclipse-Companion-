package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupportAgent
import com.example.ui.screens.DevicesScanScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.HearingAssistScreen
import com.example.ui.screens.HelpScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.RelaxationScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SoundSettingsScreen
import com.example.ui.screens.SoundToolsScreen
import com.example.ui.screens.TipsScreen
import com.example.ui.screens.TouchSettingsScreen
import com.example.ui.theme.DarkAccent
import com.example.ui.theme.DarkAccentLight
import com.example.ui.theme.DarkError
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                val currentScreen by viewModel.currentScreen.collectAsState()
                val notification by viewModel.notification.collectAsState()

                BackHandler(enabled = currentScreen != AppScreen.HOME) {
                    viewModel.navigateTo(AppScreen.HOME)
                }

                LaunchedEffect(notification) {
                    if (notification != null) {
                        delay(3500)
                        viewModel.dismissNotification()
                    }
                }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121314)),
                    bottomBar = {
                        AppBottomNavigationBar(
                            currentScreen = currentScreen,
                            onSelectScreen = { viewModel.navigateTo(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .statusBarsPadding()
                    ) {
                        when (currentScreen) {
                            AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                            AppScreen.SCAN -> DevicesScanScreen(viewModel = viewModel)
                            AppScreen.SOUND_SETTINGS -> SoundSettingsScreen(viewModel = viewModel)
                            AppScreen.TOUCH_SETTINGS -> TouchSettingsScreen(viewModel = viewModel)
                            AppScreen.HEARING_ASSIST -> HearingAssistScreen(viewModel = viewModel)
                            AppScreen.RELAXATION -> RelaxationScreen(viewModel = viewModel)
                            AppScreen.SOUND_TOOLS -> SoundToolsScreen(viewModel = viewModel)
                            AppScreen.DIAGNOSTICS -> DiagnosticsScreen(viewModel = viewModel)
                            AppScreen.HELP -> HelpScreen(viewModel = viewModel)
                            AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
                            AppScreen.PRODUCTS -> ProductsScreen(viewModel = viewModel)
                            AppScreen.TIPS -> TipsScreen(viewModel = viewModel)
                            AppScreen.EQUALIZER -> EqualizerScreen(viewModel = viewModel)
                        }

                        // Notification Toast / Banner
                        AnimatedVisibility(
                            visible = notification != null,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) {
                            val notif = notification
                            if (notif != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (notif.isError) Color(0xFF381E20) else Color(0xFF1E2333))
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = notif.message,
                                            color = if (notif.isError) Color(0xFFF2A09A) else Color(0xFFE5EAF0),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = { viewModel.dismissNotification() }) {
                                            Text("Dismiss", color = DarkAccentLight, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.soundToolsEngine.stop()
    }
}

@Composable
fun AppBottomNavigationBar(
    currentScreen: AppScreen,
    onSelectScreen: (AppScreen) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("app_bottom_nav_bar"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Floating Pill Bar (Matching Screenshot 1)
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(36.dp))
                .background(Color(0xFF22242B))
                .border(1.dp, Color(0xFF343744), RoundedCornerShape(36.dp))
                .padding(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PillNavItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    isSelected = currentScreen == AppScreen.HOME || currentScreen == AppScreen.SCAN,
                    onClick = { onSelectScreen(AppScreen.HOME) },
                    modifier = Modifier.weight(1f)
                )

                PillNavItem(
                    icon = Icons.Default.Lightbulb,
                    label = "News",
                    isSelected = currentScreen == AppScreen.TIPS,
                    onClick = { onSelectScreen(AppScreen.TIPS) },
                    modifier = Modifier.weight(1f)
                )

                PillNavItem(
                    icon = Icons.Default.Info,
                    label = "Products",
                    isSelected = currentScreen == AppScreen.PRODUCTS,
                    onClick = { onSelectScreen(AppScreen.PRODUCTS) },
                    modifier = Modifier.weight(1f)
                )

                PillNavItem(
                    icon = Icons.Default.Person,
                    label = "Profile",
                    isSelected = currentScreen == AppScreen.SETTINGS,
                    onClick = { onSelectScreen(AppScreen.SETTINGS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Circular Support Button (Matching Screenshot 1)
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF22242B))
                .border(1.dp, Color(0xFF343744), CircleShape)
                .clickable { onSelectScreen(AppScreen.HELP) }
                .testTag("support_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SupportAgent,
                contentDescription = "Help and support",
                tint = Color(0xFFB4B6FF),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PillNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) Color(0xFF4A4D86) else Color.Transparent
    val contentColor = if (isSelected) Color.White else Color(0xFFA8A8B8)

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}
