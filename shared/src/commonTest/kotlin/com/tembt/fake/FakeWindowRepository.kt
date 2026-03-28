package com.tembt.fake

import com.tembt.domain.model.ScheduleWindow
import com.tembt.domain.repository.WindowRepository
import kotlinx.coroutines.CompletableDeferred

class FakeWindowRepository : WindowRepository {

    private var windowResult: Result<ScheduleWindow> =
        Result.success(ScheduleWindow("2026-03-17", startHour = 6, endHour = 16, slots = emptyList()))
    private var checkinResult: Result<Unit> = Result.success(Unit)

    var getWindowCallCount = 0
    var checkinCallCount = 0
    var lastCheckinUuid: String? = null
    var lastCheckinSlot: String? = null

    // When set, checkin suspends until this deferred is completed — use to test in-flight guards
    var checkinDeferred: CompletableDeferred<Result<Unit>>? = null

    fun willReturnWindow(result: Result<ScheduleWindow>) { this.windowResult = result }
    fun willReturnCheckin(result: Result<Unit>) { this.checkinResult = result }

    override suspend fun getWindow(): Result<ScheduleWindow> {
        getWindowCallCount++
        return windowResult
    }

    override suspend fun checkin(playerUuid: String, slotTime: String): Result<Unit> {
        checkinCallCount++
        lastCheckinUuid = playerUuid
        lastCheckinSlot = slotTime
        return checkinDeferred?.await() ?: checkinResult
    }
}
