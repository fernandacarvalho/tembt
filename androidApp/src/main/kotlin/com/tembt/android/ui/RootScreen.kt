package com.tembt.android.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tembt.android.R
import com.tembt.android.ui.map.MapScreen
import com.tembt.domain.model.AppTab
import com.tembt.presentation.app.AppViewModel
import com.tembt.ui.schedule.ScheduleScreen
import com.tembt.ui.theme.AlabasterGrey
import com.tembt.ui.theme.Amaranth
import com.tembt.ui.tournament.TournamentScreen
import com.tembt.ui.welcome.WelcomeScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun RootScreen(viewModel: AppViewModel = koinViewModel(), deepLinkTab: AppTab? = null) {
    val showWelcome by viewModel.showWelcome.collectAsStateWithLifecycle()
    val enabledTabs by viewModel.enabledTabs.collectAsStateWithLifecycle()
    val tabs = enabledTabs

    when {
        showWelcome -> WelcomeScreen(onRegistered = { viewModel.onRegistered() })
        // Still resolving — the system splash screen covers this frame
        tabs == null -> Box(modifier = Modifier.fillMaxSize())
        tabs.size == 1 -> MapScreen()
        else -> MainTabs(tabs = tabs, deepLinkTab = deepLinkTab)
    }
}

private data class TabSpec(
    val icon: ImageVector,
    @StringRes val labelRes: Int,
)

private fun tabSpec(tab: AppTab): TabSpec = when (tab) {
    AppTab.MAP -> TabSpec(Icons.Default.Map, R.string.tab_court)
    AppTab.SCHEDULE -> TabSpec(Icons.AutoMirrored.Filled.List, R.string.tab_schedule)
    AppTab.TOURNAMENTS -> TabSpec(Icons.Default.EmojiEvents, R.string.tab_tournaments)
}

@Composable
private fun MainTabs(tabs: List<AppTab>, deepLinkTab: AppTab? = null) {
    var selectedTabName by rememberSaveable { mutableStateOf(AppTab.MAP.name) }
    // Fall back to MAP if the restored selection is no longer among the enabled tabs
    val selectedTab = tabs.firstOrNull { it.name == selectedTabName } ?: AppTab.MAP

    LaunchedEffect(deepLinkTab) {
        if (deepLinkTab != null && deepLinkTab in tabs) selectedTabName = deepLinkTab.name
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Amaranth) {
                val itemColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AlabasterGrey,
                    selectedTextColor = AlabasterGrey,
                    indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unselectedIconColor = AlabasterGrey.copy(alpha = 0.6f),
                    unselectedTextColor = AlabasterGrey.copy(alpha = 0.6f),
                )
                tabs.forEach { tab ->
                    val spec = tabSpec(tab)
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTabName = tab.name },
                        icon = { Icon(spec.icon, contentDescription = null) },
                        label = { Text(stringResource(spec.labelRes)) },
                        colors = itemColors
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())) {
            when (selectedTab) {
                AppTab.MAP -> MapScreen()
                AppTab.SCHEDULE -> ScheduleScreen()
                AppTab.TOURNAMENTS -> TournamentScreen()
            }
        }
    }
}
