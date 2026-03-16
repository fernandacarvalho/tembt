package com.tembt.platform

import com.tembt.domain.model.LocationPermissionStatus

// Interface defined in domain/platform boundary so MapViewModel depends on an abstraction,
// not the concrete expect class. Both actual implementations must implement this.
interface LocationServiceContract {
    fun getPermissionStatus(): LocationPermissionStatus
    fun markPermissionRequested()
    /** Returns the most recently cached device coordinates, or null if unavailable. */
    fun getCurrentLocation(): Pair<Double, Double>?
}
