package com.tembt.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tembt.domain.usecase.CheckinUseCase
import com.tembt.domain.usecase.GetWindowUseCase
import com.tembt.platform.DeviceIdentityProvider
import com.tembt.util.logD
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val getWindow: GetWindowUseCase,
    private val checkin: CheckinUseCase,
    private val deviceIdentity: DeviceIdentityProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<ScheduleUiEvent>()
    val uiEvent: SharedFlow<ScheduleUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadWindow()
    }

    fun loadWindow() {
        viewModelScope.launch {
            _uiState.value = ScheduleUiState.Loading
            getWindow().fold(
                onSuccess = { window -> _uiState.value = ScheduleUiState.Ready(window) },
                onFailure = { _uiState.value = ScheduleUiState.Error(it.message ?: "Erro ao carregar agenda.") }
            )
        }
    }

    /** Called from iOS deinit via ScheduleViewModelIos.clear() to stop in-flight coroutines. */
    fun cancel() {
        viewModelScope.cancel()
    }

    fun shareWindow() {
        val current = _uiState.value as? ScheduleUiState.Ready ?: return
        logD("ScheduleViewModel", "share button clicked for window date=${current.window.date}")
        viewModelScope.launch {
            // TODO: replace with a universal link (https://tembt.app/schedule?date=…) once the
            //  app is published to the App Store and Play Store and deep-link routing is configured.
            val url = "tembt://schedule?date=${current.window.date}"
            _uiEvent.emit(ScheduleUiEvent.CopyShareLink(url))
        }
    }

    fun checkin(slotTime: String) {
        val current = _uiState.value as? ScheduleUiState.Ready ?: return
        if (current.isCheckingIn) return
        viewModelScope.launch {
            _uiState.value = current.copy(isCheckingIn = true)
            val uuid = deviceIdentity.getDeviceUuid()
            checkin(uuid, slotTime).fold(
                onSuccess = {
                    val updated = _uiState.value as? ScheduleUiState.Ready ?: return@fold
                    _uiState.value = updated.copy(
                        checkedInSlotTime = slotTime,
                        isCheckingIn = false
                    )
                    // Reload to get updated player list for the slot
                    getWindow().onSuccess { window ->
                        val latest = _uiState.value as? ScheduleUiState.Ready ?: return@onSuccess
                        _uiState.value = latest.copy(window = window)
                    }
                },
                onFailure = {
                    val updated = _uiState.value as? ScheduleUiState.Ready ?: return@fold
                    _uiState.value = updated.copy(isCheckingIn = false)
                }
            )
        }
    }
}
