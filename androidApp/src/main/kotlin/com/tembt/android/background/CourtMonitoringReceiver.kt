package com.tembt.android.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.tembt.android.background.CourtMonitoringForegroundService.Companion.ACTION_PAUSE_MONITORING
import com.tembt.platform.ACTION_START_COURT_MONITORING
import com.tembt.platform.CourtMonitoringScheduler
import com.tembt.platform.CourtScheduleStorage
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.android.ext.android.inject

/**
 * Receives three broadcasts:
 *
 * 1. [ACTION_START_COURT_MONITORING] — daily alarm fires → start the monitoring service.
 * 2. [Intent.ACTION_BOOT_COMPLETED]  — device rebooted → re-arm the alarm that was cleared.
 * 3. [ACTION_PAUSE_MONITORING]       — user tapped "Não monitorar hoje" in the notification
 *                                       → mark today as paused and stop the service.
 */
class CourtMonitoringReceiver : BroadcastReceiver() {

    private val scheduleStorage: CourtScheduleStorage by inject()
    private val scheduler: CourtMonitoringScheduler by inject()

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_START_COURT_MONITORING -> {
                val serviceIntent = Intent(context, CourtMonitoringForegroundService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                val schedule = scheduleStorage.getSchedule()
                scheduler.schedule(schedule.startHour)
            }

            ACTION_PAUSE_MONITORING -> {
                val today = Clock.System.now()
                    .toLocalDateTime(TimeZone.of("America/Sao_Paulo"))
                    .date
                scheduleStorage.pauseMonitoringForToday(today)
                context.stopService(
                    Intent(context, CourtMonitoringForegroundService::class.java)
                )
            }
        }
    }
}
