package com.tembt.domain.usecase

import com.tembt.domain.model.MapCoordinates
import kotlin.test.Test
import kotlin.test.assertTrue

class CalculateDistanceUseCaseTest {

    private val useCase = CalculateDistanceUseCase()

    @Test
    fun `given same coordinates distance is zero`() {
        val coord = MapCoordinates(-22.9557, -43.1961)
        val distance = useCase(coord, coord)
        assertTrue(distance < 1.0, "Expected ~0 m, got $distance")
    }

    @Test
    fun `given coordinates 1 km apart distance is approximately 1000 m`() {
        // ~1 km north of reference point
        val user  = MapCoordinates(-22.9557, -43.1961)
        val court = MapCoordinates(-22.9467, -43.1961)   // ~1 km north
        val distance = useCase(user, court)
        assertTrue(distance in 900.0..1100.0, "Expected ~1000 m, got $distance")
    }

    @Test
    fun `given coordinates 5 km apart distance is approximately 5000 m`() {
        val user  = MapCoordinates(-22.9557, -43.1961)
        val court = MapCoordinates(-22.9107, -43.1961)   // ~5 km north
        val distance = useCase(user, court)
        assertTrue(distance in 4500.0..5500.0, "Expected ~5000 m, got $distance")
    }

    @Test
    fun `given coordinates in different hemispheres distance is positive`() {
        val user  = MapCoordinates(40.7128, -74.0060)    // New York
        val court = MapCoordinates(-22.9557, -43.1961)   // Rio de Janeiro
        val distance = useCase(user, court)
        // ~7500 km
        assertTrue(distance > 7_000_000.0, "Expected > 7 000 km, got $distance m")
    }

    @Test
    fun `distance calculation is symmetric`() {
        val a = MapCoordinates(-22.9557, -43.1961)
        val b = MapCoordinates(-23.0032, -43.3220)
        val ab = useCase(a, b)
        val ba = useCase(b, a)
        assertTrue(kotlin.math.abs(ab - ba) < 1.0, "Distance should be symmetric: $ab vs $ba")
    }
}
