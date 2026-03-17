package com.tembt.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tembt.domain.model.LocationPermissionStatus
import com.tembt.domain.usecase.GetCourtLocationUseCase
import com.tembt.domain.usecase.GetPlayersAtCourtUseCase
import com.tembt.domain.usecase.SendLocation
import com.tembt.platform.LocationServiceContract
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Note: `androidx.lifecycle.ViewModel` is available in commonMain via the KMP artifact
// `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` — this is not an Android import leak.
class MapViewModel(
    private val locationService: LocationServiceContract,
    private val getCourtLocation: GetCourtLocationUseCase,
    private val getPlayersAtCourt: GetPlayersAtCourtUseCase,
    private val sendLocationUseCase: SendLocation
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<MapUiEvent>()
    val uiEvent: SharedFlow<MapUiEvent> = _uiEvent.asSharedFlow()

    // Tracks the in-flight fetch so concurrent checkPermission() calls don't spawn duplicate requests
    private var fetchJob: Job? = null

    init {
        checkPermission()
    }

    fun onResume() = checkPermission()

    fun onPermissionResult() {
        locationService.markPermissionRequested()
        checkPermission()
    }

    fun onOpenSettingsRequested() {
        viewModelScope.launch { _uiEvent.emit(MapUiEvent.OpenAppSettings) }
    }

    fun checkPermission() {
        when (locationService.getPermissionStatus()) {
            LocationPermissionStatus.GRANTED -> {
                if (_uiState.value is MapUiState.MapReady) return
                fetchCourtAndShowMap()
            }
            LocationPermissionStatus.DENIED ->
                _uiState.value = MapUiState.PermissionRequired(LocationPermissionStatus.DENIED)
            LocationPermissionStatus.NOT_DETERMINED ->
                _uiState.value = MapUiState.PermissionRequired(LocationPermissionStatus.NOT_DETERMINED)
        }
    }

    private fun fetchCourtAndShowMap() {
        if (fetchJob?.isActive == true) return
        fetchJob = viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            getCourtLocation().fold(
                onSuccess = { center ->
                    _uiState.value = MapUiState.MapReady(center)
                    // Players fetched sequentially in the same coroutine — guaranteed to run
                    // only after court succeeds, and cancelled together if the scope is cancelled.
                    getPlayersAtCourt().onSuccess { players ->
                        val current = _uiState.value
                        if (current is MapUiState.MapReady) {
                            _uiState.value = current.copy(players = players)
                        }
                    }
                    // Player fetch failure is silent — map still displays without pins
                },
                onFailure = {
                    _uiState.value = MapUiState.Error(
                        it.message ?: "Erro ao carregar a quadra."
                    )
                }
            )
        }
    }

    fun refreshPlayers() {
        viewModelScope.launch {
            getPlayersAtCourt().onSuccess { players ->
                val current = _uiState.value
                if (current is MapUiState.MapReady) {
                    _uiState.value = current.copy(players = players)
                }
            }
        }
    }

    fun sendLocation() {
        viewModelScope.launch {
            sendLocationUseCase() // result logged by Ktor; no UI state change needed
        }
    }
}
