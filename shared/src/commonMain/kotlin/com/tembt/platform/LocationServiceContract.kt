package com.tembt.platform

import com.tembt.domain.model.LocationPermissionStatus

// Interface defined in domain/platform boundary so MapViewModel depends on an abstraction,
// not the concrete expect class. Both actual implementations must implement this.
interface LocationServiceContract {
    fun getPermissionStatus(): LocationPermissionStatus
    /** Returns GRANTED only when background ("always") access is allowed. */
    fun getBackgroundPermissionStatus(): LocationPermissionStatus
    fun markPermissionRequested()
    /**
     * Actively requests a fresh location fix, or null if unavailable/timed out.
     * @param highAccuracy When true, requests the platform's most precise (and most
     * power-hungry) profile; when false, a coarser/low-power profile is used instead.
     */
    // No default: `LocationService` (expect/actual) implements this interface, and Kotlin/Native
    // forbids default parameter values on an actual function that also overrides an interface
    // member — every call site passes highAccuracy explicitly instead.
    suspend fun getCurrentLocation(highAccuracy: Boolean): Pair<Double, Double>?
}
