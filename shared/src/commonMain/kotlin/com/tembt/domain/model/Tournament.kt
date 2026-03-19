package com.tembt.domain.model

import kotlinx.datetime.LocalDate

data class Tournament(
    val id: Int,
    val name: String,
    val venue: String,
    val city: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val registrationDeadline: LocalDate,
    val status: TournamentStatus,
    val priceMain: Double,
    val categories: List<String>,
    val registrationUrl: String
)

enum class TournamentStatus {
    OPEN, CONFIRMED, CLOSED, REGISTRATION_OPEN;

    companion object {
        fun from(value: String): TournamentStatus = when (value.lowercase()) {
            "aberto"                  -> OPEN
            "confirmado"              -> CONFIRMED
            "encerrado"               -> CLOSED
            "liberação", "liberacao"  -> REGISTRATION_OPEN
            else                      -> CLOSED
        }
    }
}
