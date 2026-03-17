package com.tembt.domain.usecase

import com.tembt.domain.repository.WindowRepository

class CheckinUseCase(private val windowRepository: WindowRepository) {
    suspend operator fun invoke(playerUuid: String, slotTime: String): Result<Unit> =
        windowRepository.checkin(playerUuid, slotTime)
}
