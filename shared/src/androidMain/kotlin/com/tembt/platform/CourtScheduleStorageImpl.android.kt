package com.tembt.platform

import android.content.Context
import com.tembt.domain.model.CourtSchedule
import kotlinx.datetime.LocalDate

private const val PREF_COURT_SCHEDULE  = "tembt_court_schedule"
private const val KEY_START_HOUR       = "start_hour"
private const val KEY_END_HOUR         = "end_hour"
private const val KEY_PAUSED_DATE      = "monitoring_paused_date"
private const val DEFAULT_START        = 6
private const val DEFAULT_END          = 16

class CourtScheduleStorageImpl(private val context: Context) : CourtScheduleStorage {

    private val prefs = context.getSharedPreferences(PREF_COURT_SCHEDULE, Context.MODE_PRIVATE)

    override fun getSchedule(): CourtSchedule = CourtSchedule(
        startHour = prefs.getInt(KEY_START_HOUR, DEFAULT_START),
        endHour   = prefs.getInt(KEY_END_HOUR,   DEFAULT_END)
    )

    override fun saveSchedule(schedule: CourtSchedule) {
        prefs.edit()
            .putInt(KEY_START_HOUR, schedule.startHour)
            .putInt(KEY_END_HOUR,   schedule.endHour)
            .apply()
    }

    override fun pauseMonitoringForToday(today: LocalDate) {
        prefs.edit().putString(KEY_PAUSED_DATE, today.toString()).apply()
    }

    override fun isMonitoringPausedFor(date: LocalDate): Boolean =
        prefs.getString(KEY_PAUSED_DATE, null) == date.toString()
}
