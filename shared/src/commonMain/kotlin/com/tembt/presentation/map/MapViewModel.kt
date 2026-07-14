package com.tembt.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tembt.domain.model.LocationPermissionStatus
import com.tembt.domain.model.NetworkError
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

    // Lifecycle-driven re-check (resume, permission result). Never auto-retries a failed fetch —
    // an Error state stays put so the user sees feedback and taps retry, avoiding Loading↔Error loops.
    fun checkPermission() = evaluatePermission(fetchFromError = false)

    // User-initiated retry from the Error screen. Re-checks permission first (it may have been
    // revoked while the error was on screen) and only re-fetches when permission is still granted.
    fun retry() = evaluatePermission(fetchFromError = true)

    private fun evaluatePermission(fetchFromError: Boolean) {
        val status = locationService.getPermissionStatus()
        val bgStatus = locationService.getBackgroundPermissionStatus()
        println("[TEMBT-DEBUG] MapViewModel.evaluatePermission: status=$status bgStatus=$bgStatus fetchFromError=$fetchFromError uiState=${_uiState.value}")
        when (status) {
            LocationPermissionStatus.GRANTED -> {
                val bgGranted = bgStatus == LocationPermissionStatus.GRANTED
                if (bgGranted) {
                    when {
                        _uiState.value is MapUiState.MapReady -> {
                            println("[TEMBT-DEBUG] MapViewModel.evaluatePermission: já está MapReady, reiniciando polling")
                            startPolling()
                        }
                        _uiState.value is MapUiState.Error && !fetchFromError -> {
                            println("[TEMBT-DEBUG] MapViewModel.evaluatePermission: em Error, aguardando retry do usuário")
                        }
                        else -> fetchCourtAndShowMap()
                    }
                } else {
                    println("[TEMBT-DEBUG] MapViewModel.evaluatePermission: permissão de background não concedida")
                    _uiState.value = MapUiState.BackgroundPermissionRequired
                }
            }
            LocationPermissionStatus.DENIED -> {
                println("[TEMBT-DEBUG] MapViewModel.evaluatePermission: permissão NEGADA")
                _uiState.value = MapUiState.PermissionRequired(LocationPermissionStatus.DENIED)
            }
            LocationPermissionStatus.NOT_DETERMINED -> {
                println("[TEMBT-DEBUG] MapViewModel.evaluatePermission: permissão NOT_DETERMINED")
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
                    _uiState.value = MapUiState.Error(errorMessageFor(err))
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

    private fun errorMessageFor(err: Throwable): String =
        if (err is NetworkError) {
            "Sem conexão. Verifique sua internet e tente novamente."
        } else {
            "Erro ao carregar a quadra."
        }

    private fun currentTimeString(): String {
        val now = Clock.System.now().toLocalDateTime(TimeZone.of("America/Sao_Paulo"))
        return "${now.hour.toString().padStart(2, '0')}:${now.minute.toString().padStart(2, '0')}"
    }

    companion object {
        private const val POLL_INTERVAL_MS = 5 * 60 * 1_000L
    }
}
