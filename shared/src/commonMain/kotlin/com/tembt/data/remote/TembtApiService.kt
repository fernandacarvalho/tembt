package com.tembt.data.remote

import com.tembt.data.remote.dto.CheckinRequest
import com.tembt.data.remote.dto.CheckinsResponse
import com.tembt.data.remote.dto.CourtResponse
import com.tembt.data.remote.dto.PlayerResponse
import com.tembt.data.remote.dto.RegisterPlayerRequest
import com.tembt.data.remote.dto.TournamentDto
import com.tembt.data.remote.dto.UpdateLocationRequest
import com.tembt.data.remote.dto.WindowResponse
import com.tembt.data.remote.dto.toDomain
import com.tembt.domain.model.MapCoordinates
import com.tembt.domain.model.NetworkError
import com.tembt.domain.model.Player
import com.tembt.domain.model.ScheduleWindow
import com.tembt.domain.model.SlotPlayer
import com.tembt.domain.model.Tournament
import com.tembt.domain.model.WindowSlot
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.datetime.LocalDate
import kotlinx.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class TembtApiService(private val client: HttpClient) {

    companion object {
        private const val BASE_URL = "https://tembt.filiponegrao.com.br/api"
    }

    suspend fun registerPlayer(uuid: String, name: String): Result<Unit> = safeCall {
        client.post("$BASE_URL/player") {
            contentType(ContentType.Application.Json)
            setBody(RegisterPlayerRequest(uuid = uuid, name = name))
        }
    }

    suspend fun getCourtLocation(): Result<MapCoordinates> = safeCall {
        val response = client.get("$BASE_URL/court").body<CourtResponse>()
        MapCoordinates(latitude = response.lat, longitude = response.lng, name = response.name)
    }

    suspend fun getPlayers(): Result<List<Player>> = safeCall {
        client.get("$BASE_URL/players").body<List<PlayerResponse>>().map {
            Player(name = it.name, lat = it.lat, lng = it.lng)
        }
    }

    suspend fun updateLocation(uuid: String, lat: Double, lng: Double): Result<Unit> = safeCall {
        client.post("$BASE_URL/location") {
            contentType(ContentType.Application.Json)
            setBody(UpdateLocationRequest(uuid = uuid, lat = lat, lng = lng))
        }
    }

    suspend fun getWindow(): Result<ScheduleWindow> = safeCall {
        val window = client.get("$BASE_URL/window").body<WindowResponse>()
        val checkins = safeCall {
            client.get("$BASE_URL/checkins").body<CheckinsResponse>()
        }.getOrNull()

        // Group check-ins by time slot so each slot knows its confirmed players
        val playersBySlot = checkins?.checkIns
            ?.groupBy { it.timeSlot }
            ?.mapValues { (_, entries) -> entries.map { SlotPlayer(name = it.player.name) } }
            ?: emptyMap()

        ScheduleWindow(
            date = window.targetDate,
            startHour = window.startHour,
            endHour = window.endHour,
            slots = window.slots.map { time ->
                WindowSlot(time = time, players = playersBySlot[time] ?: emptyList())
            }
        )
    }

    suspend fun checkin(playerUuid: String, slotTime: String): Result<Unit> = safeCall {
        client.post("$BASE_URL/checkin") {
            contentType(ContentType.Application.Json)
            setBody(CheckinRequest(uuid = playerUuid, timeSlot = slotTime))
        }
    }

    suspend fun getTournaments(
        city: String,
        from: LocalDate,
        until: LocalDate
    ): Result<List<Tournament>> = safeCall {
        client.get("$BASE_URL/tournaments") {
            parameter("city", city)
            parameter("from", from.toString())
            parameter("until", until.toString())
        }.body<List<TournamentDto>>().map { it.toDomain() }
    }

    // Wraps a network call in a Result, mapping connectivity failures to the domain NetworkError so
    // the presentation layer can show a "sem conexão" message. Unlike runCatching, this rethrows
    // CancellationException so coroutine cancellation is never swallowed.
    private inline fun <T> safeCall(block: () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpRequestTimeoutException) {
        Result.failure(NetworkError(e))
    } catch (e: ConnectTimeoutException) {
        Result.failure(NetworkError(e))
    } catch (e: SocketTimeoutException) {
        Result.failure(NetworkError(e))
    } catch (e: IOException) {
        Result.failure(NetworkError(e))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
