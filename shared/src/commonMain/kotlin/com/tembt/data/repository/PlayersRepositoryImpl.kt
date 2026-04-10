package com.tembt.data.repository

import com.tembt.data.remote.TembtApiService
import com.tembt.domain.model.Player
import com.tembt.domain.repository.PlayersRepository

class PlayersRepositoryImpl(private val api: TembtApiService) : PlayersRepository {
    override suspend fun getPlayers(): Result<List<Player>> {
        val result = api.getPlayers()
        println("[TEMBT-DEBUG] PlayersRepository: resultado da API = $result")
        return result
    }
}
