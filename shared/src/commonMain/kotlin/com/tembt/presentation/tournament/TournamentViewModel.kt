package com.tembt.presentation.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tembt.domain.usecase.GetTournamentsUseCase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TournamentViewModel(
    private val getTournaments: GetTournamentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TournamentUiState>(TournamentUiState.Loading)
    val uiState: StateFlow<TournamentUiState> = _uiState.asStateFlow()

    init { load() }

    fun load(city: String? = null) {
        _uiState.value = TournamentUiState.Loading
        viewModelScope.launch {
            getTournaments(city).fold(
                onSuccess = { list ->
                    _uiState.value = if (list.isEmpty()) TournamentUiState.Empty
                    else TournamentUiState.Ready(list)
                },
                onFailure = { e ->
                    _uiState.value = TournamentUiState.Error(
                        e.message ?: "Erro ao carregar torneios."
                    )
                }
            )
        }
    }

    /** Called from iOS deinit via TournamentViewModelIos.clear() to stop in-flight coroutines. */
    fun cancel() {
        viewModelScope.cancel()
    }
}
