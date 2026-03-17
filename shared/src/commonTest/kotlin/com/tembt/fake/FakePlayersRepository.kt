package com.tembt.fake

import com.tembt.domain.model.Player
import com.tembt.domain.repository.PlayersRepository

class FakePlayersRepository : PlayersRepository {

    private var result: Result<List<Player>> = Result.success(emptyList())
    var callCount = 0

    fun willReturn(result: Result<List<Player>>) { this.result = result }

    override suspend fun getPlayers(): Result<List<Player>> {
        callCount++
        return result
    }
}
