package com.tembt.domain.usecase

import com.tembt.domain.model.AppTab
import com.tembt.domain.model.FeatureFlag
import com.tembt.fake.FakeFeatureFlagRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SessionTabConfigTest {

    private val repository = FakeFeatureFlagRepository()
    private val sessionTabConfig = SessionTabConfig(GetEnabledTabsUseCase(repository))

    @Test
    fun `given flags change after first resolution cached tabs remain unchanged`() = runTest {
        repository.setFlag(FeatureFlag.TAB_SCHEDULE, false)
        val first = sessionTabConfig.tabs()

        repository.setFlag(FeatureFlag.TAB_SCHEDULE, true)
        val second = sessionTabConfig.tabs()

        assertEquals(listOf(AppTab.MAP), first)
        assertEquals(first, second)
    }

    @Test
    fun `when tabs requested twice use case runs once`() = runTest {
        sessionTabConfig.tabs()
        sessionTabConfig.tabs()

        assertEquals(1, repository.fetchAndActivateCallCount)
    }
}
