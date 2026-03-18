package com.tembt.domain.usecase

import com.tembt.domain.model.MapCoordinates
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val EARTH_RADIUS_METERS = 6_371_000.0

/**
 * Calculates the great-circle distance in metres between two GPS coordinates
 * using the Haversine formula.
 */
class CalculateDistanceUseCase {
    operator fun invoke(user: MapCoordinates, court: MapCoordinates): Double {
        val lat1 = user.latitude.toRadians()
        val lat2 = court.latitude.toRadians()
        val dLat = (court.latitude - user.latitude).toRadians()
        val dLng = (court.longitude - user.longitude).toRadians()

        val a = sin(dLat / 2).pow(2) +
                cos(lat1) * cos(lat2) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }

    private fun Double.toRadians(): Double = this * PI / 180.0
}
