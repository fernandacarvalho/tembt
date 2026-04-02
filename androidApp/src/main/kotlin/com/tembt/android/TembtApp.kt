package com.tembt.android

import android.app.Application
import com.tembt.android.background.createMonitoringNotificationChannel
import com.tembt.di.androidPlatformModule
import com.tembt.di.initKoin
import com.tembt.domain.model.CourtSchedule
import com.tembt.domain.repository.WindowRepository
import com.tembt.platform.CourtMonitoringScheduler
import com.tembt.platform.CourtScheduleStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TembtApp : Application() {

    private val scheduleStorage: CourtScheduleStorage by inject()
    private val scheduler: CourtMonitoringScheduler by inject()
    private val windowRepository: WindowRepository by inject()

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        initKoin(platformModules = listOf(androidPlatformModule)) {
            androidContext(this@TembtApp)
            androidLogger()
        }

        createMonitoringNotificationChannel(this)

        // Arm the alarm immediately with the cached schedule (fast, works offline).
        val cached = scheduleStorage.getSchedule()
        scheduler.schedule(cached.startHour)

        // Fetch the real schedule from the API and reschedule with the correct startHour.
        // This ensures first-time users and schedule changes are always up to date.
        appScope.launch {
            windowRepository.getWindow().getOrNull()?.let { window ->
                scheduleStorage.saveSchedule(CourtSchedule(window.startHour, window.endHour))
                scheduler.schedule(window.startHour)
            }
        }
    }
}
