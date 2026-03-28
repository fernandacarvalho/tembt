package com.tembt.ui.permission

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tembt.shared.generated.resources.Res
import com.tembt.shared.generated.resources.permission_button
import com.tembt.shared.generated.resources.permission_how_choose
import com.tembt.shared.generated.resources.permission_how_prefix
import com.tembt.shared.generated.resources.permission_how_separator
import com.tembt.shared.generated.resources.permission_how_suffix
import com.tembt.shared.generated.resources.permission_how_title
import com.tembt.shared.generated.resources.permission_subtitle
import com.tembt.shared.generated.resources.permission_title
import com.tembt.shared.generated.resources.permission_why_body
import com.tembt.shared.generated.resources.permission_why_title
import com.tembt.shared.generated.resources.welcome_player
import com.tembt.ui.components.TembtButton
import com.tembt.ui.components.TembtButtonStyle
import com.tembt.ui.theme.AlabasterGrey
import com.tembt.ui.theme.PitchBlack
import com.tembt.ui.theme.SpicyPaprika
import com.tembt.ui.theme.condensedBoldFontFamily
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PermissionScreen(
    onRequestPermission: () -> Unit
) {
    val boldFont = condensedBoldFontFamily()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.welcome_player),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 28.dp)
                    .padding(top = 24.dp)
            ) {
                Text(
                    text = stringResource(Res.string.permission_title),
                    fontFamily = boldFont,
                    fontSize = 36.sp,
                    lineHeight = 42.sp,
                    color = Color.White
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(Res.string.permission_subtitle),
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(Modifier.height(32.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 24.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        clip = false
                    )
                    .background(
                        color = AlabasterGrey,
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(horizontal = 24.dp)
                    .padding(top = 28.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                PermissionCardSection(
                    title = stringResource(Res.string.permission_why_title),
                    message = stringResource(Res.string.permission_why_body)
                )

                Spacer(Modifier.height(24.dp))

                Text(
                    text = stringResource(Res.string.permission_how_title),
                    fontFamily = boldFont,
                    fontSize = 18.sp,
                    letterSpacing = 0.5.sp,
                    color = SpicyPaprika
                )

                Spacer(Modifier.height(10.dp))

                val howPrefix = stringResource(Res.string.permission_how_prefix)
                val howSeparator = stringResource(Res.string.permission_how_separator)
                val howChoose = stringResource(Res.string.permission_how_choose)
                val howSuffix = stringResource(Res.string.permission_how_suffix)
                Text(
                    text = buildAnnotatedString {
                        append(howPrefix)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(settingsAppName) }
                        append(howSeparator)
                        settingsPath.split(" > ").forEachIndexed { index, segment ->
                            if (index > 0) append(howSeparator)
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(segment) }
                        }
                        append(howChoose)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(settingsOption) }
                        append(howSuffix)
                    },
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = PitchBlack
                )

                Spacer(Modifier.height(50.dp))

                TembtButton(
                    title = stringResource(Res.string.permission_button),
                    onClick = onRequestPermission,
                    style = TembtButtonStyle.SolidInverted,
                    modifier = Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(50.dp),
                        ambientColor = Color.Black.copy(alpha = 0.6f),
                        spotColor = Color.Black.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}

@Composable
private fun PermissionCardSection(title: String, message: String) {
    val boldFont = condensedBoldFontFamily()

    // 5. Larger card section titles
    Column {
        Text(
            text = title,
            fontFamily = boldFont,
            fontSize = 18.sp,
            letterSpacing = 0.5.sp,
            color = SpicyPaprika
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = message,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Normal,
            color = PitchBlack
        )
    }
}
