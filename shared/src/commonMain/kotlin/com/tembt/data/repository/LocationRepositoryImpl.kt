package com.tembt.data.repository

import com.tembt.data.remote.TembtApiService
import com.tembt.domain.repository.LocationRepository

class LocationRepositoryImpl(private val api: TembtApiService) : LocationRepository {
    override suspend fun updateLocation(uuid: String, lat: Double, lng: Double): Result<Unit> =
        api.updateLocation(uuid, lat, lng)
}
