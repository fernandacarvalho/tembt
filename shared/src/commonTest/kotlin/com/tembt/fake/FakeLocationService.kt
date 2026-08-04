package com.tembt.fake

import com.tembt.domain.model.LocationPermissionStatus
import com.tembt.platform.LocationServiceContract

class FakeLocationService(
    var stubbedStatus: LocationPermissionStatus = LocationPermissionStatus.NOT_DETERMINED,
    var stubbedBackgroundStatus: LocationPermissionStatus = LocationPermissionStatus.GRANTED,
    var location: Pair<Double, Double>? = null
) : LocationServiceContract {

    var markPermissionRequestedCallCount = 0
    var getCurrentLocationCallCount = 0
    var lastRequestedHighAccuracy: Boolean? = null

    override fun getPermissionStatus() = stubbedStatus
    override fun getBackgroundPermissionStatus() = stubbedBackgroundStatus
    override fun markPermissionRequested() { markPermissionRequestedCallCount++ }
    override suspend fun getCurrentLocation(highAccuracy: Boolean): Pair<Double, Double>? {
        getCurrentLocationCallCount++
        lastRequestedHighAccuracy = highAccuracy
        return location
    }
}
