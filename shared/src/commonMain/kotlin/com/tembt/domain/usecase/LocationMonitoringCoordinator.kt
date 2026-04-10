package com.tembt.domain.usecase

import com.tembt.domain.model.CourtSchedule
import com.tembt.domain.model.LocationUpdateInterval
import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.repository.CourtRepository
import com.tembt.domain.repository.WindowRepository
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
 *  2. Fetches the court session window from the API.
 *  3. Checks whether the session is scheduled for today using the API date.
 *  4. Persists the session hours from the API so the scheduler uses them.
 *  5. Fetches the court location and the user's last known location.
 *  6. Sends the user's location to the API.
 *  7. Returns the [LocationUpdateInterval] the platform should wait before the next cycle.
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
    private val windowRepository: WindowRepository,
    private val playerStorage: PlayerStorage,
    private val courtScheduleStorage: CourtScheduleStorage
) {
    /**
     * @param today The current date in the São Paulo timezone (injected so the
     *              coordinator itself remains deterministic and easy to test).
     * @return The interval to wait before the next call, or null to stop monitoring.
     */
    suspend fun runCycle(today: LocalDate): LocationUpdateInterval? {
        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: iniciando ciclo para $today")

        if (courtScheduleStorage.isMonitoringPausedFor(today)) {
            println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: monitoramento pausado para hoje, saindo")
            return null
        }

        val window = windowRepository.getWindow().getOrNull() ?: run {
            println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: ERRO ao buscar janela de horário, saindo")
            return null
        }
        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: janela = ${window.startHour}–${window.endHour} data=${window.date}")

        if (!isCourtOpen(today, window.date)) {
            println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: quadra fechada hoje, saindo")
            return null
        }

        courtScheduleStorage.saveSchedule(CourtSchedule(window.startHour, window.endHour))

        val courtCoords = courtRepository.getCourtLocation().getOrNull() ?: run {
            println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: ERRO ao buscar coords da quadra, saindo")
            return null
        }
        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: coords da quadra = lat=${courtCoords.latitude} lng=${courtCoords.longitude}")

        val userCoords = locationService.getCurrentLocation()
        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: coords do usuário = $userCoords")
        val (userLat, userLng) = userCoords ?: run {
            println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: localização do usuário indisponível, usando intervalo MEDIUM")
            return LocationUpdateInterval.MEDIUM
        }

        val distanceMeters = calculateDistance(
            user  = MapCoordinates(userLat, userLng),
            court = courtCoords
        )
        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: distância calculada = ${distanceMeters.toInt()}m")

        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: chamando sendLocation()")
        sendLocation()

        val interval = getInterval(distanceMeters)
        println("[TEMBT-DEBUG] LocationMonitoringCoordinator.runCycle: próximo intervalo = $interval")
        return interval
    }
}
