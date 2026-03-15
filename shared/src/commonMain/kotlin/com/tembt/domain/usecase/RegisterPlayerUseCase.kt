package com.tembt.domain.usecase

import com.tembt.domain.repository.PlayerRepository
import com.tembt.platform.PlayerStorage

class RegisterPlayerUseCase(
    private val playerRepository: PlayerRepository,
    private val playerStorage: PlayerStorage
) {
    suspend operator fun invoke(name: String): Result<Unit> {
        val uuid = playerStorage.getDeviceUuid()
        return playerRepository.registerPlayer(uuid, name)
            .onSuccess { playerStorage.saveRegistration(name) }
    }
}
