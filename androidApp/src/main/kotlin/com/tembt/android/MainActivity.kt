package com.tembt.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.tembt.android.ui.RootScreen
import com.tembt.ui.theme.TembtTheme

class MainActivity : ComponentActivity() {

    private val deepLinkTab = mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLinkTab.value = parseDeepLink(intent)
        setContent {
            TembtTheme {
                RootScreen(deepLinkTab = deepLinkTab.value)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkTab.value = parseDeepLink(intent)
    }

    private fun parseDeepLink(intent: Intent?): Int? {
        val uri: Uri = intent?.data ?: return null
        return when (uri.host) {
            "schedule" -> 1  // Lista tab index
            else -> null
        }
    }
}
