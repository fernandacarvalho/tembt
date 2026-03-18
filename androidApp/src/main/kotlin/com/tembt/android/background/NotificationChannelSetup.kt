package com.tembt.android.background

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

/** Creates the notification channel required by Android 8+ for the monitoring service. */
fun createMonitoringNotificationChannel(context: Context) {
    val channel = NotificationChannel(
        CourtMonitoringForegroundService.CHANNEL_ID,
        "Monitoramento de quadra",
        NotificationManager.IMPORTANCE_LOW
    ).apply {
        description = "Notificação em segundo plano para monitoramento de localização na quadra"
        setShowBadge(false)
    }
    val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.createNotificationChannel(channel)
}
