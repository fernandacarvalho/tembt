package com.tembt.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tembt.android.ui.map.MapScreen
import com.tembt.android.ui.welcome.WelcomeScreen
import com.tembt.presentation.app.AppViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RootScreen(viewModel: AppViewModel = koinViewModel()) {
    val showWelcome by viewModel.showWelcome.collectAsStateWithLifecycle()

    if (showWelcome) {
        WelcomeScreen(onRegistered = { viewModel.onRegistered() })
    } else {
        MapScreen()
    }
}
