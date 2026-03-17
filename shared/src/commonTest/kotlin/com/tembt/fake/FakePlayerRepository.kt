package com.tembt.fake

import com.tembt.domain.repository.PlayerRepository

class FakePlayerRepository : PlayerRepository {

    private var result: Result<Unit> = Result.success(Unit)
    var callCount = 0
    var lastUuid: String? = null
    var lastName: String? = null

    fun willReturn(result: Result<Unit>) { this.result = result }

    override suspend fun registerPlayer(uuid: String, name: String): Result<Unit> {
        callCount++
        lastUuid = uuid
        lastName = name
        return result
    }
}
