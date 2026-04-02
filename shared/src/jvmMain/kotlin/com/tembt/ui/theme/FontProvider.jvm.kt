package com.tembt.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

// JVM target exists only to run commonTest via ./gradlew shared:jvmTest.
// No font loading is needed here — SansSerif is a safe no-op fallback.
@Composable
actual fun condensedBlackFontFamily(): FontFamily = FontFamily.SansSerif

@Composable
actual fun condensedBoldFontFamily(): FontFamily = FontFamily.SansSerif

@Composable
actual fun condensedFontFamily(): FontFamily = FontFamily.SansSerif
