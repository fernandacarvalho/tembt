package com.tembt.presentation.map

import com.tembt.domain.model.LocationPermissionStatus
import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.model.Player

sealed class MapUiState {
    data object Loading : MapUiState()
    data class PermissionRequired(val status: LocationPermissionStatus) : MapUiState()
    data object BackgroundPermissionRequired : MapUiState()
    data class MapReady(
        val center: MapCoordinates,
        val courtName: String = "",
        val players: List<Player> = emptyList(),
        val lastUpdatedAt: String? = null
    ) : MapUiState()
    data class Error(val message: String) : MapUiState()
}
