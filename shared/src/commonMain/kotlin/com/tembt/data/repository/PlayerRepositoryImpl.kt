package com.tembt.data.repository

import com.tembt.data.remote.TembtApiService
import com.tembt.domain.repository.PlayerRepository

class PlayerRepositoryImpl(private val api: TembtApiService) : PlayerRepository {
    override suspend fun registerPlayer(uuid: String, name: String): Result<Unit> =
        api.registerPlayer(uuid, name)
}
