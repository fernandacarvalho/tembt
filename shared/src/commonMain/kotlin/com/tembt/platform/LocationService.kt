package com.tembt.platform

import com.tembt.domain.model.LocationPermissionStatus

// expect: each platform must provide a way to query the current location permission status.
// Permission requesting is intentionally handled in the platform UI layer (Activity/SwiftUI view)
// because it requires foreground UI context that must not leak into shared code.
expect class LocationService {
    fun getPermissionStatus(): LocationPermissionStatus

    // Called by the platform UI layer right before launching the system permission dialog,
    // so subsequent getPermissionStatus() calls can distinguish NOT_DETERMINED from DENIED.
    fun markPermissionRequested()
}
