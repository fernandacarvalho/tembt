package com.tembt.domain.usecase

import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.repository.CourtRepository

class GetCourtLocationUseCase(private val courtRepository: CourtRepository) {
    suspend operator fun invoke(): Result<MapCoordinates> = courtRepository.getCourtLocation()
}
