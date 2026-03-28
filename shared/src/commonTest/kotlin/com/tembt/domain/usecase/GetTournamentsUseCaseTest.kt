package com.tembt.domain.usecase

import com.tembt.domain.model.Tournament
import com.tembt.domain.model.TournamentStatus
import com.tembt.fake.FakeTournamentRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetTournamentsUseCaseTest {

    private val repo = FakeTournamentRepository()
    private val useCase = GetTournamentsUseCase(repo)

    // --- City ---

    @Test
    fun `given no city default city Rio de Janeiro is forwarded to repository`() = runTest {
        useCase(city = null)

        assertEquals(GetTournamentsUseCase.DEFAULT_CITY, repo.lastCity)
    }

    @Test
    fun `given a city it is forwarded to repository`() = runTest {
        useCase(city = "Niterói")

        assertEquals("Niterói", repo.lastCity)
    }

    // --- Date range ---

    @Test
    fun `from date is today in Sao Paulo timezone`() = runTest {
        val today = Clock.System.todayIn(TimeZone.of("America/Sao_Paulo"))

        useCase()

        assertEquals(today, repo.lastFrom)
    }

    @Test
    fun `until date is today plus 40 days`() = runTest {
        val today = Clock.System.todayIn(TimeZone.of("America/Sao_Paulo"))
        val expected = today.plus(GetTournamentsUseCase.WINDOW_DAYS, DateTimeUnit.DAY)

        useCase()

        assertEquals(expected, repo.lastUntil)
    }

    // --- Result passthrough ---

    @Test
    fun `when repository succeeds result is returned as-is`() = runTest {
        val tournaments = listOf(fakeTournament(id = 1), fakeTournament(id = 2))
        repo.willReturn(Result.success(tournaments))

        val result = useCase()

        assertTrue(result.isSuccess)
        assertEquals(tournaments, result.getOrThrow())
    }

    @Test
    fun `when repository fails error is returned as-is`() = runTest {
        repo.willReturn(Result.failure(RuntimeException("Timeout")))

        val result = useCase()

        assertTrue(result.isFailure)
        assertEquals("Timeout", result.exceptionOrNull()?.message)
    }

    // --- Status filter ---

    @Test
    fun `tournaments with CLOSED status are filtered out`() = runTest {
        val open = fakeTournament(id = 1, status = TournamentStatus.OPEN)
        val closed = fakeTournament(id = 2, status = TournamentStatus.CLOSED)
        val confirmed = fakeTournament(id = 3, status = TournamentStatus.CONFIRMED)
        repo.willReturn(Result.success(listOf(open, closed, confirmed)))

        val result = useCase().getOrThrow()

        assertEquals(listOf(open, confirmed), result)
    }

    @Test
    fun `given all tournaments are closed result is empty list`() = runTest {
        repo.willReturn(Result.success(listOf(
            fakeTournament(id = 1, status = TournamentStatus.CLOSED),
            fakeTournament(id = 2, status = TournamentStatus.CLOSED)
        )))

        val result = useCase().getOrThrow()

        assertTrue(result.isEmpty())
    }
}

private fun fakeTournament(id: Int, status: TournamentStatus = TournamentStatus.OPEN) = Tournament(
    id = id,
    name = "Torneio $id",
    venue = "Arena $id",
    city = "Rio de Janeiro",
    startDate = LocalDate(2026, 4, 10),
    endDate = LocalDate(2026, 4, 11),
    registrationDeadline = LocalDate(2026, 4, 8),
    status = status,
    priceMain = 89.90,
    categories = listOf("Feminino B", "Masculino B"),
    registrationUrl = "https://torneioja.com.br/inscricao/$id"
)
