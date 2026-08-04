package com.tembt.platform

import com.tembt.domain.model.LocationMonitoringState

/** Persists [LocationMonitoringState] locally so it survives across monitoring cycles —
 * including iOS's isolated `BGTask` wakes, which may run in a fresh process each time. */
interface LocationMonitoringStateStorage {
    /** Returns the saved state, or null if no cycle has run yet. */
    fun getState(): LocationMonitoringState?

    /** Overwrites the locally stored state. */
    fun saveState(state: LocationMonitoringState)
}
