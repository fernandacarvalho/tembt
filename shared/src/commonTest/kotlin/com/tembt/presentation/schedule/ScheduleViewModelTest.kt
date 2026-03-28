package com.tembt.presentation.schedule

import com.tembt.domain.model.ScheduleWindow
import com.tembt.domain.model.SlotPlayer
import com.tembt.domain.model.WindowSlot
import kotlinx.coroutines.CompletableDeferred
import com.tembt.domain.usecase.CheckinUseCase
import com.tembt.domain.usecase.GetWindowUseCase
import com.tembt.fake.FakePlayerStorage
import com.tembt.fake.FakeWindowRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ScheduleViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var windowRepo: FakeWindowRepository
    private lateinit var playerStorage: FakePlayerStorage

    private val defaultWindow = ScheduleWindow(
        date = "2026-03-17",
        startHour = 6,
        endHour = 16,
        slots = listOf(
            WindowSlot(time = "08:00", players = emptyList()),
            WindowSlot(time = "10:00", players = emptyList())
        )
    )

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        windowRepo = FakeWindowRepository()
        playerStorage = FakePlayerStorage(uuid = "device-uuid-42")
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun createViewModel() = ScheduleViewModel(
        getWindow = GetWindowUseCase(windowRepo),
        checkin = CheckinUseCase(windowRepo),
        deviceIdentity = playerStorage
    )

    // --- Auto-load on init ---

    @Test
    fun `on init window is loaded automatically`() =
        runTest(testDispatcher) {
            windowRepo.willReturnWindow(Result.success(defaultWindow))
            val vm = createViewModel()
            advanceUntilIdle()

            val state = assertIs<ScheduleUiState.Ready>(vm.uiState.value)
            assertEquals(defaultWindow, state.window)
        }

    @Test
    fun `on init state starts as Loading before data arrives`() =
        runTest(testDispatcher) {
            windowRepo.willReturnWindow(Result.success(defaultWindow))
            val vm = createViewModel()

            // Coroutine not yet advanced — state should still be Loading
            assertIs<ScheduleUiState.Loading>(vm.uiState.value)
        }

    // --- loadWindow ---

    @Test
    fun `when loadWindow succeeds state is Ready with window`() =
        runTest(testDispatcher) {
            windowRepo.willReturnWindow(Result.success(defaultWindow))
            val vm = createViewModel()
            advanceUntilIdle()

            assertIs<ScheduleUiState.Ready>(vm.uiState.value)
        }

    @Test
    fun `when loadWindow fails state is Error with message`() =
        runTest(testDispatcher) {
            windowRepo.willReturnWindow(Result.failure(RuntimeException("Sem conexão")))
            val vm = createViewModel()
            advanceUntilIdle()

            val state = assertIs<ScheduleUiState.Error>(vm.uiState.value)
            assertEquals("Sem conexão", state.message)
        }

    @Test
    fun `when loadWindow fails without message state shows fallback error`() =
        runTest(testDispatcher) {
            windowRepo.willReturnWindow(Result.failure(RuntimeException()))
            val vm = createViewModel()
            advanceUntilIdle()

            val state = assertIs<ScheduleUiState.Error>(vm.uiState.value)
            assertEquals("Erro ao carregar agenda.", state.message)
        }

    // --- checkin ---

    @Test
    fun `given state is Ready when checkin succeeds checkedInSlotTime is set`() =
        runTest(testDispatcher) {
            windowRepo.willReturnWindow(Result.success(defaultWindow))
            windowRepo.willReturnCheckin(Result.success(Unit))
            val vm = createViewModel()
            advanceUntilIdle()

            vm.checkin("08:00")
            advanceUntilIdle()

            val state = assertIs<ScheduleUiState.Ready>(vm.uiState.value)
            assertEquals("08:00", state.checkedInSlotTime)
        }

    @Test
    fun `given checkin succeeds window is reloaded to reflect updated players`() =
        runTest(testDispatcher) {
            windowRepo.willReturnWindow(Result.success(defaultWindow))
            windowRepo.willReturnCheckin(Result.success(Unit))
            val vm = createViewModel()
            advanceUntilIdle()

            val callsBefore = windowRepo.getWindowCallCount
            vm.checkin("08:00")
            advanceUntilIdle()

            // loadWindow called again after checkin
            assertTrue(windowRepo.getWindowCallCount > callsBefore)
        }

    @Test
    fun `given checkin succeeds checkin request uses device uuid`() =
        runTest(testDispatcher) {
            windowRepo.willReturnWindow(Result.success(defaultWindow))
            windowRepo.willReturnCheckin(Result.success(Unit))
            val vm = createViewModel()
            advanceUntilIdle()

            vm.checkin("08:00")
            advanceUntilIdle()

            assertEquals("device-uuid-42", windowRepo.lastCheckinUuid)
            assertEquals("08:00", windowRepo.lastCheckinSlot)
        }

    @Test
    fun `given checkin fails isCheckingIn is reset to false`() =
        runTest(testDispatcher) {
            windowRepo.willReturnWindow(Result.success(defaultWindow))
            windowRepo.willReturnCheckin(Result.failure(RuntimeException("Server error")))
            val vm = createViewModel()
            advanceUntilIdle()

            vm.checkin("08:00")
            advanceUntilIdle()

            val state = assertIs<ScheduleUiState.Ready>(vm.uiState.value)
            assertFalse(state.isCheckingIn)
        }

    @Test
    fun `given checkin fails checkedInSlotTime remains null`() =
        runTest(testDispatcher) {
            windowRepo.willReturnWindow(Result.success(defaultWindow))
            windowRepo.willReturnCheckin(Result.failure(RuntimeException("Server error")))
            val vm = createViewModel()
            advanceUntilIdle()

            vm.checkin("08:00")
            advanceUntilIdle()

            val state = assertIs<ScheduleUiState.Ready>(vm.uiState.value)
            assertNull(state.checkedInSlotTime)
        }

    @Test
    fun `given state is not Ready when checkin called it is ignored`() =
        runTest(testDispatcher) {
            windowRepo.willReturnWindow(Result.failure(RuntimeException("Error")))
            val vm = createViewModel()
            advanceUntilIdle()
            assertIs<ScheduleUiState.Error>(vm.uiState.value)

            vm.checkin("08:00")
            advanceUntilIdle()

            assertEquals(0, windowRepo.checkinCallCount)
        }

    @Test
    fun `given checkin already in progress second checkin call is ignored`() =
        runTest(testDispatcher) {
            // Arrange — deferred suspends checkin so isCheckingIn stays true between calls
            windowRepo.willReturnWindow(Result.success(defaultWindow))
            windowRepo.checkinDeferred = CompletableDeferred()
            val vm = createViewModel()
            advanceUntilIdle()

            // Act — first checkin starts and suspends at the repo call (isCheckingIn = true)
            vm.checkin("08:00")
            runCurrent() // advances coroutine until it suspends on checkinDeferred.await()

            // Second call while first is in-flight — must be ignored
            vm.checkin("10:00")

            // Complete the first checkin and let everything finish
            windowRepo.checkinDeferred!!.complete(Result.success(Unit))
            advanceUntilIdle()

            // Assert — only one checkin reached the repository
            assertEquals(1, windowRepo.checkinCallCount)
        }

    // --- Window contents ---

    @Test
    fun `given window has slots with players Ready state reflects all slot data`() =
        runTest(testDispatcher) {
            val window = ScheduleWindow(
                date = "2026-03-17",
                startHour = 6,
                endHour = 16,
                slots = listOf(
                    WindowSlot(
                        time = "08:00",
                        players = listOf(SlotPlayer("Alice"), SlotPlayer("Bob"))
                    )
                )
            )
            windowRepo.willReturnWindow(Result.success(window))
            val vm = createViewModel()
            advanceUntilIdle()

            val state = assertIs<ScheduleUiState.Ready>(vm.uiState.value)
            assertEquals(1, state.window.slots.size)
            assertEquals(2, state.window.slots[0].players.size)
        }
}
