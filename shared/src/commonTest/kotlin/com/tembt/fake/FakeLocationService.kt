package com.tembt.fake

import com.tembt.domain.model.LocationPermissionStatus
import com.tembt.platform.LocationServiceContract

class FakeLocationService(
    var stubbedStatus: LocationPermissionStatus = LocationPermissionStatus.NOT_DETERMINED,
    var location: Pair<Double, Double>? = null
) : LocationServiceContract {

    var markPermissionRequestedCallCount = 0

    override fun getPermissionStatus() = stubbedStatus
    override fun markPermissionRequested() { markPermissionRequestedCallCount++ }
    override fun getCurrentLocation() = location
}
