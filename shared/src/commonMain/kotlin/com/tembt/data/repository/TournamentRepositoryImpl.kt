package com.tembt.data.repository

import com.tembt.data.remote.TembtApiService
import com.tembt.domain.model.Tournament
import com.tembt.domain.repository.TournamentRepository
import kotlinx.datetime.LocalDate

class TournamentRepositoryImpl(private val api: TembtApiService) : TournamentRepository {

    override suspend fun getTournaments(
        city: String,
        from: LocalDate,
        until: LocalDate
    ): Result<List<Tournament>> = api.getTournaments(city = city, from = from, until = until)
}
