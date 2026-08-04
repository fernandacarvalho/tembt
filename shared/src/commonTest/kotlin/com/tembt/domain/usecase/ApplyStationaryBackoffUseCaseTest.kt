package com.tembt.domain.usecase

import com.tembt.domain.model.LocationMonitoringState
import com.tembt.domain.model.LocationUpdateInterval
import com.tembt.domain.model.MapCoordinates
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplyStationaryBackoffUseCaseTest {

    private val useCase = ApplyStationaryBackoffUseCase(CalculateDistanceUseCase())

    private val referencePoint = MapCoordinates(latitude = -22.9557, longitude = -43.1961)
    // ~1 m from referencePoint — well within the 30 m "same spot" threshold.
    private val sameSpotPoint = MapCoordinates(latitude = -22.95571, longitude = -43.1961)
    // ~56 m from referencePoint — well beyond the 30 m "same spot" threshold.
    private val movedPoint = MapCoordinates(latitude = -22.9552, longitude = -43.1961)

    @Test
    fun `given no previous state effective interval is the raw interval`() {
        val result = useCase(referencePoint, LocationUpdateInterval.CLOSE, previousState = null)

        assertEquals(1, result.consecutiveSameSpotCount)
        assertEquals(LocationUpdateInterval.CLOSE, result.lastEffectiveInterval)
        assertEquals(referencePoint, result.referenceCoords)
    }

    @Test
    fun `given user stays in the same spot for fewer than 3 cycles interval does not escalate`() {
        val afterFirst = useCase(sameSpotPoint, LocationUpdateInterval.CLOSE, previousState = null)
        val afterSecond = useCase(sameSpotPoint, LocationUpdateInterval.CLOSE, afterFirst)

        assertEquals(1, afterFirst.consecutiveSameSpotCount)
        assertEquals(2, afterSecond.consecutiveSameSpotCount)
        assertEquals(LocationUpdateInterval.CLOSE, afterSecond.lastEffectiveInterval)
    }

    @Test
    fun `given user stays in the same spot for 3 cycles interval escalates one step`() {
        var state: LocationMonitoringState? = null
        repeat(3) {
            state = useCase(sameSpotPoint, LocationUpdateInterval.CLOSE, state)
        }

        assertEquals(3, state!!.consecutiveSameSpotCount)
        // Ladder: VERY_CLOSE, CLOSE, MEDIUM, AT_COURT, FAR — CLOSE + 1 step = MEDIUM.
        assertEquals(LocationUpdateInterval.MEDIUM, state!!.lastEffectiveInterval)
    }

    @Test
    fun `given user stays in the same spot for 6 cycles interval escalates two steps`() {
        var state: LocationMonitoringState? = null
        repeat(6) {
            state = useCase(sameSpotPoint, LocationUpdateInterval.CLOSE, state)
        }

        assertEquals(6, state!!.consecutiveSameSpotCount)
        // CLOSE + 2 steps = AT_COURT.
        assertEquals(LocationUpdateInterval.AT_COURT, state!!.lastEffectiveInterval)
    }

    @Test
    fun `given user moves beyond the threshold consecutive count resets and interval is raw`() {
        var state: LocationMonitoringState? = null
        repeat(3) {
            state = useCase(sameSpotPoint, LocationUpdateInterval.CLOSE, state)
        }
        assertEquals(LocationUpdateInterval.MEDIUM, state!!.lastEffectiveInterval)

        val afterMove = useCase(movedPoint, LocationUpdateInterval.CLOSE, state)

        assertEquals(1, afterMove.consecutiveSameSpotCount)
        assertEquals(LocationUpdateInterval.CLOSE, afterMove.lastEffectiveInterval)
        assertEquals(movedPoint, afterMove.referenceCoords)
    }

    @Test
    fun `given escalation would exceed FAR effective interval is capped at FAR`() {
        var state: LocationMonitoringState? = null
        repeat(15) {
            state = useCase(sameSpotPoint, LocationUpdateInterval.CLOSE, state)
        }

        assertEquals(LocationUpdateInterval.FAR, state!!.lastEffectiveInterval)
    }

    @Test
    fun `given raw interval is already FAR staying still keeps it capped at FAR`() {
        var state: LocationMonitoringState? = null
        repeat(6) {
            state = useCase(sameSpotPoint, LocationUpdateInterval.FAR, state)
        }

        assertEquals(LocationUpdateInterval.FAR, state!!.lastEffectiveInterval)
    }
}
