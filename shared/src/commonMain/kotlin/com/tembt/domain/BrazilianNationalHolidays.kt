package com.tembt.domain

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus

/**
 * Brazilian national public holidays.
 *
 * Covers all fixed-date federal holidays and the four Easter-dependent
 * movable holidays (Carnaval Mon+Tue, Sexta-feira Santa, Corpus Christi).
 * Municipal/state holidays are intentionally omitted.
 */
object BrazilianNationalHolidays {

    fun isHoliday(date: LocalDate): Boolean =
        isFixedHoliday(date) || isEasterBasedHoliday(date)

    private fun isFixedHoliday(date: LocalDate): Boolean =
        when (date.month to date.dayOfMonth) {
            Month.JANUARY to 1   -> true  // Confraternização Universal (Ano Novo)
            Month.APRIL to 21    -> true  // Tiradentes
            Month.MAY to 1       -> true  // Dia do Trabalho
            Month.SEPTEMBER to 7 -> true  // Independência do Brasil
            Month.OCTOBER to 12  -> true  // Nossa Senhora Aparecida
            Month.NOVEMBER to 2  -> true  // Finados
            Month.NOVEMBER to 15 -> true  // Proclamação da República
            Month.NOVEMBER to 20 -> true  // Dia da Consciência Negra (Lei 14.759/2023)
            Month.DECEMBER to 25 -> true  // Natal
            else                 -> false
        }

    private fun isEasterBasedHoliday(date: LocalDate): Boolean {
        val easter = calculateEaster(date.year)
        return date == easter.plus(DatePeriod(days = -48)) ||  // Segunda de Carnaval
               date == easter.plus(DatePeriod(days = -47)) ||  // Terça de Carnaval
               date == easter.plus(DatePeriod(days = -2))  ||  // Sexta-feira Santa
               date == easter.plus(DatePeriod(days = 60))       // Corpus Christi
    }

    /**
     * Computes Easter Sunday for [year] using the Anonymous Gregorian algorithm
     * (Butcher's algorithm).
     */
    internal fun calculateEaster(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day   = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate(year, month, day)
    }
}
