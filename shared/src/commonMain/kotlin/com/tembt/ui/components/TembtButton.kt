package com.tembt.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tembt.ui.theme.Amaranth
import com.tembt.ui.theme.AlabasterGrey
import com.tembt.ui.theme.condensedBlackFontFamily

enum class TembtButtonStyle {
    /** White border, transparent fill, white text — for use on coloured backgrounds */
    Stroke,
    /** Solid orange fill, white text — primary action */
    SolidPrimary,
    /** Solid white fill, dark text — for use on coloured backgrounds */
    SolidInverted,
}

@Composable
fun TembtButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TembtButtonStyle = TembtButtonStyle.Stroke,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    val fontFamily = condensedBlackFontFamily()

    val containerColor = when (style) {
        TembtButtonStyle.Stroke        -> Color.Transparent
        TembtButtonStyle.SolidPrimary  -> Amaranth
        TembtButtonStyle.SolidInverted -> AlabasterGrey
    }
    val contentColor = when (style) {
        TembtButtonStyle.Stroke        -> AlabasterGrey
        TembtButtonStyle.SolidPrimary  -> AlabasterGrey
        TembtButtonStyle.SolidInverted -> Amaranth
    }
    val isEnabled = enabled && !isLoading
    val borderColor = when (style) {
        TembtButtonStyle.Stroke -> if (isEnabled) AlabasterGrey else AlabasterGrey.copy(alpha = 0.4f)
        else                    -> Color.Transparent
    }
    val disabledContainerColor = when (style) {
        TembtButtonStyle.Stroke -> Color.Transparent
        else                    -> containerColor.copy(alpha = 0.4f)
    }

    OutlinedButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(1.5.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = disabledContainerColor,
            disabledContentColor = contentColor.copy(alpha = 0.4f),
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )
        } else {
            var fontSize by remember(title) { mutableStateOf(14.sp) }
            Text(
                text = title,
                fontSize = fontSize,
                fontWeight = FontWeight.Black,
                fontFamily = fontFamily,
                letterSpacing = 2.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
                onTextLayout = { result ->
                    if (result.hasVisualOverflow) {
                        fontSize *= 0.9f
                    }
                }
            )
        }
    }
}
