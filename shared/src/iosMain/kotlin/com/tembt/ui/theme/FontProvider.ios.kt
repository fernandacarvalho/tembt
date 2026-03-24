package com.tembt.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.tembt.shared.generated.resources.HelveticaNeue_CondensedBlack
import com.tembt.shared.generated.resources.HelveticaNeue_CondensedBold
// Note: these .otf files live in commonMain/composeResources/font/ — iosMain resources
// are not embedded in the Kotlin/Native framework at runtime.
import com.tembt.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

// iOS uses Helvetica Neue Condensed (proprietary — must NOT be in commonMain or Android bundle).
// All variants extracted from HelveticaNeue.ttc into iosMain/composeResources/font/.
@Composable
actual fun condensedBlackFontFamily(): FontFamily =
    FontFamily(Font(Res.font.HelveticaNeue_CondensedBlack, weight = FontWeight.Black))

@Composable
actual fun condensedBoldFontFamily(): FontFamily =
    FontFamily(Font(Res.font.HelveticaNeue_CondensedBold, weight = FontWeight.Bold))

@Composable
actual fun condensedFontFamily(): FontFamily = FontFamily(
    Font(Res.font.HelveticaNeue_CondensedBold, weight = FontWeight.Bold),
    Font(Res.font.HelveticaNeue_CondensedBlack, weight = FontWeight.Black),
)
