package com.tembt.domain.usecase

/** Abstraction over SendLocationUseCase so MapViewModel does not depend on TembtApiService. */
fun interface SendLocation {
    suspend operator fun invoke(): Result<Unit>
}
