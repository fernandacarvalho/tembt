package com.tembt.presentation.app

import com.tembt.fake.FakePlayerStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `given user not registered showWelcome is true`() = runTest(testDispatcher) {
        val storage = FakePlayerStorage().apply { registered = false }

        val vm = AppViewModel(storage)

        assertTrue(vm.showWelcome.value)
    }

    @Test
    fun `given user registered showWelcome is false`() = runTest(testDispatcher) {
        val storage = FakePlayerStorage().apply { registered = true }

        val vm = AppViewModel(storage)

        assertFalse(vm.showWelcome.value)
    }

    @Test
    fun `when onRegistered called showWelcome becomes false`() = runTest(testDispatcher) {
        val storage = FakePlayerStorage().apply { registered = false }
        val vm = AppViewModel(storage)

        vm.onRegistered()

        assertFalse(vm.showWelcome.value)
    }
}
