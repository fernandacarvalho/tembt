package com.tembt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Official Palette ──────────────────────────────────────────────────────────
val AlabasterGrey = Color(0xFFE7E7E7)  // Light neutral — backgrounds, disabled text
val SpicyPaprika  = Color(0xFFDB5316)  // Warm orange — secondary accent, gradients
val Amaranth      = Color(0xFFCE4257)  // Red-pink — primary brand colour, CTAs
val DeepMocha     = Color(0xFF433633)  // Dark brown — secondary text, placeholders
val PitchBlack    = Color(0xFF141204)  // Near-black — primary text

// ── Semantic aliases ──────────────────────────────────────────────────────────
val TembtWhite = Color.White

val LightColors = lightColorScheme(
    primary              = Amaranth,
    onPrimary            = AlabasterGrey,
    primaryContainer     = Color(0xFFF8D7DC),
    onPrimaryContainer   = PitchBlack,
    secondary            = SpicyPaprika,
    onSecondary          = AlabasterGrey,
    secondaryContainer   = Color(0xFFF9DDD1),
    onSecondaryContainer = PitchBlack,
    background           = AlabasterGrey,
    onBackground         = PitchBlack,
    surface              = TembtWhite,
    onSurface            = PitchBlack,
    surfaceVariant       = Color(0xFFF0EEEE),
    onSurfaceVariant     = DeepMocha,
    outline              = AlabasterGrey,
    error                = Amaranth,
    onError              = TembtWhite,
)

val TembtTypography = Typography(
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
        typography  = TembtTypography,
        content     = content
    )
}
