package com.tembt.domain.model

/**
 * How often the background monitor should send location updates to the API,
 * determined by the user's distance from the court.
 */
enum class LocationUpdateInterval(val minutes: Int) {
    /** User is within 50 m (at the court) — update every 40 minutes. */
    AT_COURT(40),
    /** User is between 50 m and 1 km — update every 5 minutes. */
    VERY_CLOSE(5),
    /** User is between 1 km and 5 km — update every 10 minutes. */
    CLOSE(10),
    /** User is between 5 km and 10 km — update every 20 minutes. */
    MEDIUM(20),
    /** User is more than 10 km away — update every 60 minutes. */
    FAR(60)
}
