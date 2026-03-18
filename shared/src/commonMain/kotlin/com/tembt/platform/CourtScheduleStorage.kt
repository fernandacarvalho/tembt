package com.tembt.platform

import com.tembt.domain.model.CourtSchedule
import kotlinx.datetime.LocalDate

/** Persists the court operating hours and user monitoring preferences locally on the device. */
interface CourtScheduleStorage {
    /** Returns the saved schedule, or the default (6h–16h) if none has been saved yet. */
    fun getSchedule(): CourtSchedule

    /** Overwrites the locally stored schedule. */
    fun saveSchedule(schedule: CourtSchedule)

    /**
     * Records that the user chose to skip monitoring for the rest of [today].
     * The pause is scoped to a single calendar day and does not carry over.
     */
    fun pauseMonitoringForToday(today: LocalDate)

    /**
     * Returns true if the user previously called [pauseMonitoringForToday] for [date].
     */
    fun isMonitoringPausedFor(date: LocalDate): Boolean
}
