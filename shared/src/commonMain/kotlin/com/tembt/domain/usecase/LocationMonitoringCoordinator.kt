package com.tembt.domain.usecase

import com.tembt.domain.model.CourtSchedule
import com.tembt.domain.model.LocationUpdateInterval
import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.repository.CourtRepository
import com.tembt.domain.repository.WindowRepository
import com.tembt.platform.CourtScheduleStorage
import com.tembt.platform.LocationMonitoringStateStorage
import com.tembt.platform.LocationServiceContract
import kotlinx.datetime.LocalDateTime

/**
 * Core background-monitoring logic, shared across platforms.
 *
 * Each platform worker/service calls [runCycle] with the current date/time.
 * The coordinator:
 *  1. Returns null immediately if the user paused monitoring for today.
 *  2. Fetches the court session window from the API.
 *  3. Checks whether the session is scheduled for today and still open at the
 *     current hour, using the API date/hours.
 *  4. Persists the session hours from the API so the scheduler uses them.
 *  5. Fetches the court location and the user's location (precision tied to whether the
 *     previous cycle settled at FAR — see [ApplyStationaryBackoffUseCase]).
 *  6. Computes the raw distance band, then applies the dwell/stationary backoff on top of
 *     it, persisting the result so it survives across cycles (and isolated iOS BGTask wakes).
 *  7. Sends the user's location to the API.
 *  8. Returns the effective [LocationUpdateInterval] the platform should wait before the
 *     next cycle.
 *
 * Returns **null** when monitoring should stop (user opted out, court closed, or
 * location data unavailable).
 */
class LocationMonitoringCoordinator(
    private val isCourtOpen: IsCourtOpenUseCase,
    private val calculateDistance: CalculateDistanceUseCase,
    private val getInterval: GetLocationUpdateIntervalUseCase,
    private val applyStationaryBackoff: ApplyStationaryBackoffUseCase,
    private val sendLocation: SendLocation,
    private val locationService: LocationServiceContract,
    private val courtRepository: CourtRepository,
    private val windowRepository: WindowRepository,
    private val courtScheduleStorage: CourtScheduleStorage,
    private val locationMonitoringStateStorage: LocationMonitoringStateStorage
) {
    /**
     * @param now The current date/time in the São Paulo timezone (injected so the
     *            coordinator itself remains deterministic and easy to test).
     * @return The interval to wait before the next call, or null to stop monitoring.
     */
    suspend fun runCycle(now: LocalDateTime): LocationUpdateInterval? {
        val today = now.date
        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: iniciando ciclo para $now")

        if (courtScheduleStorage.isMonitoringPausedFor(today)) {
            println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: monitoramento pausado para hoje, saindo")
            return null
        }

        val window = windowRepository.getWindow().getOrNull() ?: run {
            println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: ERRO ao buscar janela de horário, saindo")
            return null
        }
        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: janela = ${window.startHour}–${window.endHour} data=${window.date}")

        if (!isCourtOpen(today, now.hour, window)) {
            println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: quadra fechada agora, saindo")
            return null
        }

        courtScheduleStorage.saveSchedule(CourtSchedule(window.startHour, window.endHour))

        val courtCoords = courtRepository.getCourtLocation().getOrNull() ?: run {
            println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: ERRO ao buscar coords da quadra, saindo")
            return null
        }
        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: coords da quadra = lat=${courtCoords.latitude} lng=${courtCoords.longitude}")

        val previousState = locationMonitoringStateStorage.getState()
        // High accuracy unless the last cycle settled at FAR — an unknown/stationary-far
        // user doesn't need a precise (expensive) fix just to confirm nothing changed.
        val highAccuracy = previousState == null || previousState.lastEffectiveInterval != LocationUpdateInterval.FAR

        val userCoords = locationService.getCurrentLocation(highAccuracy)
        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: coords do usuário = $userCoords")
        val (userLat, userLng) = userCoords ?: run {
            println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: localização do usuário indisponível, usando intervalo MEDIUM")
            return LocationUpdateInterval.MEDIUM
        }

        val userMapCoords = MapCoordinates(userLat, userLng)
        val distanceMeters = calculateDistance(user = userMapCoords, court = courtCoords)
        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: distância calculada = ${distanceMeters.toInt()}m")

        val rawInterval = getInterval(distanceMeters)
        val newState = applyStationaryBackoff(userMapCoords, rawInterval, previousState)
        locationMonitoringStateStorage.saveState(newState)
        println(
            "[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: banda bruta=$rawInterval " +
                "checagens paradas=${newState.consecutiveSameSpotCount} intervalo efetivo=${newState.lastEffectiveInterval}"
        )

        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: chamando sendLocation()")
        sendLocation()

        return newState.lastEffectiveInterval
    }
}
