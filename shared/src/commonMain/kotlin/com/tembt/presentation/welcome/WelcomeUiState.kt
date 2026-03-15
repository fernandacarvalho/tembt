package com.tembt.presentation.welcome

sealed class WelcomeUiState {
    data object Idle : WelcomeUiState()
    data object Loading : WelcomeUiState()
    data class Error(val message: String) : WelcomeUiState()
}
