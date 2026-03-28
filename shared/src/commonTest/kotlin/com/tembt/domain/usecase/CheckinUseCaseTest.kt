package com.tembt.domain.usecase

import com.tembt.fake.FakeWindowRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CheckinUseCaseTest {

    private val windowRepo = FakeWindowRepository()
    private val useCase    = CheckinUseCase(windowRepo)

    @Test
    fun `given valid args forwards playerUuid and slotTime to repository`() = runTest {
        useCase("player-uuid", "08:00")

        assertEquals("player-uuid", windowRepo.lastCheckinUuid)
        assertEquals("08:00", windowRepo.lastCheckinSlot)
    }

    @Test
    fun `given repository succeeds returns success`() = runTest {
        windowRepo.willReturnCheckin(Result.success(Unit))

        val result = useCase("player-uuid", "08:00")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `given repository fails propagates failure`() = runTest {
        windowRepo.willReturnCheckin(Result.failure(RuntimeException("Network error")))

        val result = useCase("player-uuid", "08:00")

        assertTrue(result.isFailure)
    }
}
