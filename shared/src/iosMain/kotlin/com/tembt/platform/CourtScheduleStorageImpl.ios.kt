package com.tembt.platform

import com.tembt.domain.model.CourtSchedule
import kotlinx.datetime.LocalDate
import platform.Foundation.NSUserDefaults

private const val KEY_START_HOUR  = "tembt_court_start_hour"
private const val KEY_END_HOUR    = "tembt_court_end_hour"
private const val KEY_PAUSED_DATE = "tembt_monitoring_paused_date"
private const val DEFAULT_START   = 6
private const val DEFAULT_END     = 16

class CourtScheduleStorageImpl : CourtScheduleStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getSchedule(): CourtSchedule {
        val start = defaults.integerForKey(KEY_START_HOUR).toInt()
            .takeIf { it != 0 } ?: DEFAULT_START
        val end = defaults.integerForKey(KEY_END_HOUR).toInt()
            .takeIf { it != 0 } ?: DEFAULT_END
        return CourtSchedule(startHour = start, endHour = end)
    }

    override fun saveSchedule(schedule: CourtSchedule) {
        defaults.setInteger(schedule.startHour.toLong(), KEY_START_HOUR)
        defaults.setInteger(schedule.endHour.toLong(), KEY_END_HOUR)
        defaults.synchronize()
    }

    override fun pauseMonitoringForToday(today: LocalDate) {
        defaults.setObject(today.toString(), KEY_PAUSED_DATE)
        defaults.synchronize()
    }

    override fun isMonitoringPausedFor(date: LocalDate): Boolean =
        defaults.stringForKey(KEY_PAUSED_DATE) == date.toString()
}
