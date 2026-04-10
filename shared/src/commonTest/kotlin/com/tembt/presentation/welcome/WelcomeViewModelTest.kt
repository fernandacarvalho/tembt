package com.tembt.presentation.welcome

import app.cash.turbine.test
import com.tembt.domain.usecase.RegisterPlayerUseCase
import com.tembt.fake.FakePlayerRepository
import com.tembt.fake.FakePlayerStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var playerRepo: FakePlayerRepository
    private lateinit var playerStorage: FakePlayerStorage

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        playerRepo = FakePlayerRepository()
        playerStorage = FakePlayerStorage()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun createViewModel() = WelcomeViewModel(
        registerPlayerUseCase = RegisterPlayerUseCase(playerRepo, playerStorage)
    )

    // --- Input validation ---

    @Test
    fun `given empty name when onStartClicked state remains Idle`() =
        runTest(testDispatcher) {
            // Arrange
            val vm = createViewModel()

            // Act
            vm.onStartClicked("")
            advanceUntilIdle()

            // Assert
            assertIs<WelcomeUiState.Idle>(vm.uiState.value)
            assertEquals(0, playerRepo.callCount)
        }

    @Test
    fun `given blank name when onStartClicked state remains Idle`() =
        runTest(testDispatcher) {
            // Arrange
            val vm = createViewModel()

            // Act
            vm.onStartClicked("   ")
            advanceUntilIdle()

            // Assert
            assertIs<WelcomeUiState.Idle>(vm.uiState.value)
            assertEquals(0, playerRepo.callCount)
        }

    @Test
    fun `name is trimmed before being passed to repository`() =
        runTest(testDispatcher) {
            playerRepo.willReturn(Result.success(Unit))
            val vm = createViewModel()

            vm.onStartClicked("  Alice  ")
            advanceUntilIdle()

            assertEquals("Alice", playerRepo.lastName)
        }

    // --- Success path ---

    @Test
    fun `given valid name and registration succeeds NavigateToMap event is emitted`() =
        runTest(testDispatcher) {
            playerRepo.willReturn(Result.success(Unit))
            val vm = createViewModel()

            vm.uiEvent.test {
                vm.onStartClicked("Alice")
                advanceUntilIdle()
                assertEquals(WelcomeUiEvent.NavigateToMap, awaitItem())
            }
        }

    @Test
    fun `given valid name and registration succeeds state remains Loading while navigation event is emitted`() =
        runTest(testDispatcher) {
            // Arrange
            playerRepo.willReturn(Result.success(Unit))
            val vm = createViewModel()

            // Act
            vm.onStartClicked("Alice")
            advanceUntilIdle()

            // Assert — VM emits the navigation event and does not reset state; UI navigates away
            assertIs<WelcomeUiState.Loading>(vm.uiState.value)
        }

    // --- Error path ---

    @Test
    fun `given registration fails state is Error with failure message`() =
        runTest(testDispatcher) {
            playerRepo.willReturn(Result.failure(RuntimeException("Servidor indisponível")))
            val vm = createViewModel()

            vm.onStartClicked("Alice")
            advanceUntilIdle()

            val state = assertIs<WelcomeUiState.Error>(vm.uiState.value)
            assertEquals("Servidor indisponível", state.message)
        }

    @Test
    fun `given registration fails without message state shows fallback error message`() =
        runTest(testDispatcher) {
            playerRepo.willReturn(Result.failure(RuntimeException()))
            val vm = createViewModel()

            vm.onStartClicked("Alice")
            advanceUntilIdle()

            val state = assertIs<WelcomeUiState.Error>(vm.uiState.value)
            assertEquals("Erro ao registrar. Tente novamente.", state.message)
        }

    @Test
    fun `given state is Error when onStartClicked called registration is attempted again`() =
        runTest(testDispatcher) {
            // Arrange — first attempt fails, leaves state in Error
            playerRepo.willReturn(Result.failure(RuntimeException("Server error")))
            val vm = createViewModel()
            vm.onStartClicked("Alice")
            advanceUntilIdle()
            assertIs<WelcomeUiState.Error>(vm.uiState.value)
            val callsAfterFirstAttempt = playerRepo.callCount

            // Act — second attempt from Error state (guard only blocks Loading, not Error)
            playerRepo.willReturn(Result.success(Unit))
            vm.onStartClicked("Alice")
            advanceUntilIdle()

            // Assert — repository was called again (not blocked by guard)
            assertEquals(callsAfterFirstAttempt + 1, playerRepo.callCount)
        }

    // --- Debounce (double-tap prevention) ---

    @Test
    fun `given loading in progress when onStartClicked called again second call is ignored`() =
        runTest(testDispatcher) {
            playerRepo.willReturn(Result.success(Unit))
            val vm = createViewModel()

            // First click — launches coroutine (queued, not started yet)
            vm.onStartClicked("Alice")
            // Advance only to run the coroutine body up to first suspension (sets state to Loading)
            runCurrent()
            assertIs<WelcomeUiState.Loading>(vm.uiState.value)

            // Second click — should be ignored because state is Loading
            vm.onStartClicked("Bob")
            advanceUntilIdle()

            assertEquals(1, playerRepo.callCount)
            assertEquals("Alice", playerRepo.lastName)
        }
}
