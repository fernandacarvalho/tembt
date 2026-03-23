package com.tembt.ios

import com.tembt.domain.model.LocationPermissionStatus
import com.tembt.domain.model.MapCoordinates
import com.tembt.presentation.map.MapUiState

// Flat, Swift-friendly representation of MapUiState.
// Sealed class hierarchies are awkward in Swift/ObjC interop;
// this single class with nullable fields is straightforward to consume from SwiftUI.
class MapUiStateIos(
    val isLoading: Boolean,
    val isPermissionRequired: Boolean,
    val isDenied: Boolean,
    val center: MapCoordinates?,
    val courtName: String,
    val error: String?,
    val players: List<PlayerIos>
) {
    companion object {
        fun from(state: MapUiState): MapUiStateIos = when (state) {
            is MapUiState.Loading -> MapUiStateIos(
                isLoading = true,
                isPermissionRequired = false,
                isDenied = false,
                center = null,
                courtName = "",
                error = null,
                players = emptyList()
            )
            is MapUiState.PermissionRequired -> MapUiStateIos(
                isLoading = false,
                isPermissionRequired = true,
                isDenied = state.status == LocationPermissionStatus.DENIED,
                center = null,
                courtName = "",
                error = null,
                players = emptyList()
            )
            is MapUiState.MapReady -> MapUiStateIos(
                isLoading = false,
                isPermissionRequired = false,
                isDenied = false,
                center = state.center,
                courtName = state.courtName,
                error = null,
                players = state.players.map { PlayerIos(it.uuid, it.name, it.lat, it.lng) }
            )
            is MapUiState.Error -> MapUiStateIos(
                isLoading = false,
                isPermissionRequired = false,
                isDenied = false,
                center = null,
                courtName = "",
                error = state.message,
                players = emptyList()
            )
        }
    }
}
