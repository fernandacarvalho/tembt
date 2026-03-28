package com.tembt.domain.usecase

import kotlinx.datetime.LocalDate

/**
 * Returns true if the court has a session scheduled for [today].
 *
 * The check is delegated entirely to the API: [sessionDate] is the date
 * returned by GET /window. If the API says the session is today, monitoring
 * proceeds; otherwise it stops.
 */
class IsCourtOpenUseCase {
    operator fun invoke(today: LocalDate, sessionDate: String): Boolean =
        today.toString() == sessionDate
}
