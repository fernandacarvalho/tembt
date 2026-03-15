package com.tembt.data.remote

import com.tembt.data.remote.dto.CourtResponse
import com.tembt.data.remote.dto.RegisterPlayerRequest
import com.tembt.domain.model.MapCoordinates
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class TembtApiService(private val client: HttpClient) {

    companion object {
        private const val BASE_URL = "https://tembt.filiponegrao.com.br/api"
    }

    suspend fun registerPlayer(uuid: String, name: String): Result<Unit> = runCatching {
        client.post("$BASE_URL/player") {
            contentType(ContentType.Application.Json)
            setBody(RegisterPlayerRequest(uuid = uuid, name = name))
        }
    }

    suspend fun getCourtLocation(): Result<MapCoordinates> = runCatching {
        val response = client.get("$BASE_URL/court").body<CourtResponse>()
        MapCoordinates(latitude = response.lat, longitude = response.lng)
    }
}
