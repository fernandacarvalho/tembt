package com.tembt.presentation.map

import app.cash.turbine.test
import com.tembt.domain.model.LocationPermissionStatus
import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.model.Player
import com.tembt.domain.usecase.GetCourtLocationUseCase
import com.tembt.domain.usecase.GetPlayersAtCourtUseCase
import com.tembt.fake.FakeCourtRepository
import com.tembt.fake.FakeLocationService
import com.tembt.fake.FakePlayersRepository
import com.tembt.fake.FakeSendLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var locationService: FakeLocationService
    private lateinit var courtRepo: FakeCourtRepository
    private lateinit var playersRepo: FakePlayersRepository
    private lateinit var sendLocation: FakeSendLocation

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        locationService = FakeLocationService()
        courtRepo = FakeCourtRepository()
        playersRepo = FakePlayersRepository()
        sendLocation = FakeSendLocation()
    }

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private fun createViewModel() = MapViewModel(
        locationService = locationService,
        getCourtLocation = GetCourtLocationUseCase(courtRepo),
        getPlayersAtCourt = GetPlayersAtCourtUseCase(playersRepo),
        sendLocationUseCase = sendLocation
    )

    // --- Permission checks ---

    @Test
    fun `given permission NOT_DETERMINED on init state is PermissionRequired with NOT_DETERMINED`() =
        runTest(testDispatcher) {
            locationService.stubbedStatus = LocationPermissionStatus.NOT_DETERMINED

            val vm = createViewModel()

            assertIs<MapUiState.PermissionRequired>(vm.uiState.value)
            assertEquals(
                LocationPermissionStatus.NOT_DETERMINED,
                (vm.uiState.value as MapUiState.PermissionRequired).status
            )
        }

    @Test
    fun `given permission DENIED on init state is PermissionRequired with DENIED`() =
        runTest(testDispatcher) {
            locationService.stubbedStatus = LocationPermissionStatus.DENIED

            val vm = createViewModel()

            assertIs<MapUiState.PermissionRequired>(vm.uiState.value)
            assertEquals(
                LocationPermissionStatus.DENIED,
                (vm.uiState.value as MapUiState.PermissionRequired).status
            )
        }

    // --- Successful map load ---

    @Test
    fun `given permission GRANTED and court succeeds state transitions to MapReady with center`() =
        runTest(testDispatcher) {
            val center = MapCoordinates(latitude =-23.0, longitude =-46.0)
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            courtRepo.willReturn(Result.success(center))
            playersRepo.willReturn(Result.success(emptyList()))

            val vm = createViewModel()
            advanceUntilIdle()

            val state = assertIs<MapUiState.MapReady>(vm.uiState.value)
            assertEquals(center, state.center)
        }

    @Test
    fun `given permission GRANTED court and players succeed state is MapReady with center and players`() =
        runTest(testDispatcher) {
            val center = MapCoordinates(latitude =-23.0, longitude =-46.0)
            val players = listOf(
                Player(uuid = "p1", name = "Alice", lat = -23.0, lng = -46.0),
                Player(uuid = "p2", name = "Bob", lat = -23.1, lng = -46.1)
            )
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            courtRepo.willReturn(Result.success(center))
            playersRepo.willReturn(Result.success(players))

            val vm = createViewModel()
            advanceUntilIdle()

            val state = assertIs<MapUiState.MapReady>(vm.uiState.value)
            assertEquals(center, state.center)
            assertEquals(players, state.players)
        }

    // --- Error states ---

    @Test
    fun `given permission GRANTED and court fetch fails state is Error`() =
        runTest(testDispatcher) {
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            courtRepo.willReturn(Result.failure(RuntimeException("Network error")))

            val vm = createViewModel()
            advanceUntilIdle()

            val state = assertIs<MapUiState.Error>(vm.uiState.value)
            assertEquals("Network error", state.message)
        }

    @Test
    fun `given court succeeds but players fail state is MapReady with empty players silent failure`() =
        runTest(testDispatcher) {
            val center = MapCoordinates(latitude =-23.0, longitude =-46.0)
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            courtRepo.willReturn(Result.success(center))
            playersRepo.willReturn(Result.failure(RuntimeException("Players unavailable")))

            val vm = createViewModel()
            advanceUntilIdle()

            val state = assertIs<MapUiState.MapReady>(vm.uiState.value)
            assertEquals(center, state.center)
            assertTrue(state.players.isEmpty())
        }

    // --- No-duplicate fetch when already MapReady ---

    @Test
    fun `given state is already MapReady when onResume called court is not re-fetched`() =
        runTest(testDispatcher) {
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            courtRepo.willReturn(Result.success(MapCoordinates(-23.0, -46.0)))

            val vm = createViewModel()
            advanceUntilIdle()
            assertIs<MapUiState.MapReady>(vm.uiState.value)

            val callsBefore = courtRepo.callCount
            vm.onResume()
            advanceUntilIdle()

            assertEquals(callsBefore, courtRepo.callCount)
        }

    // --- Refresh players ---

    @Test
    fun `given state is MapReady when refreshPlayers called players list is updated`() =
        runTest(testDispatcher) {
            val center = MapCoordinates(latitude =-23.0, longitude =-46.0)
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            courtRepo.willReturn(Result.success(center))
            playersRepo.willReturn(Result.success(emptyList()))

            val vm = createViewModel()
            advanceUntilIdle()
            assertIs<MapUiState.MapReady>(vm.uiState.value)

            val updatedPlayers = listOf(Player("p1", "Alice", -23.0, -46.0))
            playersRepo.willReturn(Result.success(updatedPlayers))

            vm.refreshPlayers()
            advanceUntilIdle()

            val state = assertIs<MapUiState.MapReady>(vm.uiState.value)
            assertEquals(updatedPlayers, state.players)
        }

    @Test
    fun `given state is not MapReady when refreshPlayers called state is unchanged`() =
        runTest(testDispatcher) {
            locationService.stubbedStatus = LocationPermissionStatus.NOT_DETERMINED
            val vm = createViewModel()

            val stateBefore = vm.uiState.value
            vm.refreshPlayers()
            advanceUntilIdle()

            assertEquals(stateBefore, vm.uiState.value)
        }

    // --- Permission result callback ---

    @Test
    fun `when onPermissionResult called marks permission requested and re-checks permission`() =
        runTest(testDispatcher) {
            locationService.stubbedStatus = LocationPermissionStatus.NOT_DETERMINED
            val vm = createViewModel()

            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            courtRepo.willReturn(Result.success(MapCoordinates(-23.0, -46.0)))
            vm.onPermissionResult()
            advanceUntilIdle()

            assertEquals(1, locationService.markPermissionRequestedCallCount)
            assertIs<MapUiState.MapReady>(vm.uiState.value)
        }

    // --- Events ---

    @Test
    fun `when onOpenSettingsRequested called OpenAppSettings event is emitted`() =
        runTest(testDispatcher) {
            locationService.stubbedStatus = LocationPermissionStatus.DENIED
            val vm = createViewModel()

            vm.uiEvent.test {
                vm.onOpenSettingsRequested()
                advanceUntilIdle()
                assertEquals(MapUiEvent.OpenAppSettings, awaitItem())
            }
        }

    // --- sendLocation ---

    @Test
    fun `when sendLocation called delegates to SendLocation use case`() =
        runTest(testDispatcher) {
            locationService.stubbedStatus = LocationPermissionStatus.DENIED
            val vm = createViewModel()

            vm.sendLocation()
            advanceUntilIdle()

            assertEquals(1, sendLocation.callCount)
        }

    @Test
    fun `when sendLocation fails UI state is unchanged`() =
        runTest(testDispatcher) {
            locationService.stubbedStatus = LocationPermissionStatus.DENIED
            sendLocation.result = Result.failure(RuntimeException("Location unavailable"))
            val vm = createViewModel()
            val stateBefore = vm.uiState.value

            vm.sendLocation()
            advanceUntilIdle()

            assertEquals(stateBefore, vm.uiState.value)
        }
}
