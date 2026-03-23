package com.tembt.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.tembt.shared.generated.resources.HelveticaNeueCondensedBlack
import com.tembt.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
actual fun condensedBlackFontFamily(): FontFamily =
    FontFamily(Font(Res.font.HelveticaNeueCondensedBlack, weight = FontWeight.Black))
