package com.tembt.fake

import com.tembt.domain.model.FeatureFlag
import com.tembt.domain.repository.FeatureFlagRepository
import kotlinx.coroutines.delay

class FakeFeatureFlagRepository : FeatureFlagRepository {

    private val flags = mutableMapOf<FeatureFlag, Boolean>()

    var fetchAndActivateCallCount = 0
    var activateLastFetchedCallCount = 0

    // When > 0, fetchAndActivate suspends for this long (virtual time) — use to test the timeout
    var fetchDelayMillis = 0L
    var fetchThrows = false
    var activateThrows = false

    fun setFlag(flag: FeatureFlag, enabled: Boolean) {
        flags[flag] = enabled
    }

    override suspend fun fetchAndActivate() {
        fetchAndActivateCallCount++
        if (fetchDelayMillis > 0) delay(fetchDelayMillis)
        if (fetchThrows) throw RuntimeException("fetch failed")
    }

    override suspend fun activateLastFetched() {
        activateLastFetchedCallCount++
        if (activateThrows) throw RuntimeException("activate failed")
    }

    override fun isEnabled(flag: FeatureFlag): Boolean = flags[flag] ?: flag.defaultValue
}
