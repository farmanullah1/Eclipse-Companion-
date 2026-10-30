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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.ui.screens.DevicesScanScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.HearingAssistScreen
import com.example.ui.screens.HelpScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RelaxationScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SoundSettingsScreen
import com.example.ui.screens.SoundToolsScreen
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
}

@Composable
fun AppBottomNavigationBar(
    currentScreen: AppScreen,
    onSelectScreen: (AppScreen) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("app_bottom_nav_bar"),
        containerColor = Color(0xFF16171A),
        tonalElevation = 0.dp
    ) {
        BottomNavItem(
            screen = AppScreen.HOME,
            icon = Icons.Default.Headphones,
            label = "Devices",
            isSelected = currentScreen == AppScreen.HOME || currentScreen == AppScreen.SCAN,
            onSelect = onSelectScreen
        )

        BottomNavItem(
            screen = AppScreen.SOUND_SETTINGS,
            icon = Icons.Default.GraphicEq,
            label = "Sound",
            isSelected = currentScreen == AppScreen.SOUND_SETTINGS,
            onSelect = onSelectScreen
        )

        BottomNavItem(
            screen = AppScreen.TOUCH_SETTINGS,
            icon = Icons.Default.TouchApp,
            label = "Controls",
            isSelected = currentScreen == AppScreen.TOUCH_SETTINGS,
            onSelect = onSelectScreen
        )

        BottomNavItem(
            screen = AppScreen.HEARING_ASSIST,
            icon = Icons.Default.Hearing,
            label = "Assist",
            isSelected = currentScreen == AppScreen.HEARING_ASSIST,
            onSelect = onSelectScreen
        )

        BottomNavItem(
            screen = AppScreen.SETTINGS,
            icon = Icons.Default.Settings,
            label = "Settings",
            isSelected = currentScreen == AppScreen.SETTINGS || currentScreen == AppScreen.HELP,
            onSelect = onSelectScreen
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.BottomNavItem(
    screen: AppScreen,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onSelect: (AppScreen) -> Unit
) {
    NavigationBarItem(
        selected = isSelected,
        onClick = { onSelect(screen) },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(20.dp)
            )
        },
        label = {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = DarkAccentLight,
            selectedTextColor = DarkAccentLight,
            unselectedIconColor = Color(0xFF7A7D85),
            unselectedTextColor = Color(0xFF7A7D85),
            indicatorColor = Color(0xFF282D42)
        )
    )
}
