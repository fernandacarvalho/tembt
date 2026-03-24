package com.tembt.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

// Condensed Black — heaviest display weight ("play?" in the schedule header).
// iOS: Helvetica Neue Condensed Black (bundled). Android: Barlow Condensed Black (SIL OFL).
@Composable
expect fun condensedBlackFontFamily(): FontFamily

// Condensed Bold — bold display weight ("Bora pro" in the schedule header).
// iOS: same Condensed Black file declared at Bold weight (only condensed variant available).
// Android: Barlow Condensed Bold (SIL OFL).
@Composable
expect fun condensedBoldFontFamily(): FontFamily

// Combined condensed family with Bold + Black weights registered.
// Use this as the base fontFamily on Text when a single title mixes both weights via SpanStyle.
@Composable
expect fun condensedFontFamily(): FontFamily
