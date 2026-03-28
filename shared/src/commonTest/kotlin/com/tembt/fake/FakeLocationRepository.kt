package com.tembt.fake

import com.tembt.domain.repository.LocationRepository

class FakeLocationRepository : LocationRepository {

    var result: Result<Unit> = Result.success(Unit)
    var callCount = 0
    var lastUuid: String? = null
    var lastLat: Double? = null
    var lastLng: Double? = null

    fun willReturn(result: Result<Unit>) { this.result = result }

    override suspend fun updateLocation(uuid: String, lat: Double, lng: Double): Result<Unit> {
        callCount++
        lastUuid = uuid
        lastLat = lat
        lastLng = lng
        return result
    }
}
