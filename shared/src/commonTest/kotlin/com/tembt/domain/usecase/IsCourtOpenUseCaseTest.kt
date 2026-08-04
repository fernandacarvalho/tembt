package com.tembt.domain.usecase

import com.tembt.domain.model.ScheduleWindow
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsCourtOpenUseCaseTest {

    private val useCase = IsCourtOpenUseCase()

    private fun window(date: String, startHour: Int = 6, endHour: Int = 16) =
        ScheduleWindow(date = date, startHour = startHour, endHour = endHour, slots = emptyList())

    @Test
    fun `given today matches session date and nowHour within window court is open`() {
        val today = LocalDate(2026, 3, 28)
        assertTrue(useCase(today, nowHour = 10, window = window("2026-03-28")))
    }

    @Test
    fun `given today does not match session date court is closed`() {
        val today = LocalDate(2026, 3, 28)
        assertFalse(useCase(today, nowHour = 10, window = window("2026-03-29")))
    }

    @Test
    fun `given session date is yesterday court is closed`() {
        val today = LocalDate(2026, 3, 28)
        assertFalse(useCase(today, nowHour = 10, window = window("2026-03-27")))
    }

    @Test
    fun `given session date is in a different month court is closed`() {
        val today = LocalDate(2026, 3, 28)
        assertFalse(useCase(today, nowHour = 10, window = window("2026-04-28")))
    }

    @Test
    fun `given nowHour is before same-day closing hour court is open`() {
        val today = LocalDate(2026, 3, 28)
        assertTrue(useCase(today, nowHour = 15, window = window("2026-03-28", startHour = 6, endHour = 16)))
    }

    @Test
    fun `given nowHour is at same-day closing hour court is closed`() {
        val today = LocalDate(2026, 3, 28)
        assertFalse(useCase(today, nowHour = 16, window = window("2026-03-28", startHour = 6, endHour = 16)))
    }

    @Test
    fun `given nowHour is past same-day closing hour court is closed`() {
        val today = LocalDate(2026, 3, 28)
        assertFalse(useCase(today, nowHour = 18, window = window("2026-03-28", startHour = 6, endHour = 16)))
    }

    @Test
    fun `given overnight window and nowHour is before midnight court is open`() {
        val today = LocalDate(2026, 3, 28)
        assertTrue(useCase(today, nowHour = 23, window = window("2026-03-28", startHour = 22, endHour = 2)))
    }

    @Test
    fun `given overnight window and nowHour is after midnight before closing court is open`() {
        val today = LocalDate(2026, 3, 28)
        assertTrue(useCase(today, nowHour = 1, window = window("2026-03-28", startHour = 22, endHour = 2)))
    }

    @Test
    fun `given overnight window and nowHour is at closing hour court is closed`() {
        val today = LocalDate(2026, 3, 28)
        assertFalse(useCase(today, nowHour = 2, window = window("2026-03-28", startHour = 22, endHour = 2)))
    }
}
