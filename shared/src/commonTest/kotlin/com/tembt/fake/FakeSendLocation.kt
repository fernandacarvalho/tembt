package com.tembt.fake

import com.tembt.domain.usecase.SendLocation

class FakeSendLocation : SendLocation {

    var callCount = 0
    var result: Result<Unit> = Result.success(Unit)

    override suspend fun invoke(): Result<Unit> {
        callCount++
        return result
    }
}
