package com.tembt.platform

import com.tembt.domain.model.LocationPermissionStatus

// expect: each platform must provide a way to query the current location permission status.
// Permission requesting is intentionally handled in the platform UI layer (Activity/SwiftUI view)
// because it requires foreground UI context that must not leak into shared code.
expect class LocationService {
    fun getPermissionStatus(): LocationPermissionStatus

    /** Returns GRANTED only when background ("always") access is allowed. */
    fun getBackgroundPermissionStatus(): LocationPermissionStatus

    // Called by the platform UI layer right before launching the system permission dialog,
    // so subsequent getPermissionStatus() calls can distinguish NOT_DETERMINED from DENIED.
    fun markPermissionRequested()

    /** Returns the most recently cached device coordinates, or null if unavailable. */
    fun getCurrentLocation(): Pair<Double, Double>?
}
