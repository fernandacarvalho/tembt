package com.tembt.fake

import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.repository.CourtRepository

class FakeCourtRepository : CourtRepository {

    private var result: Result<MapCoordinates> = Result.success(MapCoordinates(0.0, 0.0))
    var callCount = 0

    fun willReturn(result: Result<MapCoordinates>) { this.result = result }

    override suspend fun getCourtLocation(): Result<MapCoordinates> {
        callCount++
        return result
    }
}
