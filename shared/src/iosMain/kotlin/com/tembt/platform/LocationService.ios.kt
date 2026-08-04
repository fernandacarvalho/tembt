package com.tembt.platform

import com.tembt.domain.model.LocationPermissionStatus
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreLocation.kCLLocationAccuracyHundredMeters
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.seconds

private val HIGH_ACCURACY_TIMEOUT = 20.seconds
private val LOW_POWER_TIMEOUT = 12.seconds
private val CACHE_TTL = 60.seconds

actual class LocationService : LocationServiceContract {

    private val locationManager = CLLocationManager()
    private val delegate = LocationFixDelegate()

    // Short-lived cache so a coordinator cycle that reads the location twice
    // (distance calc + sendLocation) doesn't trigger two active CoreLocation bursts.
    private var cachedFix: Pair<Pair<Double, Double>, Instant>? = null

    init {
        locationManager.delegate = delegate
        // A short burst could otherwise be silently withheld/delayed if iOS judges the
        // device "stationary", causing the timeout to fire with no fix delivered.
        locationManager.pausesLocationUpdatesAutomatically = false
    }

    actual override fun getPermissionStatus(): LocationPermissionStatus {
        return when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedWhenInUse,
            kCLAuthorizationStatusAuthorizedAlways -> LocationPermissionStatus.GRANTED

            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> LocationPermissionStatus.DENIED

            kCLAuthorizationStatusNotDetermined -> LocationPermissionStatus.NOT_DETERMINED

            else -> LocationPermissionStatus.NOT_DETERMINED
        }
    }

    actual override fun getBackgroundPermissionStatus(): LocationPermissionStatus {
        return when (locationManager.authorizationStatus) {
            kCLAuthorizationStatusAuthorizedAlways -> LocationPermissionStatus.GRANTED
            kCLAuthorizationStatusDenied,
            kCLAuthorizationStatusRestricted -> LocationPermissionStatus.DENIED
            else -> LocationPermissionStatus.NOT_DETERMINED
        }
    }

    // iOS derives this from CLLocationManager.authorizationStatus; no extra tracking needed.
    actual override fun markPermissionRequested() = Unit

    // Starts a short CoreLocation burst and suspends until the delegate delivers a fix
    // (or the timeout/failure path resolves with null), then stops updating immediately —
    // this is the CoreLocation equivalent of a one-shot request, since continuous
    // startUpdatingLocation() with no stop is what previously kept GPS on at all times.
    @OptIn(ExperimentalForeignApi::class)
    actual override suspend fun getCurrentLocation(highAccuracy: Boolean): Pair<Double, Double>? {
        cachedFix?.let { (coords, fetchedAt) ->
            if (Clock.System.now() - fetchedAt < CACHE_TTL) return coords
        }

        if (getPermissionStatus() != LocationPermissionStatus.GRANTED) return null

        // Setting this without "Always" authorization (and without the UIBackgroundModes
        // "location" entry in Info.plist) is a programmer error per Apple's docs — only
        // set it once we know background access is actually granted.
        if (getBackgroundPermissionStatus() == LocationPermissionStatus.GRANTED) {
            locationManager.allowsBackgroundLocationUpdates = true
        }

        if (delegate.continuation != null) {
            // Coordinator calls this strictly sequentially per cycle; an overlap here
            // would mean a caller didn't wait for the previous fix — fail fast rather
            // than silently dropping the earlier caller's continuation.
            return null
        }

        // The court is at the beach — open sky, GPS tends to lock fast and precisely there,
        // so requesting Best near the court isn't much more expensive than the coarser tier.
        locationManager.desiredAccuracy =
            if (highAccuracy) kCLLocationAccuracyBest else kCLLocationAccuracyHundredMeters
        val timeout = if (highAccuracy) HIGH_ACCURACY_TIMEOUT else LOW_POWER_TIMEOUT

        val coords = withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine { continuation ->
                delegate.continuation = continuation
                continuation.invokeOnCancellation {
                    locationManager.stopUpdatingLocation()
                    delegate.continuation = null
                }
                locationManager.startUpdatingLocation()
            }
        }

        if (coords != null) {
            cachedFix = coords to Clock.System.now()
        }
        return coords
    }
}

// Delivers CoreLocation callbacks into the suspend function above and stops updating as
// soon as a fix (or failure) arrives, so a burst never keeps transmitting past its use.
private class LocationFixDelegate : NSObject(), CLLocationManagerDelegateProtocol {

    var continuation: CancellableContinuation<Pair<Double, Double>?>? = null

    @OptIn(ExperimentalForeignApi::class)
    override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        manager.stopUpdatingLocation()
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        val coords = location?.coordinate?.useContents { Pair(latitude, longitude) }
        val pending = continuation
        continuation = null
        if (pending?.isActive == true) pending.resume(coords)
    }

    override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        manager.stopUpdatingLocation()
        val pending = continuation
        continuation = null
        if (pending?.isActive == true) pending.resume(null)
    }
}
