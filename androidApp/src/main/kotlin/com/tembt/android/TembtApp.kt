package com.tembt.android

import android.app.Application
import com.tembt.android.background.createMonitoringNotificationChannel
import com.tembt.di.androidPlatformModule
import com.tembt.di.initKoin
import com.tembt.platform.CourtMonitoringScheduler
import com.tembt.platform.CourtScheduleStorage
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class TembtApp : Application() {

    private val scheduleStorage: CourtScheduleStorage by inject()
    private val scheduler: CourtMonitoringScheduler by inject()

    override fun onCreate() {
        super.onCreate()

        initKoin(platformModules = listOf(androidPlatformModule)) {
            androidContext(this@TembtApp)
            androidLogger()
        }

        createMonitoringNotificationChannel(this)

        // Arm the daily monitoring alarm using the locally stored schedule (default 6h).
        val schedule = scheduleStorage.getSchedule()
        scheduler.schedule(schedule.startHour)
    }
}
