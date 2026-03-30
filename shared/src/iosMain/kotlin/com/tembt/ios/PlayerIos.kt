package com.tembt.ios

// Flat, Swift-friendly representation of a Player.
// Exposed via MapUiStateIos.players so SwiftUI can render map annotations.
class PlayerIos(
    val name: String,
    val lat: Double,
    val lng: Double
)
