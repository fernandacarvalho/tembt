package com.tembt.domain.usecase

import com.tembt.domain.model.MapCoordinates

/** Abstraction over GetCourtLocationUseCase so MapViewModel does not depend on CourtRepository. */
fun interface GetCourtLocation {
    suspend operator fun invoke(): Result<MapCoordinates>
}
