package com.tembt.fake

import com.tembt.platform.CourtMonitoringScheduler

class FakeCourtMonitoringScheduler : CourtMonitoringScheduler {

    var scheduleCallCount = 0
    var lastScheduledHour: Int? = null
    var cancelCallCount = 0

    override fun schedule(startHourOfDay: Int) {
        scheduleCallCount++
        lastScheduledHour = startHourOfDay
    }

    override fun cancel() {
        cancelCallCount++
    }
}
