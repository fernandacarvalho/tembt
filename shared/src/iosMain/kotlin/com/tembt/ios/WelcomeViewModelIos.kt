package com.tembt.ios

import com.tembt.presentation.welcome.WelcomeUiEvent
import com.tembt.presentation.welcome.WelcomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

// Swift-friendly wrapper around WelcomeViewModel.
// IMPORTANT: The Swift owner MUST call clear() from deinit to cancel the coroutine scope.
class WelcomeViewModelIos(private val viewModel: WelcomeViewModel) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun startObserving(
        onStateChange: (WelcomeUiStateIos) -> Unit,
        onNavigateToMap: () -> Unit
    ) {
        scope.launch {
            viewModel.uiState.collect { onStateChange(WelcomeUiStateIos.from(it)) }
        }
        scope.launch {
            viewModel.uiEvent.collect { event ->
                if (event is WelcomeUiEvent.NavigateToMap) onNavigateToMap()
            }
        }
    }

    fun onStartClicked(name: String) = viewModel.onStartClicked(name)

    fun clear() = scope.cancel()
}
