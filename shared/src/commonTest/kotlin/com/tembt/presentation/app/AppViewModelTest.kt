package com.tembt.presentation.app

import com.tembt.domain.model.AppTab
import com.tembt.domain.model.FeatureFlag
import com.tembt.domain.usecase.GetEnabledTabsUseCase
import com.tembt.domain.usecase.SessionTabConfig
import com.tembt.fake.FakeFeatureFlagRepository
import com.tembt.fake.FakePlayerStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    private val featureFlagRepository = FakeFeatureFlagRepository()

    private fun sessionTabConfig() = SessionTabConfig(GetEnabledTabsUseCase(featureFlagRepository))

    @Test
    fun `given user not registered showWelcome is true`() = runTest(testDispatcher) {
        val storage = FakePlayerStorage().apply { registered = false }

        val vm = AppViewModel(storage, sessionTabConfig())

        assertTrue(vm.showWelcome.value)
    }

    @Test
    fun `given user registered showWelcome is false`() = runTest(testDispatcher) {
        val storage = FakePlayerStorage().apply { registered = true }

        val vm = AppViewModel(storage, sessionTabConfig())

        assertFalse(vm.showWelcome.value)
    }

    @Test
    fun `when onRegistered called showWelcome becomes false`() = runTest(testDispatcher) {
        val storage = FakePlayerStorage().apply { registered = false }
        val vm = AppViewModel(storage, sessionTabConfig())

        vm.onRegistered()

        assertFalse(vm.showWelcome.value)
    }

    @Test
    fun `given tabs not yet resolved enabledTabs is null`() = runTest(testDispatcher) {
        val vm = AppViewModel(FakePlayerStorage(), sessionTabConfig())

        assertNull(vm.enabledTabs.value)
    }

    @Test
    fun `when init completes enabledTabs contains resolved tabs`() = runTest(testDispatcher) {
        featureFlagRepository.setFlag(FeatureFlag.TAB_SCHEDULE, true)
        featureFlagRepository.setFlag(FeatureFlag.TAB_TOURNAMENTS, true)

        val vm = AppViewModel(FakePlayerStorage(), sessionTabConfig())
        advanceUntilIdle()

        assertEquals(listOf(AppTab.MAP, AppTab.SCHEDULE, AppTab.TOURNAMENTS), vm.enabledTabs.value)
    }

    @Test
    fun `given fetch and activate fail enabledTabs falls back to map only`() = runTest(testDispatcher) {
        featureFlagRepository.fetchThrows = true
        featureFlagRepository.activateThrows = true

        val vm = AppViewModel(FakePlayerStorage(), sessionTabConfig())
        advanceUntilIdle()

        assertEquals(listOf(AppTab.MAP), vm.enabledTabs.value)
    }
}
