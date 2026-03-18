package com.tembt.fake

import com.tembt.domain.model.CourtSchedule
import com.tembt.platform.CourtScheduleStorage
import kotlinx.datetime.LocalDate

class FakeCourtScheduleStorage(
    private var schedule: CourtSchedule = CourtSchedule()
) : CourtScheduleStorage {

    var saveCallCount = 0
    var lastSaved: CourtSchedule? = null
    private var pausedDate: LocalDate? = null

    override fun getSchedule(): CourtSchedule = schedule

    override fun saveSchedule(schedule: CourtSchedule) {
        saveCallCount++
        lastSaved = schedule
        this.schedule = schedule
    }

    override fun pauseMonitoringForToday(today: LocalDate) {
        pausedDate = today
    }

    override fun isMonitoringPausedFor(date: LocalDate): Boolean = pausedDate == date
}
