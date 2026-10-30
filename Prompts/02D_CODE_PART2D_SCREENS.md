# File 02D — Assist, Settings, Relaxation, Explorer, Protocol, Help, Diagnostics, Privacy (v3.0)

Continues 02C. Same package `com.eclipsecompanion.app.ui`. The welcome tour, EQ curve and About screen are in file 04 (`Extras.kt`).

### `app/src/main/java/com/eclipsecompanion/app/ui/Screens2.kt`
```kotlin
package com.eclipsecompanion.app.ui

import android.provider.Settings as AndroidSettings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eclipsecompanion.app.APP_VERSION
import com.eclipsecompanion.app.AppViewModel
import com.eclipsecompanion.app.Host
import com.eclipsecompanion.app.audio.HearingAssistHub
import com.eclipsecompanion.app.audio.RelaxSound
import com.eclipsecompanion.app.audio.RelaxationPlayer
import com.eclipsecompanion.app.domain.HearingSettings
import com.eclipsecompanion.app.domain.Reported
import com.eclipsecompanion.app.domain.SpeechClarity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TextInputDialog(title: String, initial: String, max: Int, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var t by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(title) },
        text = { OutlinedTextField(t, { t = it.take(max) }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onConfirm(t.trim()); onDismiss() }) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun ConfirmDialog(title: String, text: String, confirm: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(text) },
        confirmButton = { TextButton(onClick = { onConfirm(); onDismiss() }) { Text(confirm) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun mmss(sec: Int) = "%02d:%02d".format(sec / 60, sec % 60)

/* ------------------------------------------------------------------ Assist */

@Composable
fun AssistScreen(vm: AppViewModel, host: Host) {
    val ui by HearingAssistHub.ui.collectAsStateWithLifecycle()
    val h by vm.hearing.collectAsStateWithLifecycle()
    var toneBusy by remember { mutableStateOf(false) }
    var checklist by remember { mutableStateOf(false) }
    val hasRoute = ui.route.startsWith("Output")

    LaunchedEffect(ui.running) { while (!ui.running) { vm.refreshRoute(); delay(2000) } }

    ScreenColumn {
        Title("Hearing Assist")
        Banner(
            "Hearing Assist picks up sound with the phone’s microphone and plays it through your earbuds. It is a convenience tool, not a medical hearing aid, and does not replace professional care. Start quietly. Keep the phone away from the earbuds so they do not squeal.",
            Kind.WARN
        )
        if (checklist) AlertDialog(
            onDismissRequest = { checklist = false }, title = { Text("Before you start") },
            text = { Text("• Sit somewhere quiet.\n• Hold the phone toward the person or TV you want to hear, away from the earbuds.\n• Earbuds fitted comfortably.\n• Sound fades in very quietly. Raise the earbud volume slowly.\n• You can press Stop at any time.") },
            confirmButton = { TextButton(onClick = { checklist = false; host.requestAssist { vm.startAssist() } }) { Text("Start quietly") } },
            dismissButton = { TextButton(onClick = { checklist = false }) { Text("Cancel") } }
        )
        ECard {
            Body(ui.route)
            Body("Status: ${ui.status}${if (ui.running) " · ${mmss(ui.elapsedSec)}" else ""}")
            if (ui.running && ui.bufferMs > 0) Body("The phone’s audio buffers hold up to about ${ui.bufferMs} ms. Bluetooth adds more delay, so speech can lag slightly behind lips.")
            ui.error?.let { Banner(it, Kind.ERROR) }
            if (ui.running) PrimaryButton("Stop", { vm.stopAssist() }, Modifier.fillMaxWidth(), danger = true)
            else PrimaryButton("Start Hearing Assist", { checklist = true }, Modifier.fillMaxWidth(), enabled = hasRoute)
            if (!hasRoute && !ui.running) Body("Connect your earbuds in Android Bluetooth settings first. Playback starts quietly every time.")
            if (ui.running) LinearProgressIndicator(progress = { ui.level.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(8.dp))
        }

        Section("Presets")
        ChipRow(listOf("Conversation", "TV", "Quiet room", "Restaurant", "Outdoors"), null) { vm.assistPreset(it) }

        ECard {
            Section("Microphone sensitivity")
            Body("%.1f×".format(h.inputGain))
            Slider(h.inputGain, { v -> vm.updateHearing { it.copy(inputGain = v) } }, valueRange = HearingSettings.INPUT_MIN..HearingSettings.INPUT_MAX)
            Section("Earbud volume")
            Body("${(h.outputGain / HearingSettings.OUTPUT_MAX * 100).toInt()}% of the safe maximum")
            Slider(h.outputGain, { v -> vm.updateHearing { it.copy(outputGain = v) } }, valueRange = 0f..HearingSettings.OUTPUT_MAX)
        }

        ECard {
            Section("Speech clarity")
            ChipRow(SpeechClarity.values().map { it.label }, h.clarity.label) { l -> vm.updateHearing { it.copy(clarity = SpeechClarity.values().first { c -> c.label == l }) } }
            SwitchRow("Reduce low rumble", "Cuts wind and handling noise below 120 Hz.", h.rumbleCut) { v -> vm.updateHearing { it.copy(rumbleCut = v) } }
            SwitchRow("Safety limiter and comfort compressor", "Always on. Softens sudden loud sounds and prevents peaks.", true, enabled = false) {}
        }

        ECard {
            Section("Earbud channel test")
            Body("Plays a quiet tone in one earbud so you can check left and right.")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton("Left tone", { toneBusy = true; vm.playTone(true) { toneBusy = false } }, Modifier.weight(1f), enabled = !toneBusy && !ui.running)
                SecondaryButton("Right tone", { toneBusy = true; vm.playTone(false) { toneBusy = false } }, Modifier.weight(1f), enabled = !toneBusy && !ui.running)
            }
        }

        ECard {
            Section("Comfort tips")
            Body("Use short sessions and take breaks.")
            Body("Lower the volume if sound feels sharp or uncomfortable.")
            Body("Ending the session is always one tap: Stop, or the Stop button in the notification.")
            Body("The session also stops by itself during phone calls, if the earbuds disconnect, or if another app takes the audio.")
        }
    }
}

/* ------------------------------------------------------------------ Relaxation */

@Composable
fun RelaxScreen(vm: AppViewModel, onBack: () -> Unit) {
    val playing by vm.relax.playing.collectAsStateWithLifecycle()
    val remaining by vm.relax.remainingSec.collectAsStateWithLifecycle()
    var sound by rememberSaveable { mutableStateOf(RelaxSound.BROWN.name) }
    var minutes by rememberSaveable { mutableStateOf(30) }
    var volume by remember { mutableStateOf(0.2f) }
    val ctx = LocalContext.current
    val reduced = remember { AndroidSettings.Global.getFloat(ctx.contentResolver, AndroidSettings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f }

    SubScreen("Relaxation sounds", onBack) {
        Banner("Sounds are generated on your phone. No downloads, no internet.", Kind.INFO)
        ChipRow(RelaxSound.values().map { it.label }, RelaxSound.valueOf(sound).label) { l -> sound = RelaxSound.values().first { it.label == l }.name }
        Section("Timer")
        ChipRow(listOf("15 min", "30 min", "60 min", "No timer"), when (minutes) { 15 -> "15 min"; 30 -> "30 min"; 60 -> "60 min"; else -> "No timer" }) {
            minutes = when (it) { "15 min" -> 15; "30 min" -> 30; "60 min" -> 60; else -> 0 }
        }
        Section("Volume")
        Slider(volume, { volume = it; vm.relax.volume = it }, valueRange = 0f..RelaxationPlayer.MAX_VOLUME)
        if (playing) {
            Body(if (remaining > 0) "Time left ${mmss(remaining)}" else "Playing without a timer")
            PrimaryButton("Stop", { vm.stopRelax() }, Modifier.fillMaxWidth(), danger = true)
        } else PrimaryButton("Play", { vm.relax.volume = volume; vm.playRelax(RelaxSound.valueOf(sound), minutes) }, Modifier.fillMaxWidth())

        ECard {
            Section("Breathing pacer")
            val t = rememberInfiniteTransition(label = "breath")
            val sc by t.animateFloat(0.55f, 1f, infiniteRepeatable(tween(4500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "scale")
            val col = E.c.accent
            Canvas(Modifier.fillMaxWidth().height(160.dp)) {
                val r = size.height / 2f * (if (reduced) 0.8f else sc)
                drawCircle(col.copy(alpha = 0.25f), r, center)
                drawCircle(col, r, center, style = Stroke(width = 4f))
            }
            Body("When the circle grows, breathe in slowly. When it shrinks, breathe out.")
        }
    }
}

/* ------------------------------------------------------------------ Settings */

@Composable
fun SettingsScreen(vm: AppViewModel, host: Host, open: (String) -> Unit) {
    val s by vm.settings.collectAsStateWithLifecycle()
    val fw by vm.controller.firmware.collectAsStateWithLifecycle()
    val profile by vm.controller.profile.collectAsStateWithLifecycle()
    var dialog by remember { mutableStateOf<String?>(null) }

    ScreenColumn {
        Title("Settings")
        ECard {
            Section("Appearance")
            ChipRow(listOf("System", "Light", "Dark"), s.theme.replaceFirstChar { it.uppercase() }) { vm.setTheme(it.lowercase()) }
            SwitchRow("Simple mode", "Shows only Home, Hearing Assist and Settings. Good for family members.", s.simpleMode) { vm.setSimple(it) }
        }
        ECard {
            Section("Device")
            SettingRow("Name on this phone", s.alias, onClick = { dialog = "alias" })
            if (vm.controller.can("setName")) SettingRow("Rename earbuds", "Changes the name the earbuds broadcast", onClick = { dialog = "rename" })
            SettingRow("Firmware", when (val f = fw) { is Reported.Available -> f.value; is Reported.Unavailable -> "Not reported" })
            Body("Firmware update is not available in this app.")
            SettingRow("Protocol profile", "Unlock hardware controls", onClick = { open("protocol") })
            SettingRow("GATT explorer", "Read-only Bluetooth inspector", onClick = { open("explorer") })
        }
        ECard {
            Section("Preferences")
            Body("Preferred listening mode (only suggested after you connect; never applied silently)")
            ChipRow(listOf("None", "ANC", "Transparency", "Normal"), when (s.preferredMode) { "ANC" -> "ANC"; "TRANSPARENCY" -> "Transparency"; "NORMAL" -> "Normal"; else -> "None" }) {
                vm.setPreferredMode(when (it) { "ANC" -> "ANC"; "Transparency" -> "TRANSPARENCY"; "Normal" -> "NORMAL"; else -> null })
            }
            SwitchRow("Vibrate on confirmed changes", "A short tap, only after the earbuds confirm a change.", s.haptics) { vm.setHaptics(it) }
            Body("Listening reminder while connected")
            ChipRow(listOf("Off", "30 min", "60 min", "90 min"), when (s.listenReminderMin) { 30 -> "30 min"; 60 -> "60 min"; 90 -> "90 min"; else -> "Off" }) {
                vm.setListenReminder(when (it) { "30 min" -> 30; "60 min" -> 60; "90 min" -> 90; else -> 0 })
            }
            SettingRow("Show the welcome tour again", onClick = { vm.setFirstRunDone(false) })
        }
        ECard {
            Section("Alerts")
            Body("Low battery at ${s.lowBattery}%, critical at ${s.criticalBattery}%")
            Slider(s.lowBattery.toFloat(), { vm.setThresholds(it.toInt(), s.criticalBattery) }, valueRange = 10f..50f)
            Body("Hearing Assist break reminder every ${s.breakReminderMin} min")
            ChipRow(listOf("15", "30", "45", "60"), s.breakReminderMin.toString()) { vm.setBreakMin(it.toInt()) }
        }
        ECard {
            Section("Data")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton("Back up", { host.saveJson("eclipse-backup.json", vm.backupJson()) }, Modifier.weight(1f))
                SecondaryButton("Restore", { host.pickJson { t -> host.toast(if (vm.restoreBackup(t)) "Restored." else "Not a valid backup.") } }, Modifier.weight(1f))
            }
            SettingRow("Privacy", "What is stored on this phone", onClick = { open("privacy") })
            SettingRow("About and licences", "Version, open-source components, disclaimer", onClick = { open("about") })
            SettingRow("Android app permissions", "Review Nearby devices, microphone and notifications", onClick = { host.openAppSettings() })
            SettingRow("Help and troubleshooting", onClick = { open("help") })
            SettingRow("Diagnostics", "Copyable summary without personal data", onClick = { open("diagnostics") })
        }
        ECard {
            Section("Reset")
            SecondaryButton("Forget saved earbuds", { dialog = "forget" }, Modifier.fillMaxWidth())
            SecondaryButton("Clear app data", { dialog = "clear" }, Modifier.fillMaxWidth())
            Body("These only affect this app. They do not reset the earbuds.")
            if (vm.controller.can("factoryReset")) PrimaryButton("Reset earbuds to factory settings", { dialog = "factory" }, Modifier.fillMaxWidth(), danger = true)
        }
        Body("Eclipse Companion $APP_VERSION. Independent project, not affiliated with or endorsed by Ronin.")
    }

    when (dialog) {
        "alias" -> TextInputDialog("Name on this phone", s.alias, 30, { vm.setAlias(it) }, { dialog = null })
        "rename" -> TextInputDialog("Earbud name (1–20 plain characters)", "", 20, { vm.renameEarbuds(it) }, { dialog = null })
        "forget" -> ConfirmDialog("Forget earbuds?", "The app will stop reconnecting automatically.", "Forget", { vm.forgetDevice() }, { dialog = null })
        "clear" -> ConfirmDialog("Clear app data?", "Removes your profiles, drafts and settings from this phone.", "Clear", { vm.clearAllData() }, { dialog = null })
        "factory" -> ConfirmDialog("Reset earbuds?", "This restores the earbuds to factory settings and unpairs them. Continue?", "Reset", { vm.factoryReset() }, { dialog = null })
    }
}

/* ------------------------------------------------------------------ Explorer */

@Composable
fun ExplorerScreen(vm: AppViewModel, host: Host, onBack: () -> Unit) {
    val services by vm.controller.explorer.collectAsStateWithLifecycle()
    val listening by vm.controller.listening.collectAsStateWithLifecycle()
    val log by vm.controller.rawLog.collectAsStateWithLifecycle()
    val results = remember { mutableStateMapOf<String, String>() }
    val scope = rememberCoroutineScope()

    SubScreen("GATT explorer", onBack) {
        Banner("Read-only. This screen never writes to the earbuds. Use it to confirm the UUIDs you captured.", Kind.INFO)
        if (services.isEmpty()) Body("Connect with a Bluetooth LE profile to see services.")
        services.forEach { svc ->
            ECard {
                Text(svc.uuid, style = MaterialTheme.typography.labelMedium, color = E.c.textPrimary)
                svc.characteristics.forEach { ch ->
                    Body("${ch.uuid}  [${ch.props}]")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (ch.readable) SecondaryButton("Read", { scope.launch { results[ch.uuid] = vm.controller.explorerRead(ch.uuid) } })
                        if (ch.notifiable) SecondaryButton(if (ch.uuid in listening) "Stop listening" else "Listen", {
                            scope.launch { vm.controller.explorerListen(ch.uuid, ch.uuid !in listening) }
                        })
                    }
                    results[ch.uuid]?.let { Body(it, color = E.c.textPrimary) }
                }
            }
        }
        if (log.isNotEmpty()) {
            Section("Notifications")
            ECard { log.takeLast(40).forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = E.c.textPrimary) } }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SecondaryButton("Copy", { host.copy("notifications", log.joinToString("\n")) }, Modifier.weight(1f))
                SecondaryButton("Clear", { vm.controller.clearLog() }, Modifier.weight(1f))
            }
        }
    }
}

/* ------------------------------------------------------------------ Protocol */

private const val CAPTURE_GUIDE = """How to capture your earbuds' commands (your own device, your own official app):
1. Developer options > Enable Bluetooth HCI snoop log. Toggle Bluetooth off and on.
2. In the official app change ONE setting at a time (ANC, Transparency, Normal, each EQ preset, each touch option), waiting about 5 seconds between actions. Note the order.
3. Take a bug report and extract btsnoop_hci.log.
4. Open it in Wireshark. Filter btatt (BLE) or btrfcomm (serial). Match writes to your actions.
5. Compare payloads to find header, length, command id, parameters and checksum.
6. Fill the JSON profile. Mark a command verified:true only after you reproduced it twice and saw the earbuds react.
7. Import the profile here and reconnect."""

@Composable
fun ProtocolScreen(vm: AppViewModel, host: Host, onBack: () -> Unit) {
    val status by vm.profileStatus.collectAsStateWithLifecycle()
    val p by vm.controller.profile.collectAsStateWithLifecycle()
    SubScreen("Protocol profile", onBack) {
        Banner("The earbuds' control commands are not public, so this app never guesses them. Controls unlock only for commands you mark as verified in a profile.", Kind.INFO)
        ECard {
            Section(p.name)
            Body(status)
            Body("Transport: ${p.transport.type.uppercase()} · configured: ${if (p.transportConfigured) "yes" else "no"}")
            val v = p.commands.filterValues { it.verified }.keys
            Body("Verified commands: ${if (v.isEmpty()) "none" else v.joinToString()}")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PrimaryButton("Import profile", { host.pickJson { vm.importProfile(it) } }, Modifier.weight(1f))
            SecondaryButton("Use bundled", { vm.resetProfile() }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SecondaryButton("Save blank template", { host.saveJson("eclipse-profile-template.json", vm.templateText()) }, Modifier.weight(1f))
            SecondaryButton("Copy template", { host.copy("profile template", vm.templateText()) }, Modifier.weight(1f))
        }
        ECard {
            Section("Capture guide")
            Body(CAPTURE_GUIDE)
            SecondaryButton("Copy guide", { host.copy("capture guide", CAPTURE_GUIDE) })
        }
    }
}

/* ------------------------------------------------------------------ Help / Diagnostics / Privacy */

@Composable
fun HelpScreen(onBack: () -> Unit) {
    SubScreen("Help", onBack) {
        listOf(
            "Earbuds do not connect" to "Open the case, keep the earbuds close, and make sure Bluetooth is on. If they are paired in Android settings, tap them under “Paired in Android”. Remove and re-pair if they were used with another phone.",
            "ANC or Transparency button is greyed out" to "Hardware controls need a verified protocol profile. See Settings > Protocol profile. Until then the earbuds can still be switched with their own touch controls.",
            "Battery shows Not available" to "Some earbuds do not report the case battery or per-earbud values over a standard channel. The app never guesses. A verified profile can enable them.",
            "Hearing Assist squeals" to "Move the phone away from the earbuds, lower the Earbud volume, and turn Speech clarity down. Hearing Assist stops itself if it detects feedback.",
            "Hearing Assist for family" to "Turn on Simple mode in Settings. Then Home and Hearing Assist are the main screens. Start quietly, pick the Conversation preset, and raise the volume slowly. Take breaks.",
            "Equalizer does nothing in some apps" to "The system equalizer works with players that announce their audio session. Some streaming and video apps do not. Use Apply to earbuds when the profile supports it.",
            "About your earbuds" to "Ronin Eclipse R-7065: Bluetooth 5.3, active noise cancelling, transparency mode, quad-mic call noise reduction. Call noise reduction (ENC) only affects your voice for the other person.",
            "Factory reset of the earbuds" to "Use the reset procedure in the manual that came with the earbuds. This app only offers a reset when a verified profile supports it. Ronin support has told owners of other models to disconnect and tap both earbuds five times; that is not confirmed for the Eclipse R-7065, so check your printed manual first.",
            "The app seems stuck on connecting" to "Tap Cancel, put the earbuds in the case for a few seconds, take them out, and try again. Make sure Nearby devices permission is allowed. If the earbuds play music but the app cannot control them, the control protocol may not be verified yet (Settings > Protocol profile).",
            "What the equalizer changes" to "The earbud EQ changes the earbuds themselves, only with a verified profile. The system equalizer changes music players that announce an audio session. Neither changes phone calls, and neither works in apps that do not announce a session.",
            "Phone calls and Hearing Assist" to "Hearing Assist stops by itself when a call starts. The app never changes call microphone routing or the earbuds’ call noise reduction (ENC).",
            "Why is the case battery missing?" to "Many earbuds do not report the case level to the phone, or only when the lid is open. The app shows Not available instead of guessing.",
            "Firmware updates" to "Updating earbud firmware is not available in this app. It would need the maker’s official package and update protocol. Use the maker’s own app for updates.",
            "Reset, forget and clear are different" to "Reset earbuds changes the earbuds (only with a verified profile). Forget saved earbuds removes the saved address from this app. Clear app data erases this app’s settings. None of these is a substitute for another.",
            "Bluetooth permission" to "Android calls it Nearby devices. The app needs it to find and talk to your earbuds. It does not use your location and has no internet access."
        ).forEach { (q, a) -> ECard { Section(q); Body(a) } }
    }
}

@Composable
fun DiagnosticsScreen(vm: AppViewModel, host: Host, onBack: () -> Unit) {
    val phase by vm.controller.phase.collectAsStateWithLifecycle()
    val text = remember(phase) { vm.diagnostics() }
    SubScreen("Diagnostics", onBack) {
        Banner("Device addresses are partly hidden. Nothing here leaves your phone unless you copy and share it.", Kind.INFO)
        ECard { Text(text, style = MaterialTheme.typography.labelSmall, color = E.c.textPrimary) }
        PrimaryButton("Copy", { host.copy("diagnostics", text) }, Modifier.fillMaxWidth())
    }
}

@Composable
fun PrivacyScreen(onBack: () -> Unit) {
    SubScreen("Privacy", onBack) {
        Banner("This app has no internet permission, no account and no analytics.", Kind.OK)
        ECard {
            Section("Stored on this phone")
            Body("Theme, simple mode, name you gave the earbuds, the last earbuds you connected to, your equalizer profiles, touch settings, alert thresholds, Hearing Assist preferences (not volume), whether you finished the welcome tour, your preferred listening mode, vibration and reminder choices, and an imported protocol profile. Battery and mode readings stay in memory only and are not saved.")
            Section("Never stored or sent")
            Body("Audio, microphone data, your location, contacts or any account details. Hearing Assist processes sound live and keeps nothing.")
            Section("Clearing")
            Body("Clear app data erases everything above. It does not reset the earbuds.")
        }
    }
}
```
