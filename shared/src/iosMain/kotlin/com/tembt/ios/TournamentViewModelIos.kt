package com.tembt.ios

import com.tembt.presentation.tournament.TournamentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

// Swift-friendly wrapper around TournamentViewModel.
// Bridges Kotlin StateFlow to a callback the SwiftUI ObservableObject can consume.
//
// IMPORTANT: The Swift owner (TournamentViewModelHost) MUST call clear() from deinit to cancel
// the coroutine scope. This class is designed to be held by a @StateObject to guarantee
// that deinit — and therefore clear() — is called exactly once.
class TournamentViewModelIos(private val viewModel: TournamentViewModel) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun startObserving(onStateChange: (TournamentUiStateIos) -> Unit) {
        scope.launch {
            viewModel.uiState.collect { state ->
                onStateChange(TournamentUiStateIos.from(state))
            }
        }
    }

    fun load(city: String? = null) {
        viewModel.load(city)
    }

    fun clear() {
        viewModel.cancel()
        scope.cancel()
    }
}
