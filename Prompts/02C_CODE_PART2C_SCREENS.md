# File 02C — App shell, Home, Connect, Sound, Controls (v3.0)

Continues file 02B (same package `com.eclipsecompanion.app.ui`). **The remaining screens are in `02D_CODE_PART2D_SCREENS.md`** (Assist, Settings, Relaxation, Explorer, Protocol, Help, Diagnostics, Privacy). Create all files from 02B, 02C and 02D.

---

### `app/src/main/java/com/eclipsecompanion/app/ui/Screens.kt`
```kotlin
package com.eclipsecompanion.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Hearing
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eclipsecompanion.app.AppViewModel
import com.eclipsecompanion.app.Host
import com.eclipsecompanion.app.bluetooth.Perms
import com.eclipsecompanion.app.domain.ListeningMode
import com.eclipsecompanion.app.domain.Phase
import com.eclipsecompanion.app.domain.Reported
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext

enum class Tab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home), SOUND("Sound", Icons.Rounded.Tune), CONTROLS("Controls", Icons.Rounded.TouchApp),
    ASSIST("Assist", Icons.Rounded.Hearing), SETTINGS("Settings", Icons.Rounded.Settings)
}

@Composable
fun EclipseApp(vm: AppViewModel, host: Host, startRoute: String? = null, onRouteConsumed: () -> Unit = {}) {
    val ready by vm.ready.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    var tabName by rememberSaveable { mutableStateOf(Tab.HOME.name) }
    var sub by rememberSaveable { mutableStateOf<String?>(null) }
    val snack = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.events.collect { snack.showSnackbar(it) } }

    // Short vibration ONLY after the earbuds confirmed a change (never for pending/unconfirmed commands).
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        vm.controller.confirmedTick.collect {
            if (s.haptics) haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
    }
    // Launcher shortcuts and a music player's "Equalizer" button land here.
    LaunchedEffect(startRoute, ready) {
        if (ready && startRoute != null) {
            when (startRoute) {
                "assist" -> { sub = null; tabName = Tab.ASSIST.name }
                "equalizer" -> { sub = null; vm.soundTab.value = "Equalizer"; tabName = Tab.SOUND.name }
                "relax" -> sub = "relax"
            }
            onRouteConsumed()
        }
    }

    val tabs = if (s.simpleMode) listOf(Tab.HOME, Tab.ASSIST, Tab.SETTINGS) else Tab.values().toList()
    val tab = tabs.firstOrNull { it.name == tabName } ?: Tab.HOME
    val wide = LocalConfiguration.current.screenWidthDp >= 600
    BackHandler(enabled = sub != null) { sub = null }

    Scaffold(
        containerColor = E.c.background,
        snackbarHost = { SnackbarHost(snack) },
        bottomBar = {
            if (!wide && sub == null && s.firstRunDone) NavigationBar(containerColor = E.c.surface) {
                tabs.forEach { t ->
                    NavigationBarItem(selected = t == tab, onClick = { tabName = t.name },
                        icon = { Icon(t.icon, contentDescription = null) }, label = { Text(t.label) })
                }
            }
        }
    ) { pad ->
        Row(Modifier.padding(pad).fillMaxSize()) {
            if (wide && sub == null && s.firstRunDone) NavigationRail(containerColor = E.c.surface) {
                tabs.forEach { t ->
                    NavigationRailItem(selected = t == tab, onClick = { tabName = t.name },
                        icon = { Icon(t.icon, contentDescription = null) }, label = { Text(t.label) })
                }
            }
            Box(Modifier.weight(1f).fillMaxSize()) {
                if (!ready) CircularProgressIndicator(Modifier.align(Alignment.Center))
                else if (!s.firstRunDone) OnboardingScreen(vm, host)
                else when (sub) {
                    "connect" -> ConnectScreen(vm, host) { sub = null }
                    "relax" -> RelaxScreen(vm) { sub = null }
                    "explorer" -> ExplorerScreen(vm, host) { sub = null }
                    "protocol" -> ProtocolScreen(vm, host) { sub = null }
                    "help" -> HelpScreen { sub = null }
                    "diagnostics" -> DiagnosticsScreen(vm, host) { sub = null }
                    "privacy" -> PrivacyScreen { sub = null }
                    "about" -> AboutScreen { sub = null }
                    else -> when (tab) {
                        Tab.HOME -> HomeScreen(vm, host, open = { sub = it }, goAssist = { tabName = Tab.ASSIST.name })
                        Tab.SOUND -> SoundScreen(vm, host, open = { sub = it })
                        Tab.CONTROLS -> ControlsScreen(vm)
                        Tab.ASSIST -> AssistScreen(vm, host)
                        Tab.SETTINGS -> SettingsScreen(vm, host, open = { sub = it })
                    }
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ Home */

fun shortReason(phase: Phase, verified: Boolean): String =
    if (phase != Phase.READY) "Not connected" else if (!verified) "Needs verified profile" else ""

@Composable
fun HomeScreen(vm: AppViewModel, host: Host, open: (String) -> Unit, goAssist: () -> Unit) {
    val c = vm.controller
    val phase by c.phase.collectAsStateWithLifecycle()
    val msg by c.message.collectAsStateWithLifecycle()
    val bat by c.battery.collectAsStateWithLifecycle()
    val mode by c.confirmedMode.collectAsStateWithLifecycle()
    val pending by c.pendingMode.collectAsStateWithLifecycle()
    val profile by c.profile.collectAsStateWithLifecycle()
    val last by c.lastResult.collectAsStateWithLifecycle()
    val name by c.connectedName.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    val ctx = LocalContext.current
    val connected = phase == Phase.READY
    val btOn by vm.system.btOn.collectAsStateWithLifecycle()
    val route by vm.system.audioRoute.collectAsStateWithLifecycle()
    val prompt by vm.modePrompt.collectAsStateWithLifecycle()
    var quick by remember { mutableStateOf(emptyList<com.eclipsecompanion.app.bluetooth.Candidate>()) }
    LaunchedEffect(phase, btOn) {
        quick = if (phase != Phase.READY && btOn && Perms.bluetoothGranted(ctx)) vm.bondedMatches() else emptyList()
    }

    ScreenColumn {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Title("Eclipse")
            ConnectionPill(
                when (phase) { Phase.READY -> "Connected"; Phase.CONNECTING, Phase.DISCOVERING -> "Connecting…"; Phase.SCANNING -> "Scanning…"; else -> "Not connected" },
                connected
            )
        }

        ECard {
            EclipseIllustration()
            Text(if (connected && name != null) name!! else s.alias, style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = E.c.textPrimary)
            Body(if (connected) "Connected to your earbuds." else "Your earbuds are not connected to this app.")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (connected) {
                    SecondaryButton("Refresh", { vm.refresh() }, Modifier.weight(1f))
                    SecondaryButton("Disconnect", { vm.disconnect() }, Modifier.weight(1f))
                } else {
                    PrimaryButton("Connect earbuds", { host.requestBluetooth { open("connect") } }, Modifier.weight(1f))
                    if (s.deviceAddress != null) SecondaryButton("Reconnect", { host.requestBluetooth { vm.reconnectSaved() } }, Modifier.weight(1f))
                }
            }
        }

        if (!Perms.bluetoothGranted(ctx)) Banner("Allow “Nearby devices” so the app can find your earbuds. Nothing is sent anywhere.", Kind.INFO)
        msg?.let { Banner(it, if (phase == Phase.ERROR) Kind.ERROR else Kind.WARN) }
        if (!btOn) Banner("Bluetooth is turned off. Turn it on in Android settings to connect.", Kind.WARN)
        // Two separate links: Android's audio connection and this app's control connection.
        Body("Android audio route: $route · App control link: ${if (connected) "connected" else "not connected"}")
        if (phase == Phase.CONNECTING || phase == Phase.DISCOVERING) {
            Body("If this takes longer than 30 seconds, cancel, put the earbuds back in the case for a few seconds, and try again.")
            SecondaryButton("Cancel connecting", { vm.cancelConnect() }, Modifier.fillMaxWidth())
        } else if (!connected) {
            quick.take(2).forEach { d ->
                ECard {
                    Section("Compatible earbuds found")
                    Body("${d.name} is already paired with this phone.")
                    PrimaryButton("Connect ${d.name}", { host.requestBluetooth { vm.connect(d) } }, Modifier.fillMaxWidth())
                }
            }
        }
        prompt?.let { m ->
            ECard {
                Section("Switch back to ${m.label}?")
                Body("Your preferred mode is ${m.label}, but the earbuds report a different mode. Nothing changes unless you tap Switch.")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SecondaryButton("Not now", { vm.dismissModePrompt() }, Modifier.weight(1f))
                    PrimaryButton("Switch", { vm.acceptModePrompt() }, Modifier.weight(1f))
                }
            }
        }

        Section("Battery")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BatteryCard("Left", bat.left, Modifier.weight(1f))
            BatteryCard("Right", bat.right, Modifier.weight(1f))
            BatteryCard("Case", bat.case, Modifier.weight(1f))
        }
        if (bat.combined is Reported.Available) BatteryCard("Earbuds (combined, standard Bluetooth value)", bat.combined)

        Section("Listening mode")
        val can = c.can("setMode")
        val reason = shortReason(phase, profile.isVerified("setMode"))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(
                Triple(ListeningMode.ANC, "Noise cancelling", Icons.Rounded.VolumeOff),
                Triple(ListeningMode.TRANSPARENCY, "Transparency", Icons.Rounded.Hearing),
                Triple(ListeningMode.NORMAL, "Normal", Icons.Rounded.VolumeUp)
            ).forEach { (m, label, icon) ->
                ModeCard(
                    label, icon, selected = (mode as? Reported.Available)?.value == m, pending = pending == m,
                    enabled = can, reason = if (can) null else reason, onClick = { vm.setMode(m) }, modifier = Modifier.weight(1f)
                )
            }
        }
        last?.let { Body(it) }

        if (!profile.isVerified("setMode") && !s.simpleMode)
            Banner("Hardware controls stay locked until you import a verified protocol profile. Hearing Assist, system equalizer and relaxation sounds already work.", Kind.INFO)
        if (!profile.isVerified("setMode") && !s.simpleMode) SecondaryButton("How to unlock hardware controls", { open("protocol") })

        if (c.can("setGaming")) {
            Section("Gaming mode")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton("Turn on", { vm.setGaming(true) }, Modifier.weight(1f))
                SecondaryButton("Turn off", { vm.setGaming(false) }, Modifier.weight(1f))
            }
        }

        if (c.can("setWearDetection") || c.can("setVoicePrompts")) {
            ECard {
                Section("Earbud features")
                if (c.can("setWearDetection")) {
                    Body("Wear detection (pause when removed)")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SecondaryButton("Turn on", { vm.setWearDetection(true) }, Modifier.weight(1f))
                        SecondaryButton("Turn off", { vm.setWearDetection(false) }, Modifier.weight(1f))
                    }
                }
                if (c.can("setVoicePrompts")) {
                    Body("Voice prompts")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SecondaryButton("Turn on", { vm.setVoicePrompts(true) }, Modifier.weight(1f))
                        SecondaryButton("Turn off", { vm.setVoicePrompts(false) }, Modifier.weight(1f))
                    }
                }
            }
        }

        ECard(onClick = goAssist) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Hearing, contentDescription = null, tint = E.c.accent)
                Column(Modifier.weight(1f)) {
                    Text("Hearing Assist", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, color = E.c.textPrimary)
                    Body("Hear your surroundings more clearly through the earbuds. Not a medical device.")
                }
            }
        }
    }
}

/* ------------------------------------------------------------------ Connect */

@Composable
fun ConnectScreen(vm: AppViewModel, host: Host, onBack: () -> Unit) {
    val scanning by vm.scanner.scanning.collectAsStateWithLifecycle()
    val results by vm.scanner.results.collectAsStateWithLifecycle()
    val err by vm.scanner.error.collectAsStateWithLifecycle()
    val phase by vm.controller.phase.collectAsStateWithLifecycle()
    val msg by vm.controller.message.collectAsStateWithLifecycle()
    var bonded by remember { mutableStateOf(vm.bonded()) }

    LaunchedEffect(Unit) { vm.startScan() }
    LaunchedEffect(phase) { if (phase == Phase.READY) onBack() }
    androidx.compose.runtime.DisposableEffect(Unit) { onDispose { vm.stopScan() } }

    SubScreen("Connect earbuds", onBack) {
        Banner("Put the earbuds in pairing mode (open the case) and keep them close to the phone.", Kind.INFO)
        err?.let { Banner(it, Kind.ERROR) }
        msg?.let { Banner(it, if (phase == Phase.ERROR) Kind.ERROR else Kind.INFO) }
        if (!Perms.bluetoothGranted(androidx.compose.ui.platform.LocalContext.current)) {
            Banner("Nearby-devices permission is off. Allow it in Android’s app settings, then come back.", Kind.WARN)
            SecondaryButton("Open app permissions", { host.openAppSettings() })
        }
        if (phase == Phase.CONNECTING || phase == Phase.DISCOVERING) {
            Body("Connecting can take up to 30 seconds. You can cancel at any time.")
            SecondaryButton("Cancel connecting", { vm.cancelConnect() })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (scanning) { CircularProgressIndicator(Modifier.padding(4.dp), strokeWidth = 2.dp); SecondaryButton("Stop scan", { vm.stopScan() }) }
            else PrimaryButton("Scan again", { host.requestBluetooth { vm.startScan(); bonded = vm.bonded() } })
            SecondaryButton("Bluetooth settings", { host.openBluetoothSettings() })
        }
        Section("Paired in Android")
        if (bonded.isEmpty()) Body("No paired devices found.")
        bonded.forEach { d -> DeviceRow(d.name, d.address, d.match.label, "Paired") { vm.connect(d) } }
        Section("Nearby (Bluetooth LE)")
        if (results.isEmpty() && !scanning) Body("Nothing found yet. Check the earbuds are out of the case or in pairing mode.")
        results.forEach { d -> DeviceRow(d.name, d.address, d.match.label, if (d.bonded) "Paired" else "Not paired") { vm.connect(d) } }
    }
}

@Composable
private fun DeviceRow(name: String, address: String, match: String, state: String, onClick: () -> Unit) {
    ECard(onClick = onClick) {
        Text(name, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, color = E.c.textPrimary)
        Body("$match · $state · ${Perms.redact(address)}")
    }
}

/* ------------------------------------------------------------------ Sound */

@Composable
fun SoundScreen(vm: AppViewModel, host: Host, open: (String) -> Unit) {
    val tab by vm.soundTab.collectAsStateWithLifecycle()
    ScreenColumn {
        Title("Sound")
        ChipRow(listOf("Modes", "Equalizer", "Effects", "Relax"), tab) { if (it == "Relax") open("relax") else vm.soundTab.value = it }
        when (tab) {
            "Modes" -> ModesTab(vm)
            "Equalizer" -> EqualizerTab(vm, host)
            else -> EffectsTab(vm)
        }
    }
}

@Composable
private fun ModesTab(vm: AppViewModel) {
    val c = vm.controller
    val phase by c.phase.collectAsStateWithLifecycle()
    val mode by c.confirmedMode.collectAsStateWithLifecycle()
    val pending by c.pendingMode.collectAsStateWithLifecycle()
    val profile by c.profile.collectAsStateWithLifecycle()
    val last by c.lastResult.collectAsStateWithLifecycle()
    val can = c.can("setMode")
    val reason = shortReason(phase, profile.isVerified("setMode"))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        listOf(
            Triple(ListeningMode.ANC, "Noise cancelling", Icons.Rounded.VolumeOff),
            Triple(ListeningMode.TRANSPARENCY, "Transparency", Icons.Rounded.Hearing),
            Triple(ListeningMode.NORMAL, "Normal", Icons.Rounded.VolumeUp)
        ).forEach { (m, label, icon) ->
            ModeCard(label, icon, (mode as? Reported.Available)?.value == m, pending == m, can, if (can) null else reason, { vm.setMode(m) }, Modifier.weight(1f))
        }
    }
    last?.let { Body(it) }
    if (c.can("setTransparencyLevel")) {
        val cfg = profile.transparency
        if (cfg != null && cfg.max > cfg.min) {
            var level by remember { mutableStateOf(cfg.min.toFloat()) }
            ECard {
                Section("Transparency level")
                Body("Level ${level.toInt()}")
                androidx.compose.material3.Slider(
                    value = level, onValueChange = { level = it }, valueRange = cfg.min.toFloat()..cfg.max.toFloat(),
                    steps = (cfg.max - cfg.min - 1).coerceAtLeast(0), onValueChangeFinished = { vm.setTransparencyLevel(level.toInt()) }
                )
            }
        }
    }
    if (c.can("setAncLevel")) {
        val cfg = profile.ancLevel
        if (cfg != null && cfg.max > cfg.min) {
            var level by remember { mutableStateOf(cfg.min.toFloat()) }
            ECard {
                Section("Noise-cancelling level")
                Body("Level ${level.toInt()} of ${cfg.max}. This is the earbuds’ own setting, not an app effect.")
                androidx.compose.material3.Slider(
                    value = level, onValueChange = { level = it }, valueRange = cfg.min.toFloat()..cfg.max.toFloat(),
                    steps = (cfg.max - cfg.min - 1).coerceAtLeast(0), onValueChangeFinished = { vm.setAncLevel(level.toInt()) }
                )
            }
        }
    }
    ECard {
        Section("What each mode does")
        Body("Noise cancelling (ANC): reduces outside sound that you hear.")
        Body("Transparency: lets outside sound in so you can hear your surroundings.")
        Body("Normal: no processing of outside sound.")
        Body("Call noise reduction (ENC) is a separate feature: it cleans up your voice for the other person and is not controlled here.")
    }
}

@Composable
private fun EqualizerTab(vm: AppViewModel, host: Host) {
    val eq by vm.eq.collectAsStateWithLifecycle()
    val s by vm.settings.collectAsStateWithLifecycle()
    val status by vm.eqStatus.collectAsStateWithLifecycle()
    val profile by vm.controller.profile.collectAsStateWithLifecycle()
    val all = vm.allEqProfiles()
    val currentName = all.firstOrNull { it.id == eq.id }?.name ?: "Edited"
    var dialog by remember { mutableStateOf<String?>(null) }
    var text by remember { mutableStateOf("") }

    Body("Current: $currentName")
    ChipRow(all.map { it.name }, all.firstOrNull { it.id == eq.id }?.name) { n -> all.firstOrNull { it.name == n }?.let { vm.selectEq(it) } }

    ECard {
        EqCurve(eq.gains)
        EqDefaultsFreqLabels(eq.gains) { i, v -> vm.setBand(i, v) }
        if (com.eclipsecompanion.app.domain.EqDefaults.peakBoostDb(eq.gains) >= 6f)
            Banner("Large boosts can cause distortion. Lower the volume or reduce the boost if it sounds harsh.", Kind.WARN)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton("Reset to Flat", { vm.resetEq() }, Modifier.weight(1f))
            SecondaryButton("Save as…", { text = ""; dialog = "save" }, Modifier.weight(1f))
        }
    }
    Body(status)
    PrimaryButton("Apply to earbuds", { vm.applyEqToEarbuds() }, Modifier.fillMaxWidth(), enabled = vm.controller.can("setEq"))
    if (!vm.controller.can("setEq")) Body("Earbud EQ is locked: ${vm.controller.reasonFor("setEq")}")
    Body("System equalizer (for supporting music players) is set on the Effects tab.")

    if (s.customEq.isNotEmpty()) {
        Section("My profiles")
        s.customEq.forEach { p ->
            ECard {
                Text(p.name, color = E.c.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SecondaryButton("Use", { vm.selectEq(p) })
                    SecondaryButton("Copy", { vm.duplicateEq(p.id) })
                    SecondaryButton("Rename", { text = p.name; dialog = "rename:${p.id}" })
                    SecondaryButton("Delete", { vm.deleteEq(p.id) })
                }
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SecondaryButton("Export profiles", { host.saveJson("eclipse-eq-profiles.json", vm.exportEqJson()) }, Modifier.weight(1f))
        SecondaryButton("Import profiles", { host.pickJson { t -> val n = vm.importEqJson(t); host.toast(if (n > 0) "Imported $n profile(s)." else "No valid profiles found.") } }, Modifier.weight(1f))
    }

    dialog?.let { d ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { dialog = null },
            title = { Text(if (d == "save") "Save profile" else "Rename profile") },
            text = { androidx.compose.material3.OutlinedTextField(text, { text = it.take(40) }, singleLine = true, label = { Text("Name") }) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    if (d == "save") vm.saveEqAs(text) else vm.renameEq(d.removePrefix("rename:"), text)
                    dialog = null
                }) { Text("Save") }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { dialog = null }) { Text("Cancel") } }
        )
    }
}

/** Ten independent band sliders. Each slider only ever changes its own band. */
@Composable
fun EqDefaultsFreqLabels(gains: List<Float>, onBand: (Int, Float) -> Unit) {
    val freqs = com.eclipsecompanion.app.domain.EqDefaults.FREQS
    freqs.forEachIndexed { i, hz ->
        val label = if (hz >= 1000) "${hz / 1000} kHz" else "$hz Hz"
        val g = gains.getOrElse(i) { 0f }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            androidx.compose.material3.Text(label, Modifier.padding(end = 4.dp).then(Modifier), color = E.c.textSecondary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
            androidx.compose.material3.IconButton(onClick = { onBand(i, g - 0.5f) }) { Icon(Icons.Rounded.Remove, contentDescription = "Lower $label") }
            androidx.compose.material3.Slider(
                value = g, onValueChange = { v -> onBand(i, (Math.round(v * 2f) / 2f)) },
                valueRange = -12f..12f, modifier = Modifier.weight(1f),
            )
            androidx.compose.material3.IconButton(onClick = { onBand(i, g + 0.5f) }) { Icon(Icons.Rounded.Add, contentDescription = "Raise $label") }
            Text("%+.1f".format(g), color = E.c.textPrimary, style = androidx.compose.material3.MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun EffectsTab(vm: AppViewModel) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val sessions by com.eclipsecompanion.app.audio.SystemEffects.activeSessions.collectAsStateWithLifecycle()
    ECard {
        SwitchRow("System equalizer", "Applies your EQ to music players that announce their audio session.", s.systemEqEnabled) {
            vm.setFx(it, s.bassBoost, s.virtualizer, s.loudnessMb)
        }
        Body("Active player sessions: $sessions. Apps that do not announce a session (some video and streaming apps) are not affected.")
        SwitchRow("Automatic headroom", "Lowers the whole curve when you boost bands, so loud music does not distort. Recommended.", s.autoHeadroom) { vm.setAutoHeadroom(it) }
    }
    ECard {
        Section("Bass boost")
        androidx.compose.material3.Slider(s.bassBoost / 1000f, { vm.setFx(s.systemEqEnabled, (it * 1000).toInt(), s.virtualizer, s.loudnessMb) }, enabled = s.systemEqEnabled)
        Section("Stereo widening")
        androidx.compose.material3.Slider(s.virtualizer / 1000f, { vm.setFx(s.systemEqEnabled, s.bassBoost, (it * 1000).toInt(), s.loudnessMb) }, enabled = s.systemEqEnabled)
        Section("Loudness gain")
        androidx.compose.material3.Slider(s.loudnessMb / 1500f, { vm.setFx(s.systemEqEnabled, s.bassBoost, s.virtualizer, (it * 1500).toInt()) }, enabled = s.systemEqEnabled)
        Body("Loudness gain is capped to a moderate level. Keep volume comfortable.")
    }
}

/* ------------------------------------------------------------------ Controls (touch) */

@Composable
fun ControlsScreen(vm: AppViewModel) {
    val draft by vm.touchDraft.collectAsStateWithLifecycle()
    val status by vm.touchStatus.collectAsStateWithLifecycle()
    val profile by vm.controller.profile.collectAsStateWithLifecycle()
    var side by rememberSaveable { mutableStateOf(com.eclipsecompanion.app.domain.Side.LEFT.name) }
    val sd = com.eclipsecompanion.app.domain.Side.valueOf(side)
    val actions = vm.touchActions()
    val canApply = vm.controller.can("setTouch")

    ScreenColumn {
        Title("Touch controls")
        ChipRow(com.eclipsecompanion.app.domain.Side.values().map { it.label }, sd.label) { l ->
            side = com.eclipsecompanion.app.domain.Side.values().first { it.label == l }.name
        }
        if (!canApply) Banner(
            "Changes are saved on this phone only. ${vm.controller.reasonFor("setTouch")}", Kind.INFO
        )
        com.eclipsecompanion.app.domain.Gesture.values().forEach { g ->
            val key = com.eclipsecompanion.app.domain.touchKey(sd, g)
            ECard {
                Section(g.label)
                var open by remember { mutableStateOf(false) }
                SecondaryButton(draft[key] ?: "Not set", { open = true }, Modifier.fillMaxWidth())
                androidx.compose.material3.DropdownMenu(open, { open = false }) {
                    actions.forEach { a ->
                        androidx.compose.material3.DropdownMenuItem(text = { Text(a) }, onClick = { vm.setTouchDraft(sd, g, a); open = false })
                    }
                }
            }
        }
        // Conflict hint: the same action on two gestures of one side
        val dup = com.eclipsecompanion.app.domain.duplicateActions(draft, sd)
        if (dup.isNotEmpty()) Banner("The same action is used on more than one gesture of this earbud: ${dup.joinToString()}.", Kind.WARN)
        status?.let { Body(it) }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton("Cancel", { vm.cancelTouch(sd) }, Modifier.weight(1f))
            SecondaryButton("Save on phone", { vm.saveTouchLocal(sd) }, Modifier.weight(1f))
        }
        var review by remember { mutableStateOf(false) }
        PrimaryButton("Review and apply to earbuds", { review = true }, Modifier.fillMaxWidth(), enabled = canApply)
        if (review) {
            val lines = com.eclipsecompanion.app.domain.Gesture.values().mapNotNull { g ->
                draft[com.eclipsecompanion.app.domain.touchKey(sd, g)]?.let { "${g.label}: $it" }
            }
            ConfirmDialog(
                "Apply to ${sd.label}?",
                if (lines.isEmpty()) "Nothing is set yet." else lines.joinToString("\n"),
                "Apply", { vm.applyTouch(sd) }, { review = false }
            )
        }
        val helpLines = actions.mapNotNull { a -> com.eclipsecompanion.app.domain.TOUCH_ACTION_HELP[a]?.let { "$a: $it" } }
        if (helpLines.isNotEmpty()) ECard { Section("What the actions do"); helpLines.forEach { Body(it) } }
    }
}
```
