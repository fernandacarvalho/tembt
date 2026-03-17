package com.tembt.domain.repository

import com.tembt.domain.model.ScheduleWindow

interface WindowRepository {
    suspend fun getWindow(): Result<ScheduleWindow>
    suspend fun checkin(playerUuid: String, slotTime: String): Result<Unit>
}
