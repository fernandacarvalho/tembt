package com.tembt.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tembt.domain.model.LocationPermissionStatus
import com.tembt.domain.model.MapCoordinates
import com.tembt.platform.LocationServiceContract
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Note: `androidx.lifecycle.ViewModel` is available in commonMain via the KMP artifact
// `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` — this is not an Android import leak.
private val MAP_CENTER = MapCoordinates(
    latitude = -22.9867693,
    longitude = -43.2041565
)

class MapViewModel(
    private val locationService: LocationServiceContract
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<MapUiEvent>()
    val uiEvent: SharedFlow<MapUiEvent> = _uiEvent.asSharedFlow()

    init {
        checkPermission()
    }

    // Called by the platform UI layer when the app lifecycle resumes
    // (e.g., returning from system Settings)
    fun onResume() = checkPermission()

    // Called by the platform UI layer after the system permission dialog is dismissed
    fun onPermissionResult() {
        locationService.markPermissionRequested()
        checkPermission()
    }

    // Called when the user requests navigation to app settings (permission denied state)
    fun onOpenSettingsRequested() {
        viewModelScope.launch { _uiEvent.emit(MapUiEvent.OpenAppSettings) }
    }

    fun checkPermission() {
        _uiState.value = when (locationService.getPermissionStatus()) {
            LocationPermissionStatus.GRANTED -> MapUiState.MapReady(MAP_CENTER)
            LocationPermissionStatus.DENIED -> MapUiState.PermissionRequired(LocationPermissionStatus.DENIED)
            LocationPermissionStatus.NOT_DETERMINED -> MapUiState.PermissionRequired(LocationPermissionStatus.NOT_DETERMINED)
        }
    }
}
