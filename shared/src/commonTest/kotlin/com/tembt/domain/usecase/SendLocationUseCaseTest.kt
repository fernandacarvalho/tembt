package com.tembt.domain.usecase

import com.tembt.fake.FakeLocationRepository
import com.tembt.fake.FakeLocationService
import com.tembt.fake.FakePlayerStorage
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SendLocationUseCaseTest {

    private val locationRepo    = FakeLocationRepository()
    private val locationService = FakeLocationService()
    private val playerStorage   = FakePlayerStorage(uuid = "uuid-42")

    private fun createUseCase() = SendLocationUseCase(locationRepo, locationService, playerStorage)

    @Test
    fun `given location available invokes updateLocation with correct args`() = runTest {
        // Arrange
        locationService.location = Pair(-22.9557, -43.1961)

        // Act
        createUseCase()()

        // Assert
        assertEquals(1, locationRepo.callCount)
        assertEquals("uuid-42", locationRepo.lastUuid)
        assertEquals(-22.9557, locationRepo.lastLat)
        assertEquals(-43.1961, locationRepo.lastLng)
    }

    @Test
    fun `given location available and API succeeds returns success`() = runTest {
        // Arrange
        locationService.location = Pair(-22.9557, -43.1961)
        locationRepo.willReturn(Result.success(Unit))

        // Act
        val result = createUseCase()()

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `given location available and API fails returns failure`() = runTest {
        // Arrange
        locationService.location = Pair(-22.9557, -43.1961)
        locationRepo.willReturn(Result.failure(RuntimeException("Network error")))

        // Act
        val result = createUseCase()()

        // Assert
        assertTrue(result.isFailure)
    }

    @Test
    fun `given location unavailable returns failure without calling API`() = runTest {
        // Arrange
        locationService.location = null

        // Act
        val result = createUseCase()()

        // Assert
        assertTrue(result.isFailure)
        assertEquals(0, locationRepo.callCount)
    }

    @Test
    fun `given location unavailable failure message is set`() = runTest {
        // Arrange
        locationService.location = null

        // Act
        val result = createUseCase()()

        // Assert
        assertFalse(result.exceptionOrNull()?.message.isNullOrBlank())
    }
}
