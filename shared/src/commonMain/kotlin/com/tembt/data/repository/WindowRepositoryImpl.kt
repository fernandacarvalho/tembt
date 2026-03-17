package com.tembt.data.repository

import com.tembt.data.remote.TembtApiService
import com.tembt.domain.model.ScheduleWindow
import com.tembt.domain.repository.WindowRepository

class WindowRepositoryImpl(private val api: TembtApiService) : WindowRepository {
    override suspend fun getWindow(): Result<ScheduleWindow> = api.getWindow()
    override suspend fun checkin(playerUuid: String, slotTime: String): Result<Unit> =
        api.checkin(playerUuid, slotTime)
}
