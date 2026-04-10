package com.tembt.presentation.map

import app.cash.turbine.test
import com.tembt.domain.model.LocationPermissionStatus
import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.model.Player
import com.tembt.fake.FakeGetCourtLocation
import com.tembt.fake.FakeGetPlayersAtCourt
import com.tembt.fake.FakeLocationService
import com.tembt.fake.FakeSendLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var locationService: FakeLocationService
    private lateinit var getCourtLocation: FakeGetCourtLocation
    private lateinit var getPlayersAtCourt: FakeGetPlayersAtCourt
    private lateinit var sendLocation: FakeSendLocation
    private var latestVm: MapViewModel? = null

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        locationService = FakeLocationService()
        getCourtLocation = FakeGetCourtLocation()
        getPlayersAtCourt = FakeGetPlayersAtCourt()
        sendLocation = FakeSendLocation()
    }

    @AfterTest
    fun tearDown() {
        latestVm?.onPause()
        latestVm = null
        Dispatchers.resetMain()
    }

    private fun createViewModel(pollIntervalMs: Long = Long.MAX_VALUE) = MapViewModel(
        locationService = locationService,
        getCourtLocation = getCourtLocation,
        getPlayersAtCourt = getPlayersAtCourt,
        sendLocationUseCase = sendLocation,
        pollIntervalMs = pollIntervalMs
    ).also { latestVm = it }

    // --- Permission checks ---

    @Test
    fun `given permission NOT_DETERMINED on init state is PermissionRequired with NOT_DETERMINED`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.NOT_DETERMINED

            // Act
            val vm = createViewModel()

            // Assert
            assertIs<MapUiState.PermissionRequired>(vm.uiState.value)
            assertEquals(
                LocationPermissionStatus.NOT_DETERMINED,
                (vm.uiState.value as MapUiState.PermissionRequired).status
            )
        }

    @Test
    fun `given permission DENIED on init state is PermissionRequired with DENIED`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.DENIED

            // Act
            val vm = createViewModel()

            // Assert
            assertIs<MapUiState.PermissionRequired>(vm.uiState.value)
            assertEquals(
                LocationPermissionStatus.DENIED,
                (vm.uiState.value as MapUiState.PermissionRequired).status
            )
        }

    // --- Loading intermediate state ---

    @Test
    fun `given permission GRANTED before coroutines advance state is Loading`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(MapCoordinates(-23.0, -46.0)))
            getPlayersAtCourt.willReturn(Result.success(emptyList()))

            // Act
            val vm = createViewModel()

            // Assert — fetch coroutine is queued but not yet executed
            assertIs<MapUiState.Loading>(vm.uiState.value)
        }

    // --- Successful map load ---

    @Test
    fun `given permission GRANTED and court succeeds state transitions to MapReady with center`() =
        runTest(testDispatcher) {
            // Arrange
            val center = MapCoordinates(latitude = -23.0, longitude = -46.0)
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(center))
            getPlayersAtCourt.willReturn(Result.success(emptyList()))

            // Act
            val vm = createViewModel()
            runCurrent()

            // Assert
            val state = assertIs<MapUiState.MapReady>(vm.uiState.value)
            assertEquals(center, state.center)
        }

    @Test
    fun `given permission GRANTED and court succeeds sendLocation is called once during court fetch`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(MapCoordinates(-23.0, -46.0)))
            getPlayersAtCourt.willReturn(Result.success(emptyList()))

            // Act
            val vm = createViewModel()
            runCurrent()

            // Assert — sendLocation is triggered inside fetchCourtAndShowMap after court succeeds
            assertEquals(1, sendLocation.callCount)
        }

    @Test
    fun `given permission GRANTED court and players succeed state is MapReady with center and players`() =
        runTest(testDispatcher) {
            // Arrange
            val center = MapCoordinates(latitude = -23.0, longitude = -46.0)
            val players = listOf(
                Player(name = "Alice", lat = -23.0, lng = -46.0),
                Player(name = "Bob", lat = -23.1, lng = -46.1)
            )
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(center))
            getPlayersAtCourt.willReturn(Result.success(players))

            // Act
            val vm = createViewModel()
            runCurrent()

            // Assert
            val state = assertIs<MapUiState.MapReady>(vm.uiState.value)
            assertEquals(center, state.center)
            assertEquals(players, state.players)
        }

    @Test
    fun `given court returns name state is MapReady with courtName`() =
        runTest(testDispatcher) {
            // Arrange
            val center = MapCoordinates(latitude = -23.0, longitude = -46.0, name = "Quadra Mario Beletti")
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(center))
            getPlayersAtCourt.willReturn(Result.success(emptyList()))

            // Act
            val vm = createViewModel()
            runCurrent()

            // Assert
            val state = assertIs<MapUiState.MapReady>(vm.uiState.value)
            assertEquals("Quadra Mario Beletti", state.courtName)
        }

    @Test
    fun `given court returns no name state is MapReady with empty courtName`() =
        runTest(testDispatcher) {
            // Arrange
            val center = MapCoordinates(latitude = -23.0, longitude = -46.0)
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(center))
            getPlayersAtCourt.willReturn(Result.success(emptyList()))

            // Act
            val vm = createViewModel()
            runCurrent()

            // Assert
            val state = assertIs<MapUiState.MapReady>(vm.uiState.value)
            assertEquals("", state.courtName)
        }

    // --- lastUpdatedAt ---

    @Test
    fun `given court and players succeed lastUpdatedAt is set`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(MapCoordinates(-23.0, -46.0)))
            getPlayersAtCourt.willReturn(Result.success(emptyList()))

            // Act
            val vm = createViewModel()
            runCurrent()

            // Assert
            val state = assertIs<MapUiState.MapReady>(vm.uiState.value)
            assertNotNull(state.lastUpdatedAt)
        }

    @Test
    fun `given players fetch fails lastUpdatedAt remains null`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(MapCoordinates(-23.0, -46.0)))
            getPlayersAtCourt.willReturn(Result.failure(RuntimeException("Players unavailable")))

            // Act
            val vm = createViewModel()
            runCurrent()

            // Assert
            val state = assertIs<MapUiState.MapReady>(vm.uiState.value)
            assertNull(state.lastUpdatedAt)
        }

    // --- Error states ---

    @Test
    fun `given permission GRANTED and court fetch fails state is Error`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.failure(RuntimeException("Network error")))

            // Act
            val vm = createViewModel()
            runCurrent()

            // Assert
            val state = assertIs<MapUiState.Error>(vm.uiState.value)
            assertEquals("Network error", state.message)
            assertEquals(0, getPlayersAtCourt.callCount)
        }

    @Test
    fun `given court succeeds but players fail state is MapReady with empty players silent failure`() =
        runTest(testDispatcher) {
            // Arrange
            val center = MapCoordinates(latitude = -23.0, longitude = -46.0)
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(center))
            getPlayersAtCourt.willReturn(Result.failure(RuntimeException("Players unavailable")))

            // Act
            val vm = createViewModel()
            runCurrent()

            // Assert
            val state = assertIs<MapUiState.MapReady>(vm.uiState.value)
            assertEquals(center, state.center)
            assertTrue(state.players.isEmpty())
        }

    // --- Background permission required ---

    @Test
    fun `given foreground GRANTED but background not GRANTED on init state is BackgroundPermissionRequired`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            locationService.stubbedBackgroundStatus = LocationPermissionStatus.NOT_DETERMINED

            // Act
            val vm = createViewModel()

            // Assert
            assertIs<MapUiState.BackgroundPermissionRequired>(vm.uiState.value)
        }

    @Test
    fun `given BackgroundPermissionRequired when background is granted and onResume called state transitions to MapReady`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            locationService.stubbedBackgroundStatus = LocationPermissionStatus.NOT_DETERMINED
            getCourtLocation.willReturn(Result.success(MapCoordinates(-23.0, -46.0)))
            getPlayersAtCourt.willReturn(Result.success(emptyList()))
            val vm = createViewModel()

            // Act
            locationService.stubbedBackgroundStatus = LocationPermissionStatus.GRANTED
            vm.onResume()
            runCurrent()

            // Assert
            assertIs<MapUiState.MapReady>(vm.uiState.value)
        }

    // --- No-duplicate fetch when already MapReady ---

    @Test
    fun `given state is already MapReady when onResume called court is not re-fetched`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(MapCoordinates(-23.0, -46.0)))
            val vm = createViewModel()
            runCurrent()
            val callsBefore = getCourtLocation.callCount

            // Act
            vm.onResume()
            runCurrent()

            // Assert
            assertEquals(callsBefore, getCourtLocation.callCount)
        }

    @Test
    fun `given fetch is in flight when checkPermission called again court is not fetched twice`() =
        runTest(testDispatcher) {
            // Arrange — permission granted; fetch coroutine is queued but not yet started
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(MapCoordinates(-23.0, -46.0)))
            getPlayersAtCourt.willReturn(Result.success(emptyList()))
            val vm = createViewModel() // init queues the fetch job (isActive = true)

            // Act — trigger a second checkPermission while the first job is still active
            vm.onResume()
            runCurrent()

            // Assert — only one court fetch despite two checkPermission calls
            assertEquals(1, getCourtLocation.callCount)
        }

    @Test
    fun `given permission NOT_DETERMINED when onResume called and permission is now GRANTED state transitions to MapReady`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.NOT_DETERMINED
            getCourtLocation.willReturn(Result.success(MapCoordinates(-23.0, -46.0)))
            getPlayersAtCourt.willReturn(Result.success(emptyList()))
            val vm = createViewModel()

            // Act
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            vm.onResume()
            runCurrent()

            // Assert
            assertIs<MapUiState.MapReady>(vm.uiState.value)
        }

    // --- Refresh players ---

    @Test
    fun `given state is MapReady when refreshPlayers called players list is updated`() =
        runTest(testDispatcher) {
            // Arrange
            val center = MapCoordinates(latitude = -23.0, longitude = -46.0)
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(center))
            getPlayersAtCourt.willReturn(Result.success(emptyList()))
            val vm = createViewModel()
            runCurrent()
            val updatedPlayers = listOf(Player("Alice", -23.0, -46.0))
            getPlayersAtCourt.willReturn(Result.success(updatedPlayers))

            // Act
            vm.refreshPlayers()
            runCurrent()

            // Assert
            val state = assertIs<MapUiState.MapReady>(vm.uiState.value)
            assertEquals(updatedPlayers, state.players)
        }

    @Test
    fun `given state is not MapReady when refreshPlayers called state is unchanged`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.NOT_DETERMINED
            val vm = createViewModel()
            val stateBefore = vm.uiState.value

            // Act
            vm.refreshPlayers()
            runCurrent()

            // Assert
            assertEquals(stateBefore, vm.uiState.value)
        }

    // --- Permission result callback ---

    @Test
    fun `given permission NOT_DETERMINED when onPermissionResult called with GRANTED permission is marked requested and state transitions to MapReady`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.NOT_DETERMINED
            val vm = createViewModel()

            // Act
            locationService.stubbedStatus = LocationPermissionStatus.GRANTED
            getCourtLocation.willReturn(Result.success(MapCoordinates(-23.0, -46.0)))
            vm.onPermissionResult()
            runCurrent()

            // Assert
            assertEquals(1, locationService.markPermissionRequestedCallCount)
            assertIs<MapUiState.MapReady>(vm.uiState.value)
        }

    // --- Events ---

    @Test
    fun `when onOpenSettingsRequested called OpenAppSettings event is emitted`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.DENIED
            val vm = createViewModel()

            // Act
            vm.uiEvent.test {
                vm.onOpenSettingsRequested()
                runCurrent()

                // Assert
                assertEquals(MapUiEvent.OpenAppSettings, awaitItem())
            }
        }

    // --- sendLocation ---

    @Test
    fun `when sendLocation called delegates to SendLocation use case`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.DENIED
            val vm = createViewModel()

            // Act
            vm.sendLocation()
            runCurrent()

            // Assert
            assertEquals(1, sendLocation.callCount)
        }

    @Test
    fun `when sendLocation fails UI state is unchanged`() =
        runTest(testDispatcher) {
            // Arrange
            locationService.stubbedStatus = LocationPermissionStatus.DENIED
            sendLocation.willReturn(Result.failure(RuntimeException("Location unavailable")))
            val vm = createViewModel()
            val stateBefore = vm.uiState.value

            // Act
            vm.sendLocation()
            runCurrent()

            // Assert
            assertEquals(stateBefore, vm.uiState.value)
        }

}
