package com.tembt.platform

import android.content.Context
import com.tembt.domain.model.LocationMonitoringState
import com.tembt.domain.model.LocationUpdateInterval
import com.tembt.domain.model.MapCoordinates

private const val PREF_MONITORING_STATE = "tembt_location_monitoring_state"
private const val KEY_REFERENCE_LAT     = "reference_lat"
private const val KEY_REFERENCE_LNG     = "reference_lng"
private const val KEY_CONSECUTIVE_COUNT = "consecutive_same_spot_count"
private const val KEY_LAST_INTERVAL     = "last_effective_interval"

class LocationMonitoringStateStorageImpl(private val context: Context) : LocationMonitoringStateStorage {

    private val prefs = context.getSharedPreferences(PREF_MONITORING_STATE, Context.MODE_PRIVATE)

    override fun getState(): LocationMonitoringState? {
        if (!prefs.contains(KEY_REFERENCE_LAT)) return null
        val intervalName = prefs.getString(KEY_LAST_INTERVAL, null) ?: return null
        val interval = LocationUpdateInterval.entries.find { it.name == intervalName } ?: return null

        return LocationMonitoringState(
            referenceCoords = MapCoordinates(
                latitude = Double.fromBits(prefs.getLong(KEY_REFERENCE_LAT, 0L)),
                longitude = Double.fromBits(prefs.getLong(KEY_REFERENCE_LNG, 0L))
            ),
            consecutiveSameSpotCount = prefs.getInt(KEY_CONSECUTIVE_COUNT, 0),
            lastEffectiveInterval = interval
        )
    }

    override fun saveState(state: LocationMonitoringState) {
        prefs.edit()
            .putLong(KEY_REFERENCE_LAT, state.referenceCoords.latitude.toRawBits())
            .putLong(KEY_REFERENCE_LNG, state.referenceCoords.longitude.toRawBits())
            .putInt(KEY_CONSECUTIVE_COUNT, state.consecutiveSameSpotCount)
            .putString(KEY_LAST_INTERVAL, state.lastEffectiveInterval.name)
            .apply()
    }
}
