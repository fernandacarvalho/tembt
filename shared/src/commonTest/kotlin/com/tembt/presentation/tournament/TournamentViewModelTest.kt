package com.tembt.presentation.tournament

import com.tembt.domain.model.Tournament
import com.tembt.domain.model.TournamentStatus
import com.tembt.domain.usecase.GetTournamentsUseCase
import com.tembt.fake.FakeTournamentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class TournamentViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var repo: FakeTournamentRepository

    private val singleTournament = listOf(
        Tournament(
            id = 42,
            name = "Open Copacabana",
            venue = "Arena Atlântica",
            city = "Rio de Janeiro",
            startDate = LocalDate(2026, 4, 12),
            endDate = LocalDate(2026, 4, 13),
            registrationDeadline = LocalDate(2026, 4, 10),
            status = TournamentStatus.OPEN,
            priceMain = 89.90,
            categories = listOf("Masculino B", "Misto B"),
            registrationUrl = "https://torneioja.com.br/inscricao/42"
        )
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeTournamentRepository()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun createViewModel() = TournamentViewModel(GetTournamentsUseCase(repo))

    // --- Initial state ---

    @Test
    fun `given init before coroutines advance state is Loading`() = runTest(testDispatcher) {
        // Arrange
        repo.willReturn(Result.success(singleTournament))

        // Act
        val vm = createViewModel()

        // Assert
        assertIs<TournamentUiState.Loading>(vm.uiState.value)
    }

    @Test
    fun `given tournaments succeed on init state is Ready`() = runTest(testDispatcher) {
        // Arrange
        repo.willReturn(Result.success(singleTournament))

        // Act
        val vm = createViewModel()
        advanceUntilIdle()

        // Assert
        val state = assertIs<TournamentUiState.Ready>(vm.uiState.value)
        assertEquals(singleTournament, state.tournaments)
    }

    // --- load() ---

    @Test
    fun `when load succeeds with tournaments state is Ready`() = runTest(testDispatcher) {
        repo.willReturn(Result.success(singleTournament))
        val vm = createViewModel()
        advanceUntilIdle()

        assertIs<TournamentUiState.Ready>(vm.uiState.value)
    }

    @Test
    fun `when load succeeds with empty list state is Empty`() = runTest(testDispatcher) {
        repo.willReturn(Result.success(emptyList()))
        val vm = createViewModel()
        advanceUntilIdle()

        assertIs<TournamentUiState.Empty>(vm.uiState.value)
    }

    @Test
    fun `when load fails state is Error with message`() = runTest(testDispatcher) {
        repo.willReturn(Result.failure(RuntimeException("Sem conexão")))
        val vm = createViewModel()
        advanceUntilIdle()

        val state = assertIs<TournamentUiState.Error>(vm.uiState.value)
        assertEquals("Sem conexão", state.message)
    }

    @Test
    fun `when load fails without message state shows fallback error`() = runTest(testDispatcher) {
        repo.willReturn(Result.failure(RuntimeException()))
        val vm = createViewModel()
        advanceUntilIdle()

        val state = assertIs<TournamentUiState.Error>(vm.uiState.value)
        assertEquals("Erro ao carregar torneios.", state.message)
    }

    // --- City forwarding ---

    @Test
    fun `given no city passed to load repository receives default city`() = runTest(testDispatcher) {
        repo.willReturn(Result.success(emptyList()))
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(GetTournamentsUseCase.DEFAULT_CITY, repo.lastCity)
    }

    @Test
    fun `given city passed to load repository receives that city`() = runTest(testDispatcher) {
        repo.willReturn(Result.success(emptyList()))
        val vm = createViewModel()
        advanceUntilIdle()

        vm.load(city = "Niterói")
        advanceUntilIdle()

        assertEquals("Niterói", repo.lastCity)
    }

    // --- Reload ---

    @Test
    fun `given state is Ready when load called state transitions to Loading`() = runTest(testDispatcher) {
        // Arrange — advance to Ready first
        repo.willReturn(Result.success(singleTournament))
        val vm = createViewModel()
        advanceUntilIdle()
        assertIs<TournamentUiState.Ready>(vm.uiState.value)

        // Act
        vm.load()

        // Assert — before advancing, coroutine sets Loading synchronously
        assertIs<TournamentUiState.Loading>(vm.uiState.value)
    }

    @Test
    fun `given state is Ready when load called and succeeds state returns to Ready`() = runTest(testDispatcher) {
        // Arrange — advance to Ready first
        repo.willReturn(Result.success(singleTournament))
        val vm = createViewModel()
        advanceUntilIdle()
        assertIs<TournamentUiState.Ready>(vm.uiState.value)

        // Act
        vm.load()
        advanceUntilIdle()

        // Assert
        assertIs<TournamentUiState.Ready>(vm.uiState.value)
    }
}
