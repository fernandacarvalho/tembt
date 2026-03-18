package com.tembt.platform

/**
 * Schedules (or cancels) the daily background alarm that starts location
 * monitoring when the court operating window begins.
 *
 * Platform-specific: Android uses AlarmManager, iOS uses BGTaskScheduler.
 */
interface CourtMonitoringScheduler {
    /**
     * Arms the next daily trigger for [startHourOfDay] (0–23, local São Paulo time).
     * Calling this again replaces any previously scheduled alarm.
     */
    fun schedule(startHourOfDay: Int)

    /** Cancels any pending alarm / task. */
    fun cancel()
}
