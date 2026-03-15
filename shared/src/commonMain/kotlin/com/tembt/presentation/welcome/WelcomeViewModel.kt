package com.tembt.presentation.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tembt.domain.usecase.RegisterPlayerUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WelcomeViewModel(
    private val registerPlayerUseCase: RegisterPlayerUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<WelcomeUiState>(WelcomeUiState.Idle)
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<WelcomeUiEvent>()
    val uiEvent: SharedFlow<WelcomeUiEvent> = _uiEvent.asSharedFlow()

    fun onStartClicked(name: String) {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) return
        if (_uiState.value is WelcomeUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = WelcomeUiState.Loading
            registerPlayerUseCase(trimmedName).fold(
                onSuccess = { _uiEvent.emit(WelcomeUiEvent.NavigateToMap) },
                onFailure = {
                    _uiState.value = WelcomeUiState.Error(
                        it.message ?: "Erro ao registrar. Tente novamente."
                    )
                }
            )
        }
    }
}
