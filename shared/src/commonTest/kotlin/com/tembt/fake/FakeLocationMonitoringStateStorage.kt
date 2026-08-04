package com.tembt.fake

import com.tembt.domain.model.LocationMonitoringState
import com.tembt.platform.LocationMonitoringStateStorage

class FakeLocationMonitoringStateStorage(
    private var state: LocationMonitoringState? = null
) : LocationMonitoringStateStorage {

    var saveCallCount = 0
    var lastSaved: LocationMonitoringState? = null

    override fun getState(): LocationMonitoringState? = state

    override fun saveState(state: LocationMonitoringState) {
        saveCallCount++
        lastSaved = state
        this.state = state
    }
}
