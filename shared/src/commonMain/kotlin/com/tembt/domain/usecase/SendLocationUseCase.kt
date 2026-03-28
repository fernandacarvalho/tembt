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
        val (lat, lng) = locationService.getCurrentLocation()
            ?: return Result.failure(IllegalStateException("Localização não disponível"))
        return locationRepository.updateLocation(
            uuid = playerStorage.getDeviceUuid(),
            lat = lat,
            lng = lng
        )
    }
}
