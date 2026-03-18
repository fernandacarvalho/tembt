package com.tembt.domain.usecase

import com.tembt.domain.model.LocationUpdateInterval

/**
 * Maps a distance (metres) to the appropriate [LocationUpdateInterval].
 *
 * Thresholds:
 *  - < 50 m     →  AT_COURT   (40 min) — user is at the court
 *  - < 1 000 m  →  VERY_CLOSE (5 min)
 *  - < 5 000 m  →  CLOSE      (10 min)
 *  - < 10 000 m →  MEDIUM     (20 min)
 *  - ≥ 10 000 m →  FAR        (60 min)
 */
class GetLocationUpdateIntervalUseCase {
    operator fun invoke(distanceMeters: Double): LocationUpdateInterval = when {
        distanceMeters < 50.0     -> LocationUpdateInterval.AT_COURT
        distanceMeters < 1_000.0  -> LocationUpdateInterval.VERY_CLOSE
        distanceMeters < 5_000.0  -> LocationUpdateInterval.CLOSE
        distanceMeters < 10_000.0 -> LocationUpdateInterval.MEDIUM
        else                      -> LocationUpdateInterval.FAR
    }
}
