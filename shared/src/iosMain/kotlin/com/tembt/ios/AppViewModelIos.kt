package com.tembt.ios

import com.tembt.presentation.app.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

// Swift-friendly wrapper around AppViewModel.
// IMPORTANT: The Swift owner MUST call clear() from deinit to cancel the coroutine scope.
class AppViewModelIos(private val viewModel: AppViewModel) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun startObserving(onShowWelcomeChanged: (Boolean) -> Unit) {
        scope.launch {
            viewModel.showWelcome.collect { onShowWelcomeChanged(it) }
        }
    }

    fun onRegistered() = viewModel.onRegistered()

    fun clear() = scope.cancel()
}
