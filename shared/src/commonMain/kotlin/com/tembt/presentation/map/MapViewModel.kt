package com.tembt.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tembt.domain.model.LocationPermissionStatus
import com.tembt.domain.usecase.GetCourtLocation
import com.tembt.domain.usecase.GetPlayersAtCourt
import com.tembt.domain.usecase.SendLocation
import com.tembt.platform.LocationServiceContract
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Note: `androidx.lifecycle.ViewModel` is available in commonMain via the KMP artifact
// `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` — this is not an Android import leak.
class MapViewModel(
    private val locationService: LocationServiceContract,
    private val getCourtLocation: GetCourtLocation,
    private val getPlayersAtCourt: GetPlayersAtCourt,
    private val sendLocationUseCase: SendLocation,
    private val pollIntervalMs: Long = POLL_INTERVAL_MS
) : ViewModel() {

    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<MapUiEvent>()
    val uiEvent: SharedFlow<MapUiEvent> = _uiEvent.asSharedFlow()

    // Tracks the in-flight fetch so concurrent checkPermission() calls don't spawn duplicate requests
    private var fetchJob: Job? = null
    private var pollingJob: Job? = null

    init {
        checkPermission()
    }

    fun onResume() = checkPermission()

    fun onPause() {
        println("[TEMBT-DEBUG] MapViewModel.onPause: cancelando polling")
        pollingJob?.cancel()
        pollingJob = null
    }

    fun onPermissionResult() {
        locationService.markPermissionRequested()
        checkPermission()
    }

    fun onOpenSettingsRequested() {
        viewModelScope.launch { _uiEvent.emit(MapUiEvent.OpenAppSettings) }
    }

    fun checkPermission() {
        val status = locationService.getPermissionStatus()
        val bgStatus = locationService.getBackgroundPermissionStatus()
        println("[TEMBT-DEBUG] MapViewModel.checkPermission: status=$status bgStatus=$bgStatus uiState=${_uiState.value}")
        when (status) {
            LocationPermissionStatus.GRANTED -> {
                val bgGranted = bgStatus == LocationPermissionStatus.GRANTED
                if (bgGranted) {
                    if (_uiState.value is MapUiState.MapReady) {
                        println("[TEMBT-DEBUG] MapViewModel.checkPermission: já está MapReady, reiniciando polling")
                        startPolling()
                        return
                    }
                    fetchCourtAndShowMap()
                } else {
                    println("[TEMBT-DEBUG] MapViewModel.checkPermission: permissão de background não concedida")
                    _uiState.value = MapUiState.BackgroundPermissionRequired
                }
            }
            LocationPermissionStatus.DENIED -> {
                println("[TEMBT-DEBUG] MapViewModel.checkPermission: permissão NEGADA")
                _uiState.value = MapUiState.PermissionRequired(LocationPermissionStatus.DENIED)
            }
            LocationPermissionStatus.NOT_DETERMINED -> {
                println("[TEMBT-DEBUG] MapViewModel.checkPermission: permissão NOT_DETERMINED")
                _uiState.value = MapUiState.PermissionRequired(LocationPermissionStatus.NOT_DETERMINED)
            }
        }
    }

    private fun fetchCourtAndShowMap() {
        if (fetchJob?.isActive == true) {
            println("[TEMBT-DEBUG] MapViewModel.fetchCourtAndShowMap: fetch já em andamento, ignorando")
            return
        }
        fetchJob = viewModelScope.launch {
            println("[TEMBT-DEBUG] MapViewModel.fetchCourtAndShowMap: iniciando busca da quadra")
            _uiState.value = MapUiState.Loading
            getCourtLocation().fold(
                onSuccess = { center ->
                    println("[TEMBT-DEBUG] MapViewModel.fetchCourtAndShowMap: quadra recebida — lat=${center.latitude} lng=${center.longitude} name=${center.name}")
                    _uiState.value = MapUiState.MapReady(center = center, courtName = center.name)
                    val sendResult = sendLocationUseCase()
                    println("[TEMBT-DEBUG] MapViewModel.fetchCourtAndShowMap: sendLocation resultado = $sendResult")
                    doRefreshPlayers()
                    startPolling()
                },
                onFailure = { err ->
                    println("[TEMBT-DEBUG] MapViewModel.fetchCourtAndShowMap: ERRO ao buscar quadra — ${err.message}")
                    _uiState.value = MapUiState.Error(
                        err.message ?: "Erro ao carregar a quadra."
                    )
                }
            )
        }
    }

    fun refreshPlayers() {
        println("[TEMBT-DEBUG] MapViewModel.refreshPlayers: chamado — reiniciando polling")
        pollingJob?.cancel()
        viewModelScope.launch {
            doRefreshPlayers()
            startPolling()
        }
    }

    fun sendLocation() {
        println("[TEMBT-DEBUG] MapViewModel.sendLocation: disparando envio de localização")
        viewModelScope.launch {
            val result = sendLocationUseCase()
            println("[TEMBT-DEBUG] MapViewModel.sendLocation: resultado = $result")
        }
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(pollIntervalMs)
                doRefreshPlayers()
            }
        }
    }

    private suspend fun doRefreshPlayers() {
        val result = getPlayersAtCourt()
        println("[TEMBT-DEBUG] MapViewModel.doRefreshPlayers: resultado = $result")
        result.onSuccess { players ->
            println("[TEMBT-DEBUG] MapViewModel.doRefreshPlayers: jogadores recebidos — count=${players.size}")
            val current = _uiState.value
            if (current is MapUiState.MapReady) {
                _uiState.value = current.copy(
                    players = players,
                    lastUpdatedAt = currentTimeString()
                )
            }
        }
        result.onFailure { err ->
            println("[TEMBT-DEBUG] MapViewModel.doRefreshPlayers: ERRO — ${err.message}")
        }
    }

    private fun currentTimeString(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.of("America/Sao_Paulo"))
        return "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
    }

    companion object {
        private const val POLL_INTERVAL_MS = 5 * 60 * 1_000L
    }
}
