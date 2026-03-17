package com.tembt.ios

import com.tembt.di.initKoin
import com.tembt.di.iosPlatformModule
import com.tembt.platform.PlayerStorage
import com.tembt.presentation.app.AppViewModel
import com.tembt.presentation.map.MapViewModel
import com.tembt.presentation.schedule.ScheduleViewModel
import com.tembt.presentation.welcome.WelcomeViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

// Called once from Swift iOSApp.init() to start the Koin container.
fun startKoin() {
    initKoin(platformModules = listOf(iosPlatformModule))
}

// Provides convenient access to Koin-managed objects from Swift.
object KoinHelper : KoinComponent {
    fun getMapViewModelIos(): MapViewModelIos = MapViewModelIos(get<MapViewModel>())
    fun getWelcomeViewModelIos(): WelcomeViewModelIos = WelcomeViewModelIos(get<WelcomeViewModel>())
    fun getAppViewModelIos(): AppViewModelIos = AppViewModelIos(get<AppViewModel>())
    fun getScheduleViewModelIos(): ScheduleViewModelIos = ScheduleViewModelIos(get<ScheduleViewModel>())

    // Exposed for SwiftUI to read the initial value synchronously and avoid a flash
    fun isPlayerRegistered(): Boolean = get<PlayerStorage>().isRegistered()
}
