package com.tembt.domain.usecase

import com.tembt.domain.model.LocationUpdateInterval
import kotlin.test.Test
import kotlin.test.assertEquals

class GetLocationUpdateIntervalUseCaseTest {

    private val useCase = GetLocationUpdateIntervalUseCase()

    // --- AT_COURT (< 50 m) ---

    @Test
    fun `given distance 0 m interval is AT_COURT`() {
        assertEquals(LocationUpdateInterval.AT_COURT, useCase(0.0))
    }

    @Test
    fun `given distance 49 m interval is AT_COURT`() {
        assertEquals(LocationUpdateInterval.AT_COURT, useCase(49.9))
    }

    // --- VERY_CLOSE (50 m – 999 m) ---

    @Test
    fun `given distance exactly 50 m interval is VERY_CLOSE`() {
        assertEquals(LocationUpdateInterval.VERY_CLOSE, useCase(50.0))
    }

    @Test
    fun `given distance 999 m interval is VERY_CLOSE`() {
        assertEquals(LocationUpdateInterval.VERY_CLOSE, useCase(999.9))
    }

    // --- CLOSE (1 000 m – 4 999 m) ---

    @Test
    fun `given distance exactly 1000 m interval is CLOSE`() {
        assertEquals(LocationUpdateInterval.CLOSE, useCase(1_000.0))
    }

    @Test
    fun `given distance 4999 m interval is CLOSE`() {
        assertEquals(LocationUpdateInterval.CLOSE, useCase(4_999.9))
    }

    // --- MEDIUM (5 000 m – 9 999 m) ---

    @Test
    fun `given distance exactly 5000 m interval is MEDIUM`() {
        assertEquals(LocationUpdateInterval.MEDIUM, useCase(5_000.0))
    }

    @Test
    fun `given distance 9999 m interval is MEDIUM`() {
        assertEquals(LocationUpdateInterval.MEDIUM, useCase(9_999.9))
    }

    // --- FAR (≥ 10 000 m) ---

    @Test
    fun `given distance exactly 10000 m interval is FAR`() {
        assertEquals(LocationUpdateInterval.FAR, useCase(10_000.0))
    }

    @Test
    fun `given distance 100 km interval is FAR`() {
        assertEquals(LocationUpdateInterval.FAR, useCase(100_000.0))
    }

    // --- Minute values ---

    @Test
    fun `AT_COURT interval is 40 minutes`() {
        assertEquals(40, LocationUpdateInterval.AT_COURT.minutes)
    }

    @Test
    fun `VERY_CLOSE interval is 5 minutes`() {
        assertEquals(5, LocationUpdateInterval.VERY_CLOSE.minutes)
    }

    @Test
    fun `CLOSE interval is 10 minutes`() {
        assertEquals(10, LocationUpdateInterval.CLOSE.minutes)
    }

    @Test
    fun `MEDIUM interval is 20 minutes`() {
        assertEquals(20, LocationUpdateInterval.MEDIUM.minutes)
    }

    @Test
    fun `FAR interval is 60 minutes`() {
        assertEquals(60, LocationUpdateInterval.FAR.minutes)
    }
}
