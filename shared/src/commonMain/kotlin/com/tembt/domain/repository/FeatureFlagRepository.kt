package com.tembt.domain.repository

import com.tembt.domain.model.FeatureFlag

interface FeatureFlagRepository {

    suspend fun fetchAndActivate()

    // Applies the values fetched in a previous session without hitting the network
    suspend fun activateLastFetched()

    fun isEnabled(flag: FeatureFlag): Boolean
}
