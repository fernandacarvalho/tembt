package com.tembt.fake

import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.usecase.GetCourtLocation

class FakeGetCourtLocation : GetCourtLocation {

    var callCount = 0
    var result: Result<MapCoordinates> = Result.success(MapCoordinates(0.0, 0.0))

    fun willReturn(result: Result<MapCoordinates>) {
        this.result = result
    }

    override suspend fun invoke(): Result<MapCoordinates> {
        callCount++
        return result
    }
}
