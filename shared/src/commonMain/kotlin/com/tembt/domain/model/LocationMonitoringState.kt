package com.tembt.domain.model

/**
 * Cross-cycle memory for [com.tembt.domain.usecase.LocationMonitoringCoordinator]'s dwell
 * (stationary) backoff: where the user was when the streak started, how many consecutive
 * cycles they've stayed within that spot, and the effective interval last applied.
 */
data class LocationMonitoringState(
    val referenceCoords: MapCoordinates,
    val consecutiveSameSpotCount: Int,
    val lastEffectiveInterval: LocationUpdateInterval
)
