package com.tembt.data.remote

import com.tembt.data.remote.dto.CheckinRequest
import com.tembt.data.remote.dto.CheckinsResponse
import com.tembt.data.remote.dto.CourtResponse
import com.tembt.data.remote.dto.PlayerResponse
import com.tembt.data.remote.dto.RegisterPlayerRequest
import com.tembt.data.remote.dto.UpdateLocationRequest
import com.tembt.data.remote.dto.WindowResponse
import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.model.Player
import com.tembt.domain.model.ScheduleWindow
import com.tembt.domain.model.SlotPlayer
import com.tembt.domain.model.WindowSlot
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

    suspend fun getPlayers(): Result<List<Player>> = runCatching {
        client.get("$BASE_URL/players").body<List<PlayerResponse>>().map {
            Player(uuid = it.uuid, name = it.name, lat = it.lat, lng = it.lng)
        }
    }

    suspend fun updateLocation(uuid: String, lat: Double, lng: Double): Result<Unit> = runCatching {
        client.post("$BASE_URL/location") {
            contentType(ContentType.Application.Json)
            setBody(UpdateLocationRequest(uuid = uuid, lat = lat, lng = lng))
        }
    }

    suspend fun getWindow(): Result<ScheduleWindow> = runCatching {
        val window = client.get("$BASE_URL/window").body<WindowResponse>()
        val checkins = runCatching {
            client.get("$BASE_URL/checkins").body<CheckinsResponse>()
        }.getOrNull()

        // Group check-ins by time slot so each slot knows its confirmed players
        val playersBySlot = checkins?.checkIns
            ?.groupBy { it.timeSlot }
            ?.mapValues { (_, entries) -> entries.map { SlotPlayer(name = it.player.name) } }
            ?: emptyMap()

        ScheduleWindow(
            date = window.targetDate,
            slots = window.slots.map { time ->
                WindowSlot(time = time, players = playersBySlot[time] ?: emptyList())
            }
        )
    }

    suspend fun checkin(playerUuid: String, slotTime: String): Result<Unit> = runCatching {
        client.post("$BASE_URL/checkin") {
            contentType(ContentType.Application.Json)
            setBody(CheckinRequest(uuid = playerUuid, timeSlot = slotTime))
        }
    }
}
