package com.tembt.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.tembt.android.ui.RootScreen
import com.tembt.domain.model.AppTab
import com.tembt.presentation.app.AppViewModel
import com.tembt.ui.theme.TembtTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModel()
    private val deepLinkTab = mutableStateOf<AppTab?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Hold the launch screen until the session tab config is resolved (max ~3s)
        installSplashScreen().setKeepOnScreenCondition { appViewModel.enabledTabs.value == null }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLinkTab.value = parseDeepLink(intent)
        setContent {
            TembtTheme {
                RootScreen(viewModel = appViewModel, deepLinkTab = deepLinkTab.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkTab.value = parseDeepLink(intent)
    }

    private fun parseDeepLink(intent: Intent?): AppTab? {
        val uri: Uri = intent?.data ?: return null
        return when (uri.host) {
            "schedule" -> AppTab.SCHEDULE
            else -> null
        }
    }
}
