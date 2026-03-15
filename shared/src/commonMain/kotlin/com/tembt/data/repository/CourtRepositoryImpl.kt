package com.tembt.data.repository

import com.tembt.data.remote.TembtApiService
import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.repository.CourtRepository

class CourtRepositoryImpl(private val api: TembtApiService) : CourtRepository {
    override suspend fun getCourtLocation(): Result<MapCoordinates> =
        api.getCourtLocation()
}
