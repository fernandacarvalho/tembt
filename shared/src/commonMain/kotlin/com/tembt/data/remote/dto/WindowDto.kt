package com.tembt.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WindowResponse(
    @SerialName("target_date") val targetDate: String,
    @SerialName("start_hour") val startHour: Int,
    @SerialName("end_hour") val endHour: Int,
    val slots: List<String>
)

@Serializable
data class CheckinsResponse(
    val date: String,
    @SerialName("check_ins") val checkIns: List<CheckinEntryDto>
)

@Serializable
data class CheckinEntryDto(
    val id: Int,
    val player: CheckinPlayerDto,
    val date: String,
    @SerialName("time_slot") val timeSlot: String
)

@Serializable
data class CheckinPlayerDto(val name: String)

@Serializable
data class CheckinRequest(
    val uuid: String,
    @SerialName("time_slot") val timeSlot: String
)
