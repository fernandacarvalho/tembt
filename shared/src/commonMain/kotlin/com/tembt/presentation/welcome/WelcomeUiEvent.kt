package com.tembt.presentation.welcome

sealed class WelcomeUiEvent {
    data object NavigateToMap : WelcomeUiEvent()
}
