package com.tembt.presentation.tournament

import com.tembt.domain.model.Tournament

sealed interface TournamentUiState {
    data object Loading : TournamentUiState
    data class Ready(val tournaments: List<Tournament>) : TournamentUiState
    data object Empty : TournamentUiState
    data class Error(val message: String) : TournamentUiState
}
