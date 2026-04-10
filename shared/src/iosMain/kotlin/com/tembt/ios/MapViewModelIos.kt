package com.tembt.ios

import com.tembt.presentation.map.MapViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

// Swift-friendly wrapper around MapViewModel.
// Bridges Kotlin StateFlow to a callback the SwiftUI ObservableObject can consume.
//
// IMPORTANT: The Swift owner (MapViewModelHost) MUST call clear() from deinit to cancel
// the coroutine scope. This class is designed to be held by a @StateObject to guarantee
// that deinit — and therefore clear() — is called exactly once.
class MapViewModelIos(private val viewModel: MapViewModel) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun startObserving(onStateChange: (MapUiStateIos) -> Unit) {
        scope.launch {
            viewModel.uiState.collect { state ->
                onStateChange(MapUiStateIos.from(state))
            }
        }
    }

    fun checkPermission() {
        viewModel.checkPermission()
    }

    fun refreshPlayers() {
        viewModel.refreshPlayers()
    }

    fun onPause() {
        viewModel.onPause()
    }

    fun sendLocation() {
        viewModel.sendLocation()
    }

    // Cancels the entire scope — stops all collection. Call from Swift deinit.
    fun clear() {
        scope.cancel()
    }
}
