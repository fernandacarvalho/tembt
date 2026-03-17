package com.tembt.ios

import com.tembt.presentation.schedule.ScheduleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

// Swift-friendly wrapper around ScheduleViewModel.
// Bridges Kotlin StateFlow to a callback the SwiftUI ObservableObject can consume.
//
// IMPORTANT: The Swift owner (ScheduleViewModelHost) MUST call clear() from deinit to cancel
// the coroutine scope. This class is designed to be held by a @StateObject to guarantee
// that deinit — and therefore clear() — is called exactly once.
class ScheduleViewModelIos(private val viewModel: ScheduleViewModel) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun startObserving(onStateChange: (ScheduleUiStateIos) -> Unit) {
        scope.launch {
            viewModel.uiState.collect { state ->
                onStateChange(ScheduleUiStateIos.from(state))
            }
        }
    }

    fun loadWindow() {
        viewModel.loadWindow()
    }

    fun checkin(slotTime: String) {
        viewModel.checkin(slotTime)
    }

    // Cancels the observer scope and the shared ViewModel's coroutine scope.
    // Call from Swift deinit to ensure no in-flight network coroutines outlive the view.
    fun clear() {
        viewModel.cancel()
        scope.cancel()
    }
}
