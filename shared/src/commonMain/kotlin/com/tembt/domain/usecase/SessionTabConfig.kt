package com.tembt.domain.usecase

import com.tembt.domain.model.AppTab
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Registered as a Koin single: tabs are resolved once per process and frozen for the
// whole session so the user never sees tabs appear or disappear while the app is open.
class SessionTabConfig(private val getEnabledTabs: GetEnabledTabsUseCase) {

    private val mutex = Mutex()
    private var cached: List<AppTab>? = null

    suspend fun tabs(): List<AppTab> = mutex.withLock {
        cached ?: getEnabledTabs().also { cached = it }
    }
}
