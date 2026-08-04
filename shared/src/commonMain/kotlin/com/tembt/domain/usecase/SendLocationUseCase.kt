package com.tembt.domain.usecase

import com.tembt.domain.repository.LocationRepository
import com.tembt.platform.LocationServiceContract
import com.tembt.platform.PlayerStorage

class SendLocationUseCase(
    private val locationRepository: LocationRepository,
    private val locationService: LocationServiceContract,
    private val playerStorage: PlayerStorage
) : SendLocation {
    override suspend operator fun invoke(): Result<Unit> {
        println("[TEMBT-DEBUG] SendLocation: chamando getCurrentLocation()")
        // highAccuracy = false: SendLocation always runs right after the coordinator's own
        // (correctly-tiered) read, so this normally reuses that cached fix rather than
        // issuing a new platform request.
        val coords = locationService.getCurrentLocation(highAccuracy = false)
        println("[TEMBT-DEBUG] SendLocation: getCurrentLocation() retornou = $coords")
        val (lat, lng) = coords ?: run {
            println("[TEMBT-DEBUG] SendLocation: ERRO — localização não disponível, abortando envio")
            return Result.failure(IllegalStateException("Localização não disponível"))
        }
        val uuid = playerStorage.getDeviceUuid()
        println("[TEMBT-DEBUG] SendLocation: enviando para API — uuid=$uuid lat=$lat lng=$lng")
        val result = locationRepository.updateLocation(uuid = uuid, lat = lat, lng = lng)
        println("[TEMBT-DEBUG] SendLocation: resposta da API = $result")
        return result
    }
}
