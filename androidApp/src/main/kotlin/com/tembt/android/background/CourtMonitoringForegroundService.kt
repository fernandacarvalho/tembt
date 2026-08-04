package com.tembt.android.background

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.tembt.domain.usecase.LocationMonitoringCoordinator
import com.tembt.platform.CourtMonitoringScheduler
import com.tembt.platform.CourtScheduleStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.android.ext.android.inject

/**
 * Foreground service that runs the adaptive location-monitoring loop.
 *
 * Lifecycle:
 *  - Started by [CourtMonitoringReceiver] when the daily alarm fires.
 *  - Calls [LocationMonitoringCoordinator.runCycle] once per computed interval.
 *  - Stops itself when the court closing hour is reached or the coordinator
 *    signals that monitoring should stop (court closed today, user paused).
 *  - After stopping, re-arms the next day's alarm via [CourtMonitoringScheduler].
 *
 * The persistent notification includes a "Não monitorar hoje" action that lets
 * the user opt out for the rest of the day without going into app Settings.
 */
class CourtMonitoringForegroundService : Service() {

    private val coordinator: LocationMonitoringCoordinator by inject()
    private val scheduleStorage: CourtScheduleStorage by inject()
    private val scheduler: CourtMonitoringScheduler by inject()

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification())
        startMonitoringLoop()
        return START_STICKY
    }

    private fun startMonitoringLoop() {
        serviceScope.launch {
            val saoPaulo = TimeZone.of("America/Sao_Paulo")
            val schedule = scheduleStorage.getSchedule()

            while (true) {
                val now = Clock.System.now().toLocalDateTime(saoPaulo)

                val interval = coordinator.runCycle(now)
                if (interval == null) break  // paused by user, court closed today, or past closing hour

                delay(interval.minutes * 60_000L)
            }

            // Re-arm the alarm for tomorrow before stopping
            scheduler.schedule(schedule.startHour)
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun buildNotification(): Notification {
        val pauseIntent = PendingIntent.getBroadcast(
            this,
            REQUEST_CODE_PAUSE,
            Intent(ACTION_PAUSE_MONITORING).setPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("TEMBT")
            .setContentText("Monitorando localização para a quadra")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .addAction(
                Notification.Action.Builder(
                    null,
                    "Não monitorar hoje",
                    pauseIntent
                ).build()
            )
            .build()
    }

    companion object {
        const val CHANNEL_ID            = "tembt_monitoring_channel"
        const val NOTIFICATION_ID       = 1001
        const val ACTION_PAUSE_MONITORING = "com.tembt.android.ACTION_PAUSE_COURT_MONITORING"
        private const val REQUEST_CODE_PAUSE = 1002
    }
}
