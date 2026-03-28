package com.tembt.domain.usecase

import com.tembt.domain.model.CourtSchedule
import com.tembt.domain.model.LocationUpdateInterval
import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.model.ScheduleWindow
import com.tembt.fake.FakeCourtRepository
import com.tembt.fake.FakeCourtScheduleStorage
import com.tembt.fake.FakeLocationService
import com.tembt.fake.FakePlayerStorage
import com.tembt.fake.FakeSendLocation
import com.tembt.fake.FakeWindowRepository
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocationMonitoringCoordinatorTest {

    private val courtCoords = MapCoordinates(latitude = -22.9557, longitude = -43.1961)

    // today always matches the window date so the court is "open"
    private val today        = LocalDate(2026, 3, 28)
    private val sessionDate  = "2026-03-28"
    private val otherDate    = "2026-03-29"

    private val locationService  = FakeLocationService()
    private val courtRepo        = FakeCourtRepository()
    private val windowRepo       = FakeWindowRepository()
    private val sendLocation     = FakeSendLocation()
    private val playerStorage    = FakePlayerStorage()
    private val scheduleStorage  = FakeCourtScheduleStorage()

    private fun defaultWindow(date: String = sessionDate, startHour: Int = 6, endHour: Int = 16) =
        ScheduleWindow(date = date, startHour = startHour, endHour = endHour, slots = emptyList())

    private fun createCoordinator() = LocationMonitoringCoordinator(
        isCourtOpen          = IsCourtOpenUseCase(),
        calculateDistance    = CalculateDistanceUseCase(),
        getInterval          = GetLocationUpdateIntervalUseCase(),
        sendLocation         = sendLocation,
        locationService      = locationService,
        courtRepository      = courtRepo,
        windowRepository     = windowRepo,
        playerStorage        = playerStorage,
        courtScheduleStorage = scheduleStorage
    )

    // --- Session date guard ---

    @Test
    fun `given window date does not match today runCycle returns null and does not send location`() = runTest {
        windowRepo.willReturnWindow(Result.success(defaultWindow(date = otherDate)))
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9557, -43.1961)

        val result = createCoordinator().runCycle(today)

        assertNull(result)
        assertEquals(0, sendLocation.callCount)
    }

    // --- Pause guard ---

    @Test
    fun `given monitoring paused for today runCycle returns null without sending location`() = runTest {
        windowRepo.willReturnWindow(Result.success(defaultWindow()))
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9557, -43.1961)
        scheduleStorage.pauseMonitoringForToday(today)

        val result = createCoordinator().runCycle(today)

        assertNull(result)
        assertEquals(0, sendLocation.callCount)
    }

    @Test
    fun `given monitoring paused for a different day runCycle proceeds normally`() = runTest {
        windowRepo.willReturnWindow(Result.success(defaultWindow()))
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9512, -43.1961)   // ~500 m away
        scheduleStorage.pauseMonitoringForToday(LocalDate(2026, 3, 21))  // different day

        val result = createCoordinator().runCycle(today)

        assertEquals(LocationUpdateInterval.VERY_CLOSE, result)
    }

    // --- Schedule saved from API ---

    @Test
    fun `given window with custom hours runCycle saves schedule to storage`() = runTest {
        windowRepo.willReturnWindow(Result.success(defaultWindow(startHour = 8, endHour = 18)))
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9557, -43.1961)

        createCoordinator().runCycle(today)

        assertEquals(CourtSchedule(startHour = 8, endHour = 18), scheduleStorage.lastSaved)
    }

    // --- Distance-based intervals ---

    @Test
    fun `given session today and user within 50m runCycle returns AT_COURT`() = runTest {
        windowRepo.willReturnWindow(Result.success(defaultWindow()))
        courtRepo.willReturn(Result.success(courtCoords))
        // ~30 m north of court — within AT_COURT threshold
        locationService.location = Pair(-22.9554, -43.1961)

        val result = createCoordinator().runCycle(today)

        assertEquals(LocationUpdateInterval.AT_COURT, result)
        assertEquals(1, sendLocation.callCount)
    }

    @Test
    fun `given session today and user within 1km runCycle returns VERY_CLOSE`() = runTest {
        windowRepo.willReturnWindow(Result.success(defaultWindow()))
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9512, -43.1961)   // ~500 m

        val result = createCoordinator().runCycle(today)

        assertEquals(LocationUpdateInterval.VERY_CLOSE, result)
        assertEquals(1, sendLocation.callCount)
    }

    @Test
    fun `given session today and user between 1km and 5km runCycle returns CLOSE`() = runTest {
        windowRepo.willReturnWindow(Result.success(defaultWindow()))
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9377, -43.1961)   // ~2 km

        val result = createCoordinator().runCycle(today)

        assertEquals(LocationUpdateInterval.CLOSE, result)
        assertEquals(1, sendLocation.callCount)
    }

    @Test
    fun `given session today and user between 5km and 10km runCycle returns MEDIUM`() = runTest {
        windowRepo.willReturnWindow(Result.success(defaultWindow()))
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.8927, -43.1961)   // ~7 km

        val result = createCoordinator().runCycle(today)

        assertEquals(LocationUpdateInterval.MEDIUM, result)
        assertEquals(1, sendLocation.callCount)
    }

    @Test
    fun `given session today and user beyond 10km runCycle returns FAR`() = runTest {
        windowRepo.willReturnWindow(Result.success(defaultWindow()))
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.7757, -43.1961)   // ~20 km

        val result = createCoordinator().runCycle(today)

        assertEquals(LocationUpdateInterval.FAR, result)
        assertEquals(1, sendLocation.callCount)
    }

    // --- Error cases ---

    @Test
    fun `given window fetch fails runCycle returns null`() = runTest {
        windowRepo.willReturnWindow(Result.failure(RuntimeException("Network error")))
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9557, -43.1961)

        val result = createCoordinator().runCycle(today)

        assertNull(result)
        assertEquals(0, sendLocation.callCount)
    }

    @Test
    fun `given court fetch fails runCycle returns null`() = runTest {
        windowRepo.willReturnWindow(Result.success(defaultWindow()))
        courtRepo.willReturn(Result.failure(RuntimeException("Network error")))
        locationService.location = Pair(-22.9557, -43.1961)

        val result = createCoordinator().runCycle(today)

        assertNull(result)
        assertEquals(0, sendLocation.callCount)
    }

    @Test
    fun `given user location unavailable runCycle returns FAR`() = runTest {
        windowRepo.willReturnWindow(Result.success(defaultWindow()))
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = null

        val result = createCoordinator().runCycle(today)

        assertEquals(LocationUpdateInterval.FAR, result)
        assertEquals(0, sendLocation.callCount)
    }
}
