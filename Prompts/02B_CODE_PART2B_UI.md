# File 02B — Compose UI (theme, components, app shell) (v3.0)

Package `com.eclipsecompanion.app.ui`. Companion files: `02B` continues in **02C** (`02C_CODE_PART2C_SCREENS.md`: Home, Sound, Controls, Assist, Settings and sub-screens). Create every file in 02B, 02C, 02D and the new extras in file 04 (`Extras.kt`: EQ curve, welcome tour, About screen). Copy verbatim; fix only real compile errors and list changes in `docs/CHANGES_FROM_PROMPT.md`.

Design tokens come from master prompt section 7. Icons: only `androidx.compose.material.icons` (extended, Rounded set) plus the Canvas illustration below. Vector drawables for the launcher icon are in file 03.

---

### `app/src/main/java/com/eclipsecompanion/app/ui/Theme.kt`
```kotlin
package com.eclipsecompanion.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Immutable
class EclipseColors(
    val background: Color, val surface: Color, val surfaceSecondary: Color,
    val textPrimary: Color, val textSecondary: Color, val outline: Color,
    val accent: Color, val accentContainer: Color,
    val success: Color, val warning: Color, val error: Color, val onAccent: Color
)

val LightEclipse = EclipseColors(
    background = Color(0xFFF6F6F4), surface = Color(0xFFFFFFFF), surfaceSecondary = Color(0xFFEEEEEC),
    textPrimary = Color(0xFF181818), textSecondary = Color(0xFF666663), outline = Color(0xFFD7D7D2),
    accent = Color(0xFF344C63), accentContainer = Color(0xFFE3EAF0),
    success = Color(0xFF2D6A4F), warning = Color(0xFF9A6700), error = Color(0xFFB3261E), onAccent = Color(0xFFFFFFFF)
)

val DarkEclipse = EclipseColors(
    background = Color(0xFF121314), surface = Color(0xFF1B1C1E), surfaceSecondary = Color(0xFF25272A),
    textPrimary = Color(0xFFF3F3F0), textSecondary = Color(0xFFB7B8B5), outline = Color(0xFF424447),
    accent = Color(0xFFB8CBDC), accentContainer = Color(0xFF283743),
    success = Color(0xFF82C9A5), warning = Color(0xFFF1C46A), error = Color(0xFFF2A09A), onAccent = Color(0xFF10202D)
)

val LocalEclipse = staticCompositionLocalOf { LightEclipse }

object E {
    val c: EclipseColors @Composable @ReadOnlyComposable get() = LocalEclipse.current
}

private val EclipseTypography = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.SemiBold, lineHeight = 32.sp),
    titleMedium = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 12.sp)
)

@Composable
fun EclipseTheme(mode: String, content: @Composable () -> Unit) {
    val dark = when (mode) { "dark" -> true; "light" -> false; else -> isSystemInDarkTheme() }
    val e = if (dark) DarkEclipse else LightEclipse
    val scheme = if (dark) darkColorScheme(
        primary = e.accent, onPrimary = e.onAccent, primaryContainer = e.accentContainer, onPrimaryContainer = e.textPrimary,
        background = e.background, onBackground = e.textPrimary, surface = e.surface, onSurface = e.textPrimary,
        surfaceVariant = e.surfaceSecondary, onSurfaceVariant = e.textSecondary, outline = e.outline, error = e.error,
        secondaryContainer = e.accentContainer, onSecondaryContainer = e.textPrimary
    ) else lightColorScheme(
        primary = e.accent, onPrimary = e.onAccent, primaryContainer = e.accentContainer, onPrimaryContainer = e.textPrimary,
        background = e.background, onBackground = e.textPrimary, surface = e.surface, onSurface = e.textPrimary,
        surfaceVariant = e.surfaceSecondary, onSurfaceVariant = e.textSecondary, outline = e.outline, error = e.error,
        secondaryContainer = e.accentContainer, onSecondaryContainer = e.textPrimary
    )
    CompositionLocalProvider(LocalEclipse provides e) {
        MaterialTheme(colorScheme = scheme, typography = EclipseTypography, content = content)
    }
}
```

---

### `app/src/main/java/com/eclipsecompanion/app/ui/Components.kt`
```kotlin
package com.eclipsecompanion.app.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eclipsecompanion.app.domain.Reported
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 840.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp), content = content
        )
    }
}

@Composable
fun ECard(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Surface(modifier.fillMaxWidth(), shape = shape, color = E.c.surface, border = BorderStroke(1.dp, E.c.outline)) {
        Column(
            (if (onClick != null) Modifier.clip(shape).clickable(role = Role.Button, onClick = onClick) else Modifier).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp), content = content
        )
    }
}

@Composable fun Title(text: String) = Text(text, style = MaterialTheme.typography.headlineMedium, color = E.c.textPrimary)
@Composable fun Section(text: String) = Text(text, style = MaterialTheme.typography.titleMedium, color = E.c.textPrimary)
@Composable fun Body(text: String, modifier: Modifier = Modifier, color: Color = E.c.textSecondary, align: TextAlign? = null) =
    Text(text, modifier, style = MaterialTheme.typography.bodyMedium, color = color, textAlign = align)

enum class Kind { INFO, WARN, ERROR, OK }

@Composable
fun Banner(text: String, kind: Kind = Kind.INFO, modifier: Modifier = Modifier) {
    val col = when (kind) { Kind.INFO -> E.c.accent; Kind.WARN -> E.c.warning; Kind.ERROR -> E.c.error; Kind.OK -> E.c.success }
    val icon = if (kind == Kind.INFO || kind == Kind.OK) Icons.Rounded.Info else Icons.Rounded.Warning
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = E.c.surfaceSecondary, border = BorderStroke(1.dp, col)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = col, modifier = Modifier.size(20.dp))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = E.c.textPrimary)
        }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, danger: Boolean = false) {
    Button(
        onClick, modifier.heightIn(min = 48.dp), enabled = enabled, shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (danger) E.c.error else E.c.accent,
            contentColor = if (danger) E.c.background else E.c.onAccent,
            disabledContainerColor = E.c.surfaceSecondary, disabledContentColor = E.c.textSecondary
        )
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    OutlinedButton(
        onClick, modifier.heightIn(min = 48.dp), enabled = enabled, shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, E.c.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = E.c.accent, disabledContentColor = E.c.textSecondary)
    ) { Text(text, style = MaterialTheme.typography.labelLarge) }
}

@Composable
fun SwitchRow(title: String, subtitle: String?, checked: Boolean, enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = if (enabled) E.c.textPrimary else E.c.textSecondary)
            if (subtitle != null) Body(subtitle)
        }
        Switch(checked, onChange, enabled = enabled)
    }
}

@Composable
fun SettingRow(title: String, subtitle: String? = null, icon: ImageVector? = null, onClick: (() -> Unit)? = null, trailing: (@Composable () -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 56.dp).then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = E.c.accent)
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = E.c.textPrimary)
            if (subtitle != null) Body(subtitle)
        }
        trailing?.invoke()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChipRow(items: List<String>, selected: String?, onSelect: (String) -> Unit) {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { name ->
            FilterChip(selected = name == selected, onClick = { onSelect(name) }, label = { Text(name) }, shape = RoundedCornerShape(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScreen(title: String, onBack: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(title, style = MaterialTheme.typography.titleMedium) },
            navigationIcon = { IconButton(onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = E.c.background, titleContentColor = E.c.textPrimary, navigationIconContentColor = E.c.textPrimary)
        )
        Box(Modifier.weight(1f)) { ScreenColumn(content) }
    }
}

@Composable
fun ConnectionPill(text: String, ok: Boolean) {
    Surface(shape = CircleShape, color = if (ok) E.c.accentContainer else E.c.surfaceSecondary, border = BorderStroke(1.dp, if (ok) E.c.success else E.c.outline)) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Canvas(Modifier.size(8.dp)) { drawCircle(if (ok) Color(0xFF2D9B6A) else Color(0xFF8A8A86)) }
            Text(text, style = MaterialTheme.typography.labelMedium, color = E.c.textPrimary)
        }
    }
}

/** Mode card: shows ONLY the confirmed state as selected; [pending] shows a progress ring. */
@Composable
fun ModeCard(label: String, icon: ImageVector, selected: Boolean, pending: Boolean, enabled: Boolean, reason: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(20.dp)
    val state = when { pending -> "Changing, waiting for earbuds"; selected -> "Selected, confirmed by earbuds"; !enabled -> "Unavailable. ${reason ?: ""}"; else -> "Not selected" }
    Surface(
        modifier.heightIn(min = 112.dp).semantics { stateDescription = state; contentDescription = label },
        shape = shape, color = if (selected) E.c.accentContainer else E.c.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) E.c.accent else E.c.outline)
    ) {
        Column(
            Modifier.clip(shape).clickable(enabled = enabled && !pending, role = Role.Button, onClick = onClick).padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
        ) {
            Box(Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                when {
                    pending -> CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp, color = E.c.accent)
                    selected -> Icon(Icons.Rounded.Check, contentDescription = null, tint = E.c.accent)
                    else -> Icon(icon, contentDescription = null, tint = if (enabled) E.c.accent else E.c.textSecondary)
                }
            }
            Text(label, style = MaterialTheme.typography.labelLarge, color = if (enabled) E.c.textPrimary else E.c.textSecondary, textAlign = TextAlign.Center)
            if (!enabled && reason != null) Text(reason, style = MaterialTheme.typography.labelSmall, color = E.c.textSecondary, textAlign = TextAlign.Center, maxLines = 3)
        }
    }
}

private fun hhmm(ms: Long) = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))

@Composable
fun BatteryCard(label: String, value: Reported<Int>, modifier: Modifier = Modifier) {
    val desc = when (value) {
        is Reported.Available -> "$label battery ${value.value} percent, last reported ${hhmm(value.atMillis)}"
        is Reported.Unavailable -> "$label battery not available"
    }
    Surface(modifier.semantics(mergeDescendants = true) { contentDescription = desc }, shape = RoundedCornerShape(20.dp), color = E.c.surface, border = BorderStroke(1.dp, E.c.outline)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = E.c.textSecondary)
            when (value) {
                is Reported.Available -> {
                    Text("${value.value}%", style = MaterialTheme.typography.titleMedium, color = E.c.textPrimary)
                    LinearProgressIndicator(
                        progress = { value.value / 100f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                        color = if (value.value <= 20) E.c.warning else E.c.success, trackColor = E.c.surfaceSecondary
                    )
                    Text("Last reported ${hhmm(value.atMillis)}", style = MaterialTheme.typography.labelSmall, color = E.c.textSecondary)
                }
                is Reported.Unavailable -> {
                    Text("Not available", style = MaterialTheme.typography.titleMedium, color = E.c.textSecondary)
                    Text(value.reason, style = MaterialTheme.typography.labelSmall, color = E.c.textSecondary, maxLines = 2)
                }
            }
        }
    }
}

/** Original illustration (not the vendor's artwork): pearl-white earbuds and a charging case. */
@Composable
fun EclipseIllustration(modifier: Modifier = Modifier) {
    val c = E.c
    Canvas(modifier.fillMaxWidth().aspectRatio(2.2f).semantics { contentDescription = "Illustration of white earbuds and a charging case" }) {
        val w = size.width; val h = size.height
        val caseTL = Offset(w * 0.32f, h * 0.30f); val caseSz = Size(w * 0.36f, h * 0.58f)
        drawRoundRect(c.surfaceSecondary, caseTL, caseSz, CornerRadius(h * 0.22f))
        drawRoundRect(c.outline, caseTL, caseSz, CornerRadius(h * 0.22f), style = Stroke(width = 3f))
        drawLine(c.outline, Offset(caseTL.x, h * 0.54f), Offset(caseTL.x + caseSz.width, h * 0.54f), strokeWidth = 3f)
        drawCircle(c.accent, h * 0.025f, Offset(w * 0.5f, h * 0.68f))
        fun bud(cx: Float, lean: Float) {
            val r = h * 0.15f
            val head = Offset(cx, h * 0.36f)
            drawRoundRect(Color.White, Offset(cx - r * 0.38f + lean, h * 0.40f), Size(r * 0.76f, h * 0.42f), CornerRadius(r * 0.38f))
            drawRoundRect(c.outline, Offset(cx - r * 0.38f + lean, h * 0.40f), Size(r * 0.76f, h * 0.42f), CornerRadius(r * 0.38f), style = Stroke(width = 3f))
            drawCircle(Color.White, r, head)
            drawCircle(c.outline, r, head, style = Stroke(width = 3f))
            drawCircle(c.surfaceSecondary, r * 0.45f, head)
        }
        bud(w * 0.16f, -h * 0.02f)
        bud(w * 0.84f, h * 0.02f)
    }
}
```
