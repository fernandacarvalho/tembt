package com.tembt.domain.usecase

import com.tembt.domain.model.LocationMonitoringState
import com.tembt.domain.model.LocationUpdateInterval
import com.tembt.domain.model.MapCoordinates

private const val SAME_SPOT_THRESHOLD_METERS = 30.0
private const val CYCLES_PER_ESCALATION_STEP = 3

// Ordered by minutes, not by distance — LocationUpdateInterval's bands aren't monotonic by
// minutes (AT_COURT=40 sits between MEDIUM=20 and FAR=60), so escalation walks this explicit
// ladder rather than the enum's declaration order.
private val ESCALATION_LADDER = listOf(
    LocationUpdateInterval.VERY_CLOSE,
    LocationUpdateInterval.CLOSE,
    LocationUpdateInterval.MEDIUM,
    LocationUpdateInterval.AT_COURT,
    LocationUpdateInterval.FAR
)

/**
 * Backs off the polling interval when the user stays within [SAME_SPOT_THRESHOLD_METERS] of a
 * fixed reference point for multiple consecutive cycles — e.g. someone who lives at a `CLOSE`
 * distance from the court but hasn't left home shouldn't be tracked as often as someone
 * actually approaching it. Every [CYCLES_PER_ESCALATION_STEP] consecutive same-spot cycles,
 * the effective interval steps one degree up [ESCALATION_LADDER], capped at `FAR`. Movement
 * beyond the threshold resets the streak and the effective interval back to the raw,
 * distance-based band.
 */
class ApplyStationaryBackoffUseCase(
    private val calculateDistance: CalculateDistanceUseCase
) {
    operator fun invoke(
        userCoords: MapCoordinates,
        rawInterval: LocationUpdateInterval,
        previousState: LocationMonitoringState?
    ): LocationMonitoringState {
        val isSameSpot = previousState != null &&
            calculateDistance(userCoords, previousState.referenceCoords) < SAME_SPOT_THRESHOLD_METERS

        val consecutiveCount = if (isSameSpot) previousState!!.consecutiveSameSpotCount + 1 else 1
        val referenceCoords = if (isSameSpot) previousState!!.referenceCoords else userCoords

        val steps = consecutiveCount / CYCLES_PER_ESCALATION_STEP
        val effectiveInterval = escalate(rawInterval, steps)

        return LocationMonitoringState(
            referenceCoords = referenceCoords,
            consecutiveSameSpotCount = consecutiveCount,
            lastEffectiveInterval = effectiveInterval
        )
    }

    private fun escalate(base: LocationUpdateInterval, steps: Int): LocationUpdateInterval {
        val index = ESCALATION_LADDER.indexOf(base)
        val escalatedIndex = (index + steps).coerceAtMost(ESCALATION_LADDER.lastIndex)
        return ESCALATION_LADDER[escalatedIndex]
    }
}
