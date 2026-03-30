package com.tembt.domain.usecase

import com.tembt.domain.model.Player
import com.tembt.fake.FakePlayersRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetPlayersAtCourtUseCaseTest {

    private val playersRepo = FakePlayersRepository()
    private val useCase     = GetPlayersAtCourtUseCase(playersRepo)

    @Test
    fun `given repository succeeds returns player list`() = runTest {
        val players = listOf(Player("Alice", -22.9, -43.1))
        playersRepo.willReturn(Result.success(players))

        val result = useCase()

        assertEquals(players, result.getOrNull())
    }

    @Test
    fun `given repository succeeds delegates to repository`() = runTest {
        playersRepo.willReturn(Result.success(emptyList()))

        useCase()

        assertEquals(1, playersRepo.callCount)
    }

    @Test
    fun `given repository fails propagates failure`() = runTest {
        playersRepo.willReturn(Result.failure(RuntimeException("Network error")))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
