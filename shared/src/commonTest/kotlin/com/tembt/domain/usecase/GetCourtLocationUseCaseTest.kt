package com.tembt.domain.usecase

import com.tembt.domain.model.MapCoordinates
import com.tembt.fake.FakeCourtRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCourtLocationUseCaseTest {

    private val courtRepo = FakeCourtRepository()
    private val useCase   = GetCourtLocationUseCase(courtRepo)

    @Test
    fun `given repository succeeds returns coordinates`() = runTest {
        val coords = MapCoordinates(latitude = -22.9557, longitude = -43.1961, name = "Quadra")
        courtRepo.willReturn(Result.success(coords))

        val result = useCase()

        assertEquals(coords, result.getOrNull())
    }

    @Test
    fun `given repository succeeds delegates to repository`() = runTest {
        courtRepo.willReturn(Result.success(MapCoordinates(0.0, 0.0)))

        useCase()

        assertEquals(1, courtRepo.callCount)
    }

    @Test
    fun `given repository fails propagates failure`() = runTest {
        courtRepo.willReturn(Result.failure(RuntimeException("Network error")))

        val result = useCase()

        assertTrue(result.isFailure)
    }
}
