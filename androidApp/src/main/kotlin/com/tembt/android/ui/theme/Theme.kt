package com.tembt.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Palette ──────────────────────────────────────────────────────────────────
val PumpkinSpice  = Color(0xFFFC7A1E) // primary — buttons, highlights, checked states
val MoltenOrange  = Color(0xFFF24C00) // secondary — stronger accent
val DuskBlue      = Color(0xFF485696) // dark accent — muted text, icons, graphic shapes
val ApricotCream  = Color(0xFFF9C784) // highlight / surface variant
val AlabasterGrey = Color(0xFFE7E7E7) // background
val TembtBlack    = Color(0xFF1A1A1A) // primary text
val TembtWhite    = Color(0xFFFFFFFF)

private val LightColors = lightColorScheme(
    primary              = PumpkinSpice,
    onPrimary            = TembtWhite,
    primaryContainer     = ApricotCream,
    onPrimaryContainer   = TembtBlack,
    secondary            = MoltenOrange,
    onSecondary          = TembtWhite,
    secondaryContainer   = Color(0xFFFFF0E0),
    onSecondaryContainer = TembtBlack,
    tertiary             = DuskBlue,
    onTertiary           = TembtWhite,
    tertiaryContainer    = Color(0xFFDDE2F4),
    onTertiaryContainer  = TembtBlack,
    background           = AlabasterGrey,
    onBackground         = TembtBlack,
    surface              = TembtWhite,
    onSurface            = TembtBlack,
    surfaceVariant       = Color(0xFFF5F0E8),
    onSurfaceVariant     = DuskBlue,
    outline              = Color(0xFFCCCCCC),
    error                = MoltenOrange,
    onError              = TembtWhite,
)

// ── Typography — SansSerif (Helvetica-equivalent on Android) ─────────────────
private val TembtTypography = Typography(
    displayLarge   = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 57.sp, fontWeight = FontWeight.Black,    lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    headlineLarge  = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 32.sp, fontWeight = FontWeight.Bold,     lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 28.sp, fontWeight = FontWeight.Bold,     lineHeight = 36.sp),
    titleLarge     = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, lineHeight = 28.sp),
    titleMedium    = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, lineHeight = 24.sp),
    titleSmall     = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, fontWeight = FontWeight.Medium,   lineHeight = 20.sp),
    bodyLarge      = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, fontWeight = FontWeight.Normal,   lineHeight = 24.sp),
    bodyMedium     = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, fontWeight = FontWeight.Normal,   lineHeight = 20.sp),
    bodySmall      = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, fontWeight = FontWeight.Normal,   lineHeight = 16.sp),
    labelLarge     = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, fontWeight = FontWeight.Medium,   lineHeight = 20.sp),
    labelMedium    = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, fontWeight = FontWeight.Medium,   lineHeight = 16.sp),
    labelSmall     = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 11.sp, fontWeight = FontWeight.Medium,   lineHeight = 16.sp),
)

@Composable
fun TembtTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = TembtTypography,
        content = content
    )
}
