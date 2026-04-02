package com.tembt.domain.usecase

import com.tembt.domain.model.Player

/** Abstraction over GetPlayersAtCourtUseCase so MapViewModel does not depend on PlayersRepository. */
fun interface GetPlayersAtCourt {
    suspend operator fun invoke(): Result<List<Player>>
}
