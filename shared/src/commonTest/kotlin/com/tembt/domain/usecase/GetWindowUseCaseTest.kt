package com.tembt.domain.usecase

import com.tembt.domain.model.ScheduleWindow
import com.tembt.fake.FakeWindowRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetWindowUseCaseTest {

    private val windowRepo = FakeWindowRepository()
    private val useCase    = GetWindowUseCase(windowRepo)

    @Test
    fun `given repository succeeds returns window`() = runTest {
        val window = ScheduleWindow(date = "2026-03-28", startHour = 8, endHour = 17, slots = emptyList())
        windowRepo.willReturnWindow(Result.success(window))

        val result = useCase()

        assertEquals(window, result.getOrNull())
    }

    @Test
    fun `given repository succeeds delegates to repository`() = runTest {
        useCase()

        assertEquals(1, windowRepo.getWindowCallCount)
    }

    @Test
    fun `given repository fails propagates failure`() = runTest {
        windowRepo.willReturnWindow(Result.failure(RuntimeException("Network error")))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
