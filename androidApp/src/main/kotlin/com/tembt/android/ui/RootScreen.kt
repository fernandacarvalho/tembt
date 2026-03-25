package com.tembt.android.ui

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
import com.tembt.ui.theme.AlabasterGrey
import com.tembt.ui.theme.Amaranth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tembt.android.ui.map.MapScreen
import com.tembt.ui.schedule.ScheduleScreen
import com.tembt.ui.tournament.TournamentScreen
import com.tembt.ui.welcome.WelcomeScreen
import com.tembt.presentation.app.AppViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RootScreen(viewModel: AppViewModel = koinViewModel(), deepLinkTab: Int? = null) {
    val showWelcome by viewModel.showWelcome.collectAsStateWithLifecycle()

    if (showWelcome) {
        WelcomeScreen(onRegistered = { viewModel.onRegistered() })
    } else {
        MainTabs(deepLinkTab = deepLinkTab)
    }
}

@Composable
private fun MainTabs(deepLinkTab: Int? = null) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(deepLinkTab) {
        if (deepLinkTab != null) selectedTab = deepLinkTab
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
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Map, contentDescription = null) },
                    label = { Text("Quadra") },
                    colors = itemColors
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = null) },
                    label = { Text("Lista") },
                    colors = itemColors
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                    label = { Text("Torneios") },
                    colors = itemColors
                )
            }
        }
    ) { _ ->
        when (selectedTab) {
            0 -> MapScreen()
            1 -> ScheduleScreen()
            2 -> TournamentScreen()
        }
    }
}
