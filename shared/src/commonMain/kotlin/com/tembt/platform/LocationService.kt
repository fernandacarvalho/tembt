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

    /**
     * Actively requests a fresh location fix, or null if unavailable/timed out.
     * @param highAccuracy When true, requests the platform's most precise (and most
     * power-hungry) profile; when false, a coarser/low-power profile is used instead.
     */
    suspend fun getCurrentLocation(highAccuracy: Boolean): Pair<Double, Double>?
}
