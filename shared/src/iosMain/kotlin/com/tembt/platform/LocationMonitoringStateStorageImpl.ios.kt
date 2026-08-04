package com.tembt.platform

import com.tembt.domain.model.LocationMonitoringState
import com.tembt.domain.model.LocationUpdateInterval
import com.tembt.domain.model.MapCoordinates
import platform.Foundation.NSUserDefaults

private const val KEY_REFERENCE_LAT     = "tembt_monitoring_reference_lat"
private const val KEY_REFERENCE_LNG     = "tembt_monitoring_reference_lng"
private const val KEY_CONSECUTIVE_COUNT = "tembt_monitoring_consecutive_same_spot_count"
private const val KEY_LAST_INTERVAL     = "tembt_monitoring_last_effective_interval"

class LocationMonitoringStateStorageImpl : LocationMonitoringStateStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getState(): LocationMonitoringState? {
        val intervalName = defaults.stringForKey(KEY_LAST_INTERVAL) ?: return null
        val interval = LocationUpdateInterval.entries.find { it.name == intervalName } ?: return null

        return LocationMonitoringState(
            referenceCoords = MapCoordinates(
                latitude = defaults.doubleForKey(KEY_REFERENCE_LAT),
                longitude = defaults.doubleForKey(KEY_REFERENCE_LNG)
            ),
            consecutiveSameSpotCount = defaults.integerForKey(KEY_CONSECUTIVE_COUNT).toInt(),
            lastEffectiveInterval = interval
        )
    }

    override fun saveState(state: LocationMonitoringState) {
        defaults.setDouble(state.referenceCoords.latitude, KEY_REFERENCE_LAT)
        defaults.setDouble(state.referenceCoords.longitude, KEY_REFERENCE_LNG)
        defaults.setInteger(state.consecutiveSameSpotCount.toLong(), KEY_CONSECUTIVE_COUNT)
        defaults.setObject(state.lastEffectiveInterval.name, KEY_LAST_INTERVAL)
        defaults.synchronize()
    }
}
