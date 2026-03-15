package com.tembt.ios

import com.tembt.di.initKoin
import com.tembt.di.iosPlatformModule
import com.tembt.presentation.map.MapViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

// Called once from Swift iOSApp.init() to start the Koin container.
fun startKoin() {
    initKoin(platformModules = listOf(iosPlatformModule))
}

// Provides convenient access to Koin-managed objects from Swift.
object KoinHelper : KoinComponent {
    fun getMapViewModelIos(): MapViewModelIos = MapViewModelIos(get<MapViewModel>())
}
