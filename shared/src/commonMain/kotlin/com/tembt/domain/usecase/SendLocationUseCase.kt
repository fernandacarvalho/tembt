package com.tembt.domain.usecase

import com.tembt.data.remote.TembtApiService
import com.tembt.platform.LocationServiceContract
import com.tembt.platform.PlayerStorage

class SendLocationUseCase(
    private val api: TembtApiService,
    private val locationService: LocationServiceContract,
    private val playerStorage: PlayerStorage
) : SendLocation {
    override suspend operator fun invoke(): Result<Unit> {
        val (lat, lng) = locationService.getCurrentLocation()
            ?: return Result.failure(IllegalStateException("Localização não disponível"))
        return api.updateLocation(
            uuid = playerStorage.getDeviceUuid(),
            lat = lat,
            lng = lng
        )
    }
}
