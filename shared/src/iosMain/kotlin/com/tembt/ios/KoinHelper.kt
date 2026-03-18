package com.tembt.ios

import com.tembt.di.initKoin
import com.tembt.di.iosPlatformModule
import com.tembt.domain.usecase.LocationMonitoringCoordinator
import com.tembt.platform.CourtMonitoringScheduler
import com.tembt.platform.CourtMonitoringSchedulerImpl
import com.tembt.platform.CourtScheduleStorage
import com.tembt.platform.PlayerStorage
import com.tembt.presentation.app.AppViewModel
import com.tembt.presentation.map.MapViewModel
import com.tembt.presentation.schedule.ScheduleViewModel
import com.tembt.presentation.welcome.WelcomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

// Called once from Swift iOSApp.init() to start the Koin container and arm the alarm.
fun startKoin() {
    initKoin(platformModules = listOf(iosPlatformModule))
    // Arm the daily monitoring alarm using the locally stored schedule (default 6h).
    val scheduler = KoinHelper.getCourtMonitoringScheduler()
    val schedule  = KoinHelper.getCourtScheduleStorage().getSchedule()
    scheduler.schedule(schedule.startHour)
}

// Provides convenient access to Koin-managed objects from Swift.
object KoinHelper : KoinComponent {
    fun getMapViewModelIos(): MapViewModelIos = MapViewModelIos(get<MapViewModel>())
    fun getWelcomeViewModelIos(): WelcomeViewModelIos = WelcomeViewModelIos(get<WelcomeViewModel>())
    fun getAppViewModelIos(): AppViewModelIos = AppViewModelIos(get<AppViewModel>())
    fun getScheduleViewModelIos(): ScheduleViewModelIos = ScheduleViewModelIos(get<ScheduleViewModel>())

    // Exposed for SwiftUI to read the initial value synchronously and avoid a flash
    fun isPlayerRegistered(): Boolean = get<PlayerStorage>().isRegistered()

    fun getCourtMonitoringScheduler(): CourtMonitoringScheduler = get()
    fun getCourtScheduleStorage(): CourtScheduleStorage = get()

    /**
     * Called from Swift's BGTask handler when the system fires the background task.
     * Runs one monitoring cycle and re-schedules the next alarm.
     * The [expirationHandler] closure should call BGTask.setTaskCompleted when done.
     */
    fun handleBackgroundMonitoringTask(expirationHandler: () -> Unit) {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        val coordinator = get<LocationMonitoringCoordinator>()
        val storage     = get<CourtScheduleStorage>()
        val scheduler   = get<CourtMonitoringScheduler>()

        scope.launch {
            try {
                val saoPaulo = TimeZone.of("America/Sao_Paulo")
                val today    = Clock.System.now().toLocalDateTime(saoPaulo).date
                coordinator.runCycle(today)
            } finally {
                // Re-arm the alarm for the next day
                val schedule = storage.getSchedule()
                scheduler.schedule(schedule.startHour)
                scope.cancel()
                expirationHandler()
            }
        }
    }
}
