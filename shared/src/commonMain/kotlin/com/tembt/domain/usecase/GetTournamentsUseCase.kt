package com.tembt.domain.usecase

import com.tembt.domain.model.Tournament
import com.tembt.domain.model.TournamentStatus
import com.tembt.domain.repository.TournamentRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

class GetTournamentsUseCase(private val repository: TournamentRepository) {

    suspend operator fun invoke(city: String? = null): Result<List<Tournament>> {
        val targetCity = city ?: DEFAULT_CITY
        val tz = TimeZone.of("America/Sao_Paulo")
        val today = Clock.System.todayIn(tz)
        val until = today.plus(WINDOW_DAYS, DateTimeUnit.DAY)
        return repository.getTournaments(city = targetCity, from = today, until = until)
            .map { list -> list.filter { it.status != TournamentStatus.CLOSED } }
    }

    companion object {
        const val DEFAULT_CITY = "Rio de Janeiro"
        const val WINDOW_DAYS = 40
    }
}
