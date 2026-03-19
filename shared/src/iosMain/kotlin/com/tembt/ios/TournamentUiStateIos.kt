package com.tembt.ios

import com.tembt.domain.model.TournamentStatus
import com.tembt.presentation.tournament.TournamentUiState

class TournamentIos(
    val id: Int,
    val name: String,
    val venue: String,
    val city: String,
    val startDate: String,               // ISO "yyyy-MM-dd"
    val endDate: String,
    val registrationDeadline: String,
    val status: String,
    val priceMain: Double,
    val categories: List<String>,
    val registrationUrl: String
)

class TournamentUiStateIos(
    val isLoading: Boolean,
    val tournaments: List<TournamentIos>,
    val isEmpty: Boolean,
    val error: String?
) {
    companion object {
        fun from(state: TournamentUiState): TournamentUiStateIos = when (state) {
            is TournamentUiState.Loading -> TournamentUiStateIos(
                isLoading = true,
                tournaments = emptyList(),
                isEmpty = false,
                error = null
            )
            is TournamentUiState.Ready -> TournamentUiStateIos(
                isLoading = false,
                tournaments = state.tournaments.map { t ->
                    TournamentIos(
                        id = t.id,
                        name = t.name,
                        venue = t.venue,
                        city = t.city,
                        startDate = t.startDate.toString(),
                        endDate = t.endDate.toString(),
                        registrationDeadline = t.registrationDeadline.toString(),
                        status = when (t.status) {
                            TournamentStatus.OPEN              -> "Aberto"
                            TournamentStatus.CONFIRMED         -> "Confirmado"
                            TournamentStatus.CLOSED            -> "Encerrado"
                            TournamentStatus.REGISTRATION_OPEN -> "Inscrições abertas"
                        },
                        priceMain = t.priceMain,
                        categories = t.categories,
                        registrationUrl = t.registrationUrl
                    )
                },
                isEmpty = false,
                error = null
            )
            is TournamentUiState.Empty -> TournamentUiStateIos(
                isLoading = false,
                tournaments = emptyList(),
                isEmpty = true,
                error = null
            )
            is TournamentUiState.Error -> TournamentUiStateIos(
                isLoading = false,
                tournaments = emptyList(),
                isEmpty = false,
                error = state.message
            )
        }
    }
}
