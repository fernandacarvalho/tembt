package com.tembt.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class RegisterPlayerRequest(
    val uuid: String,
    val name: String
)
