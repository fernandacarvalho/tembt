package com.tembt.ui.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tembt.presentation.welcome.WelcomeUiEvent
import com.tembt.presentation.welcome.WelcomeUiState
import com.tembt.presentation.welcome.WelcomeViewModel
import com.tembt.shared.generated.resources.Res
import com.tembt.shared.generated.resources.tembt_logo
import com.tembt.shared.generated.resources.welcome_button
import com.tembt.shared.generated.resources.welcome_name_placeholder
import com.tembt.shared.generated.resources.welcome_player
import com.tembt.shared.generated.resources.welcome_subtitle
import com.tembt.shared.generated.resources.welcome_title
import com.tembt.ui.components.TembtButton
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val ColorDeepMocha  = Color(0xFF433633)
private val ColorPitchBlack = Color(0xFF141204)

@Composable
fun WelcomeScreen(
    onRegistered: () -> Unit,
    viewModel: WelcomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var name by rememberSaveable { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            if (event is WelcomeUiEvent.NavigateToMap) onRegistered()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.welcome_player),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 32.dp)
        ) {
            Spacer(Modifier.weight(1f))

            Image(
                painter = painterResource(Res.drawable.tembt_logo),
                contentDescription = stringResource(Res.string.welcome_title),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .height(180.dp),
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = stringResource(Res.string.welcome_subtitle),
                fontSize = 18.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.85f),
                lineHeight = 26.sp
            )

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = {
                    Text(stringResource(Res.string.welcome_name_placeholder), color = ColorDeepMocha.copy(alpha = 0.45f))
                },
                singleLine = true,
                enabled = uiState !is WelcomeUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    disabledContainerColor = Color.White.copy(alpha = 0.8f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    errorBorderColor = Color.Transparent,
                    focusedTextColor = ColorPitchBlack,
                    unfocusedTextColor = ColorPitchBlack,
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        viewModel.onStartClicked(name)
                    }
                ),
                isError = uiState is WelcomeUiState.Error
            )

            if (uiState is WelcomeUiState.Error) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = (uiState as WelcomeUiState.Error).message,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(50.dp))

            TembtButton(
                title = stringResource(Res.string.welcome_button),
                onClick = {
                    focusManager.clearFocus()
                    viewModel.onStartClicked(name)
                },
                enabled = name.isNotBlank(),
                isLoading = uiState is WelcomeUiState.Loading
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}
