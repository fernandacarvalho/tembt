package com.tembt.domain.usecase

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IsCourtOpenUseCaseTest {

    private val useCase = IsCourtOpenUseCase()

    @Test
    fun `given today matches session date court is open`() {
        val today = LocalDate(2026, 3, 28)
        assertTrue(useCase(today, sessionDate = "2026-03-28"))
    }

    @Test
    fun `given today does not match session date court is closed`() {
        val today = LocalDate(2026, 3, 28)
        assertFalse(useCase(today, sessionDate = "2026-03-29"))
    }

    @Test
    fun `given session date is yesterday court is closed`() {
        val today = LocalDate(2026, 3, 28)
        assertFalse(useCase(today, sessionDate = "2026-03-27"))
    }

    @Test
    fun `given session date is in a different month court is closed`() {
        val today = LocalDate(2026, 3, 28)
        assertFalse(useCase(today, sessionDate = "2026-04-28"))
    }
}
