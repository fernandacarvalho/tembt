package com.tembt.fake

import com.tembt.domain.model.Tournament
import com.tembt.domain.repository.TournamentRepository
import kotlinx.datetime.LocalDate

class FakeTournamentRepository : TournamentRepository {

    private var result: Result<List<Tournament>> = Result.success(emptyList())

    var callCount = 0
    var lastCity: String? = null
    var lastFrom: LocalDate? = null
    var lastUntil: LocalDate? = null

    fun willReturn(result: Result<List<Tournament>>) { this.result = result }

    override suspend fun getTournaments(
        city: String,
        from: LocalDate,
        until: LocalDate
    ): Result<List<Tournament>> {
        callCount++
        lastCity = city
        lastFrom = from
        lastUntil = until
        return result
    }
}
