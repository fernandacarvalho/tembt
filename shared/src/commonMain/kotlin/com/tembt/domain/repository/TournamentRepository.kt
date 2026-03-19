package com.tembt.domain.repository

import com.tembt.domain.model.Tournament
import kotlinx.datetime.LocalDate

interface TournamentRepository {
    suspend fun getTournaments(
        city: String,
        from: LocalDate,
        until: LocalDate
    ): Result<List<Tournament>>
}
