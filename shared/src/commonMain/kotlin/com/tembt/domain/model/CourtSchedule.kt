package com.tembt.domain.model

/**
 * Defines the daily window during which the court operates.
 * Stored locally per [com.tembt.platform.CourtScheduleStorage].
 */
data class CourtSchedule(
    val startHour: Int = 6,
    val endHour: Int = 16
)
