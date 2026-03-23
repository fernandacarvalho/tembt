package com.tembt.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

// Font used for button labels and display text requiring condensed black weight.
// iOS: Helvetica Neue Condensed Black (bundled — licensed for iOS system use).
// Android: Roboto (system font, Apache 2.0) — Helvetica is proprietary and must not ship on Android.
@Composable
expect fun condensedBlackFontFamily(): FontFamily
