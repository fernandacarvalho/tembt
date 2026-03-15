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
    val isDenied: Boolean,      // true when permission was explicitly denied → show Settings button
    val center: MapCoordinates? // non-null only when map is ready to display
) {
    companion object {
        fun from(state: MapUiState): MapUiStateIos = when (state) {
            is MapUiState.Loading -> MapUiStateIos(
                isLoading = true,
                isPermissionRequired = false,
                isDenied = false,
                center = null
            )
            is MapUiState.PermissionRequired -> MapUiStateIos(
                isLoading = false,
                isPermissionRequired = true,
                isDenied = state.status == LocationPermissionStatus.DENIED,
                center = null
            )
            is MapUiState.MapReady -> MapUiStateIos(
                isLoading = false,
                isPermissionRequired = false,
                isDenied = false,
                center = state.center
            )
        }
    }
}
