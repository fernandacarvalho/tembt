package com.tembt.data.remote.dto

import com.tembt.domain.model.Tournament
import com.tembt.domain.model.TournamentStatus
import kotlinx.datetime.LocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TournamentDto(
    val id: Int,
    val name: String,
    val venue: String,
    val city: String,
    @SerialName("start_date") val startDate: String,
    @SerialName("end_date") val endDate: String,
    @SerialName("registration_deadline") val registrationDeadline: String,
    val status: String,
    @SerialName("price_main") val priceMain: Double,
    val categories: List<String>
)

fun TournamentDto.toDomain() = Tournament(
    id = id,
    name = name,
    venue = venue,
    city = city,
    startDate = LocalDate.parse(startDate),
    endDate = LocalDate.parse(endDate),
    registrationDeadline = LocalDate.parse(registrationDeadline),
    status = TournamentStatus.from(status),
    priceMain = priceMain,
    categories = categories,
    registrationUrl = "https://torneioja.com.br/inscricao/$id"
)
