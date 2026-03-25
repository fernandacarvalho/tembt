package com.tembt.ui

import androidx.compose.ui.window.ComposeUIViewController
import com.tembt.ui.permission.PermissionScreen
import com.tembt.ui.schedule.ScheduleScreen
import com.tembt.ui.theme.TembtTheme
import com.tembt.ui.tournament.TournamentScreen
import com.tembt.ui.welcome.WelcomeScreen
import platform.UIKit.UIViewController

fun welcomeViewController(onRegistered: () -> Unit): UIViewController =
    ComposeUIViewController {
        TembtTheme { WelcomeScreen(onRegistered = onRegistered) }
    }

fun scheduleViewController(): UIViewController =
    ComposeUIViewController {
        TembtTheme { ScheduleScreen() }
    }

fun tournamentViewController(): UIViewController =
    ComposeUIViewController {
        TembtTheme { TournamentScreen() }
    }

fun permissionViewController(
    isDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
): UIViewController =
    ComposeUIViewController {
        TembtTheme {
            PermissionScreen(
                isDenied = isDenied,
                onRequestPermission = onRequestPermission,
                onOpenSettings = onOpenSettings
            )
        }
    }
