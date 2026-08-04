package com.tembt.domain.usecase

import com.tembt.domain.model.ScheduleWindow
import kotlinx.datetime.LocalDate

/**
 * Returns true if the court has a session scheduled for [today] and [nowHour]
 * is still within the session's operating window.
 *
 * The session date/hours are delegated entirely to the API via [window]
 * (GET /window). If the API says the session isn't today, or [nowHour] is at
 * or past closing, monitoring stops.
 */
class IsCourtOpenUseCase {
    operator fun invoke(today: LocalDate, nowHour: Int, window: ScheduleWindow): Boolean {
        val isSessionDay = today.toString() == window.date
        if (!isSessionDay) return false
        return !isPastClosingHour(nowHour, window.startHour, window.endHour)
    }

    // Overnight windows (endHour <= startHour, e.g. 22 -> 2) stay open through
    // midnight and only close once nowHour reaches endHour the following morning.
    private fun isPastClosingHour(nowHour: Int, startHour: Int, endHour: Int): Boolean =
        if (endHour > startHour) {
            nowHour >= endHour
        } else {
            nowHour >= endHour && nowHour < startHour
        }
}
