package com.tembt.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.tembt.shared.generated.resources.BarlowCondensed_Black
import com.tembt.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

// Barlow Condensed (SIL Open Font License) — closest licensed alternative to Helvetica Neue Condensed.
// Helvetica Neue is proprietary and must not be distributed in the Android bundle.
@Composable
actual fun condensedBlackFontFamily(): FontFamily =
    FontFamily(Font(Res.font.BarlowCondensed_Black, weight = FontWeight.Black))
