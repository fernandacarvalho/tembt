package com.tembt.presentation.schedule

import com.tembt.domain.model.ScheduleWindow

sealed class ScheduleUiState {
    data object Loading : ScheduleUiState()
    data class Ready(
        val window: ScheduleWindow,
        val checkedInSlotTime: String? = null,
        val isCheckingIn: Boolean = false
    ) : ScheduleUiState()
    data class Error(val message: String) : ScheduleUiState()
}
