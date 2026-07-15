package com.tembt.data.repository

import com.tembt.domain.model.FeatureFlag
import com.tembt.domain.repository.FeatureFlagRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration.Companion.minutes

class FeatureFlagRepositoryImpl : FeatureFlagRepository {

    private val remoteConfig by lazy { Firebase.remoteConfig }
    private val initMutex = Mutex()
    private var initialized = false

    private suspend fun ensureInitialized() = initMutex.withLock {
        if (initialized) return@withLock
        remoteConfig.settings {
            // Within this window fetchAndActivate serves the local cache instantly (no network)
            minimumFetchInterval = 5.minutes
        }
        remoteConfig.setDefaults(
            *FeatureFlag.entries.map { it.key to it.defaultValue }.toTypedArray()
        )
        initialized = true
    }

    override suspend fun fetchAndActivate() {
        ensureInitialized()
        remoteConfig.fetchAndActivate()
    }

    override suspend fun activateLastFetched() {
        ensureInitialized()
        remoteConfig.activate()
    }

    // getValue instead of the inline get<T> operator: the inline helper was compiled
    // with JVM target 17 and cannot be inlined into this module's target 11 bytecode
    override fun isEnabled(flag: FeatureFlag): Boolean {
        val value = remoteConfig.getValue(flag.key).asBoolean()
        println("[DEBUG] ${flag.key} $value")
        return value
    }
}
