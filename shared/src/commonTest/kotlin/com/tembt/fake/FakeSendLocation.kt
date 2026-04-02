package com.tembt.fake

import com.tembt.domain.usecase.SendLocation

class FakeSendLocation : SendLocation {

    var callCount = 0
    private var result: Result<Unit> = Result.success(Unit)

    fun willReturn(result: Result<Unit>) {
        this.result = result
    }

    override suspend fun invoke(): Result<Unit> {
        callCount++
        return result
    }
}
