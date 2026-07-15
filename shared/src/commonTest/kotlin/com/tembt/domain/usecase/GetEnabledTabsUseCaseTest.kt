package com.tembt.domain.usecase

import com.tembt.domain.model.AppTab
import com.tembt.domain.model.FeatureFlag
import com.tembt.fake.FakeFeatureFlagRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetEnabledTabsUseCaseTest {

    private val repository = FakeFeatureFlagRepository()
    private val useCase = GetEnabledTabsUseCase(repository)

    @Test
    fun `given both flags off only map tab is enabled`() = runTest {
        repository.setFlag(FeatureFlag.TAB_SCHEDULE, false)
        repository.setFlag(FeatureFlag.TAB_TOURNAMENTS, false)

        val tabs = useCase()

        assertEquals(listOf(AppTab.MAP), tabs)
    }

    @Test
    fun `given schedule flag on tabs are map and schedule in order`() = runTest {
        repository.setFlag(FeatureFlag.TAB_SCHEDULE, true)
        repository.setFlag(FeatureFlag.TAB_TOURNAMENTS, false)

        val tabs = useCase()

        assertEquals(listOf(AppTab.MAP, AppTab.SCHEDULE), tabs)
    }

    @Test
    fun `given tournaments flag on tabs are map and tournaments`() = runTest {
        repository.setFlag(FeatureFlag.TAB_SCHEDULE, false)
        repository.setFlag(FeatureFlag.TAB_TOURNAMENTS, true)

        val tabs = useCase()

        assertEquals(listOf(AppTab.MAP, AppTab.TOURNAMENTS), tabs)
    }

    @Test
    fun `given both flags on tabs are map schedule and tournaments in order`() = runTest {
        repository.setFlag(FeatureFlag.TAB_SCHEDULE, true)
        repository.setFlag(FeatureFlag.TAB_TOURNAMENTS, true)

        val tabs = useCase()

        assertEquals(listOf(AppTab.MAP, AppTab.SCHEDULE, AppTab.TOURNAMENTS), tabs)
    }

    @Test
    fun `given no flags fetched defaults produce only map`() = runTest {
        val tabs = useCase()

        assertEquals(listOf(AppTab.MAP), tabs)
    }

    @Test
    fun `given fetch exceeds timeout activateLastFetched is used as fallback`() = runTest {
        repository.fetchDelayMillis = GetEnabledTabsUseCase.DEFAULT_FETCH_TIMEOUT_MILLIS + 1

        useCase()

        assertEquals(1, repository.activateLastFetchedCallCount)
    }

    @Test
    fun `given fetch fails activateLastFetched is called`() = runTest {
        repository.fetchThrows = true

        useCase()

        assertEquals(1, repository.activateLastFetchedCallCount)
    }

    @Test
    fun `given fetch and activate fail tabs fall back to defaults`() = runTest {
        repository.fetchThrows = true
        repository.activateThrows = true

        val tabs = useCase()

        assertEquals(listOf(AppTab.MAP), tabs)
    }

    @Test
    fun `given fetch succeeds fallback activate is not called`() = runTest {
        useCase()

        assertEquals(1, repository.fetchAndActivateCallCount)
        assertEquals(0, repository.activateLastFetchedCallCount)
    }

    @Test
    fun `given fetch exceeds timeout flags still resolve from last activated values`() = runTest {
        repository.fetchDelayMillis = GetEnabledTabsUseCase.DEFAULT_FETCH_TIMEOUT_MILLIS + 1
        repository.setFlag(FeatureFlag.TAB_SCHEDULE, true)

        val tabs = useCase()

        assertEquals(listOf(AppTab.MAP, AppTab.SCHEDULE), tabs)
    }
}
