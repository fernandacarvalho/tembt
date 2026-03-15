package com.tembt.domain.repository

import com.tembt.domain.model.MapCoordinates

interface CourtRepository {
    suspend fun getCourtLocation(): Result<MapCoordinates>
}
