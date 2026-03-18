package com.tembt.domain.usecase

import com.tembt.domain.BrazilianNationalHolidays
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/**
 * Returns true if the court should be monitored on [date].
 *
 * The court is considered open on weekends and Brazilian national holidays.
 * Regular weekdays are treated as non-operating days.
 */
class IsCourtOpenUseCase(
    private val holidays: BrazilianNationalHolidays = BrazilianNationalHolidays
) {
    operator fun invoke(date: LocalDate): Boolean =
        date.dayOfWeek == DayOfWeek.SATURDAY ||
        date.dayOfWeek == DayOfWeek.SUNDAY   ||
        holidays.isHoliday(date)
}
