package com.tembt.domain.usecase

import com.tembt.domain.model.Player
import com.tembt.domain.repository.PlayersRepository

class GetPlayersAtCourtUseCase(private val playersRepository: PlayersRepository) {
    suspend operator fun invoke(): Result<List<Player>> = playersRepository.getPlayers()
}
