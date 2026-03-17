package com.tembt.domain.usecase

import com.tembt.fake.FakePlayerRepository
import com.tembt.fake.FakePlayerStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RegisterPlayerUseCaseTest {

    private val playerRepo = FakePlayerRepository()
    private val playerStorage = FakePlayerStorage(uuid = "device-uuid-99")

    private val useCase = RegisterPlayerUseCase(playerRepo, playerStorage)

    @Test
    fun `when registration succeeds result is success`() = runTest {
        playerRepo.willReturn(Result.success(Unit))

        val result = useCase("Alice")

        assertTrue(result.isSuccess)
    }

    @Test
    fun `when registration succeeds device uuid is passed to repository`() = runTest {
        playerRepo.willReturn(Result.success(Unit))

        useCase("Alice")

        assertEquals("device-uuid-99", playerRepo.lastUuid)
    }

    @Test
    fun `when registration succeeds trimmed name is passed to repository`() = runTest {
        playerRepo.willReturn(Result.success(Unit))

        useCase("Alice")

        assertEquals("Alice", playerRepo.lastName)
    }

    @Test
    fun `when registration succeeds registration is saved in storage`() = runTest {
        playerRepo.willReturn(Result.success(Unit))

        useCase("Alice")

        assertTrue(playerStorage.registered)
        assertEquals("Alice", playerStorage.savedName)
    }

    @Test
    fun `when registration fails result is failure`() = runTest {
        playerRepo.willReturn(Result.failure(RuntimeException("API error")))

        val result = useCase("Alice")

        assertTrue(result.isFailure)
        assertEquals("API error", result.exceptionOrNull()?.message)
    }

    @Test
    fun `when registration fails storage is not updated`() = runTest {
        playerRepo.willReturn(Result.failure(RuntimeException("API error")))

        useCase("Alice")

        assertFalse(playerStorage.registered)
        assertNull(playerStorage.savedName)
    }

    @Test
    fun `when registration fails repository was still called once`() = runTest {
        playerRepo.willReturn(Result.failure(RuntimeException("API error")))

        useCase("Alice")

        assertEquals(1, playerRepo.callCount)
    }
}
