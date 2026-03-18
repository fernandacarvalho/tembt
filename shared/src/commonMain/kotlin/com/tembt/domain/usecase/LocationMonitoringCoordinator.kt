package com.tembt.domain.usecase

import com.tembt.domain.model.LocationUpdateInterval
import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.repository.CourtRepository
import com.tembt.platform.CourtScheduleStorage
import com.tembt.platform.LocationServiceContract
import com.tembt.platform.PlayerStorage
import kotlinx.datetime.LocalDate

/**
 * Core background-monitoring logic, shared across platforms.
 *
 * Each platform worker/service calls [runCycle] with today's date.
 * The coordinator:
 *  1. Returns null immediately if the user paused monitoring for today.
 *  2. Checks whether the court is open today (weekend / holiday).
 *  3. Fetches the court location and the user's last known location.
 *  4. Sends the user's location to the API.
 *  5. Returns the [LocationUpdateInterval] the platform should wait before the next cycle.
 *
 * Returns **null** when monitoring should stop (user opted out, court closed, or
 * location data unavailable).
 */
class LocationMonitoringCoordinator(
    private val isCourtOpen: IsCourtOpenUseCase,
    private val calculateDistance: CalculateDistanceUseCase,
    private val getInterval: GetLocationUpdateIntervalUseCase,
    private val sendLocation: SendLocation,
    private val locationService: LocationServiceContract,
    private val courtRepository: CourtRepository,
    private val playerStorage: PlayerStorage,
    private val courtScheduleStorage: CourtScheduleStorage
) {
    /**
     * @param today The current date in the São Paulo timezone (injected so the
     *              coordinator itself remains deterministic and easy to test).
     * @return The interval to wait before the next call, or null to stop monitoring.
     */
    suspend fun runCycle(today: LocalDate): LocationUpdateInterval? {
        if (courtScheduleStorage.isMonitoringPausedFor(today)) return null
        if (!isCourtOpen(today)) return null

        val courtCoords = courtRepository.getCourtLocation().getOrNull() ?: return null
        val (userLat, userLng) = locationService.getCurrentLocation() ?: return LocationUpdateInterval.FAR

        val distanceMeters = calculateDistance(
            user  = MapCoordinates(userLat, userLng),
            court = courtCoords
        )

        sendLocation()

        return getInterval(distanceMeters)
    }
}
