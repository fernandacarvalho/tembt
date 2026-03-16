package com.tembt.domain.repository

import com.tembt.domain.model.Player

interface PlayersRepository {
    suspend fun getPlayers(): Result<List<Player>>
}
