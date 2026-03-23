package com.tembt.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CourtResponse(
    val lat: Double,
    @SerialName("long") val lng: Double,
    val name: String = ""   // optional — empty until backend includes the field
)
