package com.tembt.domain.repository

interface LocationRepository {
    suspend fun updateLocation(uuid: String, lat: Double, lng: Double): Result<Unit>
}
