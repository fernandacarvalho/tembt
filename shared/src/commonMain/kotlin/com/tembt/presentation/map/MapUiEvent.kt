package com.tembt.presentation.map

sealed class MapUiEvent {
    // Tells the platform UI layer to open the app's system settings screen
    data object OpenAppSettings : MapUiEvent()
}
