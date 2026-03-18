package com.tembt.domain.usecase

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsCourtOpenUseCaseTest {

    private val useCase = IsCourtOpenUseCase()

    // --- Weekends ---

    @Test
    fun `given Saturday court is open`() {
        // 2026-03-14 is a Saturday
        assertTrue(useCase(LocalDate(2026, 3, 14)))
    }

    @Test
    fun `given Sunday court is open`() {
        // 2026-03-15 is a Sunday
        assertTrue(useCase(LocalDate(2026, 3, 15)))
    }

    @Test
    fun `given Monday court is closed`() {
        // 2026-03-16 is a Monday
        assertFalse(useCase(LocalDate(2026, 3, 16)))
    }

    @Test
    fun `given Wednesday court is closed`() {
        // 2026-03-18 is a Wednesday
        assertFalse(useCase(LocalDate(2026, 3, 18)))
    }

    @Test
    fun `given Friday court is closed`() {
        // 2026-03-20 is a Friday
        assertFalse(useCase(LocalDate(2026, 3, 20)))
    }

    // --- Fixed holidays ---

    @Test
    fun `given January 1st court is open`() {
        assertTrue(useCase(LocalDate(2026, 1, 1)))
    }

    @Test
    fun `given April 21st Tiradentes court is open`() {
        assertTrue(useCase(LocalDate(2026, 4, 21)))
    }

    @Test
    fun `given May 1st Dia do Trabalho court is open`() {
        assertTrue(useCase(LocalDate(2026, 5, 1)))
    }

    @Test
    fun `given September 7th Independencia court is open`() {
        assertTrue(useCase(LocalDate(2026, 9, 7)))
    }

    @Test
    fun `given October 12th Nossa Senhora Aparecida court is open`() {
        assertTrue(useCase(LocalDate(2026, 10, 12)))
    }

    @Test
    fun `given November 2nd Finados court is open`() {
        assertTrue(useCase(LocalDate(2026, 11, 2)))
    }

    @Test
    fun `given November 15th Proclamacao da Republica court is open`() {
        assertTrue(useCase(LocalDate(2026, 11, 15)))
    }

    @Test
    fun `given November 20th Consciencia Negra court is open`() {
        assertTrue(useCase(LocalDate(2026, 11, 20)))
    }

    @Test
    fun `given December 25th Natal court is open`() {
        assertTrue(useCase(LocalDate(2026, 12, 25)))
    }

    // --- Easter-based holidays for 2026 ---
    // Easter 2026: April 5

    @Test
    fun `given Sexta-feira Santa 2026 court is open`() {
        // Easter 2026-04-05, Good Friday = 2026-04-03
        assertTrue(useCase(LocalDate(2026, 4, 3)))
    }

    @Test
    fun `given Segunda de Carnaval 2026 court is open`() {
        // Easter 2026-04-05 minus 48 days = 2026-02-16
        assertTrue(useCase(LocalDate(2026, 2, 16)))
    }

    @Test
    fun `given Terca de Carnaval 2026 court is open`() {
        // Easter 2026-04-05 minus 47 days = 2026-02-17
        assertTrue(useCase(LocalDate(2026, 2, 17)))
    }

    @Test
    fun `given Corpus Christi 2026 court is open`() {
        // Easter 2026-04-05 plus 60 days = 2026-06-04
        assertTrue(useCase(LocalDate(2026, 6, 4)))
    }
}
