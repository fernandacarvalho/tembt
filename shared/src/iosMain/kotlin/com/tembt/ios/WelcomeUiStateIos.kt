package com.tembt.ios

import com.tembt.presentation.welcome.WelcomeUiState

// Flat, Swift-friendly representation of WelcomeUiState.
class WelcomeUiStateIos(
    val isLoading: Boolean,
    val error: String?
) {
    companion object {
        fun from(state: WelcomeUiState): WelcomeUiStateIos = when (state) {
            is WelcomeUiState.Idle -> WelcomeUiStateIos(isLoading = false, error = null)
            is WelcomeUiState.Loading -> WelcomeUiStateIos(isLoading = true, error = null)
            is WelcomeUiState.Error -> WelcomeUiStateIos(isLoading = false, error = state.message)
        }
    }
}
