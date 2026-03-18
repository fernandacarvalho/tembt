package com.tembt.domain

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BrazilianNationalHolidaysTest {

    @Test
    fun `calculateEaster 2024 is March 31`() {
        assertEquals(LocalDate(2024, 3, 31), BrazilianNationalHolidays.calculateEaster(2024))
    }

    @Test
    fun `calculateEaster 2025 is April 20`() {
        assertEquals(LocalDate(2025, 4, 20), BrazilianNationalHolidays.calculateEaster(2025))
    }

    @Test
    fun `calculateEaster 2026 is April 5`() {
        assertEquals(LocalDate(2026, 4, 5), BrazilianNationalHolidays.calculateEaster(2026))
    }

    @Test
    fun `calculateEaster 2027 is March 28`() {
        assertEquals(LocalDate(2027, 3, 28), BrazilianNationalHolidays.calculateEaster(2027))
    }

    @Test
    fun `January 2 is not a holiday`() {
        assertFalse(BrazilianNationalHolidays.isHoliday(LocalDate(2026, 1, 2)))
    }

    @Test
    fun `June 15 regular day is not a holiday`() {
        assertFalse(BrazilianNationalHolidays.isHoliday(LocalDate(2026, 6, 15)))
    }

    @Test
    fun `all nine fixed holidays are recognised for 2026`() {
        val fixedHolidays = listOf(
            LocalDate(2026, 1, 1),
            LocalDate(2026, 4, 21),
            LocalDate(2026, 5, 1),
            LocalDate(2026, 9, 7),
            LocalDate(2026, 10, 12),
            LocalDate(2026, 11, 2),
            LocalDate(2026, 11, 15),
            LocalDate(2026, 11, 20),
            LocalDate(2026, 12, 25)
        )
        fixedHolidays.forEach { date ->
            assertTrue(BrazilianNationalHolidays.isHoliday(date), "$date should be a holiday")
        }
    }
}
