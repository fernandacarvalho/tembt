package com.tembt.platform

import com.tembt.domain.model.LocationPermissionStatus

// JVM actual — stub for the jvm() target used exclusively to run commonTest on the JVM.
// Tests inject FakeLocationService directly; this class is never instantiated in tests.
actual class LocationService : LocationServiceContract {
    actual override fun getPermissionStatus() = LocationPermissionStatus.NOT_DETERMINED
    actual override fun markPermissionRequested() = Unit
    actual override fun getCurrentLocation(): Pair<Double, Double>? = null
}
