package com.tembt.platform

import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitDay
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.NSCalendarUnitMonth
import platform.Foundation.NSCalendarUnitSecond
import platform.Foundation.NSCalendarUnitYear
import platform.Foundation.NSDate
import platform.Foundation.NSTimeZone
import platform.Foundation.timeZoneWithName
import platform.Foundation.timeZoneForSecondsFromGMT
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS background-task scheduler.
 *
 * Uses [BGTaskScheduler] (iOS 13+) to fire the court-monitoring check at
 * [startHourOfDay] in the São Paulo timezone. iOS does not guarantee exact
 * delivery — it treats the requested time as the *earliest* begin date and
 * may delay the task depending on battery, network, and usage patterns.
 *
 * **Registration requirement**: the task identifier [TASK_IDENTIFIER] must be
 * listed in Info.plist under `BGTaskSchedulerPermittedIdentifiers` and
 * [registerHandler] must be called before `applicationDidFinishLaunching`
 * completes (see [com.tembt.ios.KoinHelper]).
 *
 * **Background location**: once the task fires and determines the court is
 * open, iOS requires `allowsBackgroundLocationUpdates = true` on
 * CLLocationManager (set in the Swift layer) and the "Location updates"
 * background mode in Xcode capabilities.
 */
@OptIn(ExperimentalForeignApi::class)
class CourtMonitoringSchedulerImpl : CourtMonitoringScheduler {

    override fun schedule(startHourOfDay: Int) {
        val request = BGAppRefreshTaskRequest(TASK_IDENTIFIER)
        request.earliestBeginDate = nextOccurrenceNSDate(startHourOfDay)
        BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
    }

    override fun cancel() {
        BGTaskScheduler.sharedScheduler
            .cancelTaskRequestWithIdentifier(TASK_IDENTIFIER)
    }

    private fun nextOccurrenceNSDate(hour: Int): NSDate {
        // Fall back to UTC-3 (São Paulo standard offset) if the named timezone is unavailable.
        val saoPaulo = NSTimeZone.timeZoneWithName("America/Sao_Paulo")
            ?: NSTimeZone.timeZoneForSecondsFromGMT(-3 * 3600)
        val calendar = NSCalendar.currentCalendar.apply {
            timeZone = saoPaulo
        }
        val now        = NSDate()
        val components = calendar.components(
            NSCalendarUnitYear or NSCalendarUnitMonth or NSCalendarUnitDay or
            NSCalendarUnitHour or NSCalendarUnitMinute or NSCalendarUnitSecond,
            fromDate = now
        )
        components.hour   = hour.toLong()
        components.minute = 0
        components.second = 0

        var target = calendar.dateFromComponents(components) ?: now
        // If the target time has already passed today, move to tomorrow.
        // Compare via timeIntervalSinceReferenceDate to avoid using timeIntervalSinceDate:
        // which is unavailable in Kotlin/Native bindings for the current iOS SDK.
        if (target.timeIntervalSinceReferenceDate <= now.timeIntervalSinceReferenceDate) {
            components.day = components.day + 1
            target = calendar.dateFromComponents(components) ?: now
        }
        return target
    }

    companion object {
        const val TASK_IDENTIFIER = "com.tembt.court.monitoring"
    }
}
