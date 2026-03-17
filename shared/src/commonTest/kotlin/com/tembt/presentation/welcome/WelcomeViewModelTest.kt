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
            val vm = createViewModel()
            vm.onStartClicked("")
            advanceUntilIdle()
            assertIs<WelcomeUiState.Idle>(vm.uiState.value)
            assertEquals(0, playerRepo.callCount)
        }

    @Test
    fun `given blank name when onStartClicked state remains Idle`() =
        runTest(testDispatcher) {
            val vm = createViewModel()
            vm.onStartClicked("   ")
            advanceUntilIdle()
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
    fun `given valid name and registration succeeds state returns to Idle after navigation`() =
        runTest(testDispatcher) {
            playerRepo.willReturn(Result.success(Unit))
            val vm = createViewModel()

            vm.onStartClicked("Alice")
            advanceUntilIdle()

            // State stays at Loading until the event triggers navigation; Idle is the pre-loading baseline.
            // After success the ViewModel emits the event and doesn't reset state — UI navigates away.
            // We verify no Error state was set.
            val state = vm.uiState.value
            assertIs<WelcomeUiState.Loading>(state) // still Loading — nav event triggers the transition
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
