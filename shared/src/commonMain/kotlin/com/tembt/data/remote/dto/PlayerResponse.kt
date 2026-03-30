package com.tembt.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerResponse(
    val name: String,
    val lat: Double,
    @SerialName("long") val lng: Double
)
