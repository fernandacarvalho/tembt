package com.tembt.fake

import com.tembt.domain.model.Player
import com.tembt.domain.usecase.GetPlayersAtCourt

class FakeGetPlayersAtCourt : GetPlayersAtCourt {

    var callCount = 0
    var result: Result<List<Player>> = Result.success(emptyList())

    fun willReturn(result: Result<List<Player>>) {
        this.result = result
    }

    override suspend fun invoke(): Result<List<Player>> {
        callCount++
        return result
    }
}
