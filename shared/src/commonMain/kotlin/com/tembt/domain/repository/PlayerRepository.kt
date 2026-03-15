package com.tembt.domain.repository

interface PlayerRepository {
    suspend fun registerPlayer(uuid: String, name: String): Result<Unit>
}
