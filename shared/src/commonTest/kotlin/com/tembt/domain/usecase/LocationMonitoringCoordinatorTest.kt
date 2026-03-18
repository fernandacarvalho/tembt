package com.tembt.domain.usecase

import com.tembt.domain.model.LocationUpdateInterval
import com.tembt.domain.model.MapCoordinates
import com.tembt.fake.FakeCourtRepository
import com.tembt.fake.FakeCourtScheduleStorage
import com.tembt.fake.FakeLocationService
import com.tembt.fake.FakePlayerStorage
import com.tembt.fake.FakeSendLocation
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LocationMonitoringCoordinatorTest {

    private val courtCoords = MapCoordinates(latitude = -22.9557, longitude = -43.1961)

    private val weekday  = LocalDate(2026, 3, 16)  // Monday
    private val saturday = LocalDate(2026, 3, 14)  // Saturday

    private val locationService   = FakeLocationService()
    private val courtRepo         = FakeCourtRepository()
    private val sendLocation      = FakeSendLocation()
    private val playerStorage     = FakePlayerStorage()
    private val scheduleStorage   = FakeCourtScheduleStorage()

    private fun createCoordinator() = LocationMonitoringCoordinator(
        isCourtOpen          = IsCourtOpenUseCase(),
        calculateDistance    = CalculateDistanceUseCase(),
        getInterval          = GetLocationUpdateIntervalUseCase(),
        sendLocation         = sendLocation,
        locationService      = locationService,
        courtRepository      = courtRepo,
        playerStorage        = playerStorage,
        courtScheduleStorage = scheduleStorage
    )

    // --- Weekday guard ---

    @Test
    fun `given weekday runCycle returns null and does not send location`() = runTest {
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9557, -43.1961)

        val result = createCoordinator().runCycle(weekday)

        assertNull(result)
        assertEquals(0, sendLocation.callCount)
    }

    // --- Pause guard ---

    @Test
    fun `given monitoring paused for today runCycle returns null without sending location`() = runTest {
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9557, -43.1961)
        scheduleStorage.pauseMonitoringForToday(saturday)

        val result = createCoordinator().runCycle(saturday)

        assertNull(result)
        assertEquals(0, sendLocation.callCount)
    }

    @Test
    fun `given monitoring paused for a different day runCycle proceeds normally`() = runTest {
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9512, -43.1961)   // ~500 m away
        scheduleStorage.pauseMonitoringForToday(LocalDate(2026, 3, 7))  // different Saturday

        val result = createCoordinator().runCycle(saturday)

        assertEquals(LocationUpdateInterval.VERY_CLOSE, result)
    }

    // --- AT_COURT: user within 50 m ---

    @Test
    fun `given weekend and user within 50m runCycle returns AT_COURT`() = runTest {
        courtRepo.willReturn(Result.success(courtCoords))
        // ~30 m north of court — within AT_COURT threshold
        locationService.location = Pair(-22.9554, -43.1961)

        val result = createCoordinator().runCycle(saturday)

        assertEquals(LocationUpdateInterval.AT_COURT, result)
        assertEquals(1, sendLocation.callCount)
    }

    // --- Distance-based intervals ---

    @Test
    fun `given weekend and user within 1km runCycle returns VERY_CLOSE`() = runTest {
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9512, -43.1961)   // ~500 m

        val result = createCoordinator().runCycle(saturday)

        assertEquals(LocationUpdateInterval.VERY_CLOSE, result)
        assertEquals(1, sendLocation.callCount)
    }

    @Test
    fun `given weekend and user between 1km and 5km runCycle returns CLOSE`() = runTest {
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9377, -43.1961)   // ~2 km

        val result = createCoordinator().runCycle(saturday)

        assertEquals(LocationUpdateInterval.CLOSE, result)
        assertEquals(1, sendLocation.callCount)
    }

    @Test
    fun `given weekend and user between 5km and 10km runCycle returns MEDIUM`() = runTest {
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.8927, -43.1961)   // ~7 km

        val result = createCoordinator().runCycle(saturday)

        assertEquals(LocationUpdateInterval.MEDIUM, result)
        assertEquals(1, sendLocation.callCount)
    }

    @Test
    fun `given weekend and user beyond 10km runCycle returns FAR`() = runTest {
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.7757, -43.1961)   // ~20 km

        val result = createCoordinator().runCycle(saturday)

        assertEquals(LocationUpdateInterval.FAR, result)
        assertEquals(1, sendLocation.callCount)
    }

    // --- Error cases ---

    @Test
    fun `given court fetch fails runCycle returns null`() = runTest {
        courtRepo.willReturn(Result.failure(RuntimeException("Network error")))
        locationService.location = Pair(-22.9557, -43.1961)

        val result = createCoordinator().runCycle(saturday)

        assertNull(result)
        assertEquals(0, sendLocation.callCount)
    }

    @Test
    fun `given user location unavailable runCycle returns FAR`() = runTest {
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = null

        val result = createCoordinator().runCycle(saturday)

        assertEquals(LocationUpdateInterval.FAR, result)
        assertEquals(0, sendLocation.callCount)
    }

    @Test
    fun `given holiday runCycle returns non-null interval`() = runTest {
        courtRepo.willReturn(Result.success(courtCoords))
        locationService.location = Pair(-22.9512, -43.1961)   // ~500 m

        val newYearsDay = LocalDate(2026, 1, 1)
        val result = createCoordinator().runCycle(newYearsDay)

        assertEquals(LocationUpdateInterval.VERY_CLOSE, result)
    }
}
