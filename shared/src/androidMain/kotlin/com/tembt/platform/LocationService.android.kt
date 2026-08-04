package com.tembt.platform

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.tembt.domain.model.LocationPermissionStatus
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private const val PREF_NAME = "tembt_prefs"
private const val KEY_LOCATION_ASKED = "location_permission_asked"
private val HIGH_ACCURACY_TIMEOUT = 20.seconds
private val LOW_POWER_TIMEOUT = 12.seconds
private val CACHE_TTL = 60.seconds
private val MAX_CACHED_FIX_AGE = 20.minutes

actual class LocationService(private val context: Context) : LocationServiceContract {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // Short-lived cache so a coordinator cycle that reads the location twice
    // (distance calc + sendLocation) doesn't trigger two active GPS requests.
    private var cachedFix: Pair<Pair<Double, Double>, Instant>? = null

    actual override fun getPermissionStatus(): LocationPermissionStatus {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return when {
            fineGranted || coarseGranted -> LocationPermissionStatus.GRANTED
            !prefs.getBoolean(KEY_LOCATION_ASKED, false) -> LocationPermissionStatus.NOT_DETERMINED
            else -> LocationPermissionStatus.DENIED
        }
    }

    actual override fun getBackgroundPermissionStatus(): LocationPermissionStatus {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return getPermissionStatus()
        }
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return if (granted) LocationPermissionStatus.GRANTED else LocationPermissionStatus.DENIED
    }

    actual override fun markPermissionRequested() {
        prefs.edit().putBoolean(KEY_LOCATION_ASKED, true).apply()
    }

    actual override suspend fun getCurrentLocation(highAccuracy: Boolean): Pair<Double, Double>? {
        cachedFix?.let { (coords, fetchedAt) ->
            if (Clock.System.now() - fetchedAt < CACHE_TTL) return coords
        }

        if (!hasLocationPermission()) return null

        val coords = requestActiveFix(highAccuracy) ?: lastKnownFixIfFresh()
        if (coords != null) {
            cachedFix = coords to Clock.System.now()
        }
        return coords
    }

    private fun hasLocationPermission(): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineGranted || coarseGranted
    }

    // Requests a single fresh fix, tuned by highAccuracy — the court is at the beach (open sky),
    // where GPS tends to lock fast and precisely, so the FINE/HIGH profile isn't much more
    // expensive there than the coarse one. Always removes the listener (finally/cancellation)
    // so a timeout never leaves GPS running.
    @SuppressLint("MissingPermission")
    private suspend fun requestActiveFix(highAccuracy: Boolean): Pair<Double, Double>? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria().apply {
            if (highAccuracy) {
                accuracy = Criteria.ACCURACY_FINE
                powerRequirement = Criteria.POWER_HIGH
            } else {
                accuracy = Criteria.ACCURACY_COARSE
                powerRequirement = Criteria.POWER_LOW
            }
        }
        val provider = locationManager.getBestProvider(criteria, true) ?: return null
        val timeout = if (highAccuracy) HIGH_ACCURACY_TIMEOUT else LOW_POWER_TIMEOUT

        return withTimeoutOrNull(timeout) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) {
                            continuation.resume(Pair(location.latitude, location.longitude))
                        }
                    }

                    @Deprecated("Deprecated in Java, still required to implement on minSdk 26")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
                    override fun onProviderEnabled(provider: String) = Unit
                    override fun onProviderDisabled(provider: String) {
                        locationManager.removeUpdates(this)
                        if (continuation.isActive) continuation.resume(null)
                    }
                }

                continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }

                try {
                    locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                } catch (e: SecurityException) {
                    if (continuation.isActive) continuation.resume(null)
                }
            }
        }
    }

    // Last-resort fallback if the active request times out or no provider is enabled.
    // Rejects stale cached fixes rather than returning arbitrarily old coordinates —
    // the coordinator already falls back to the MEDIUM interval when this returns null.
    @SuppressLint("MissingPermission")
    private fun lastKnownFixIfFresh(): Pair<Double, Double>? {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).mapNotNull { provider ->
            try {
                locationManager.getLastKnownLocation(provider)
            } catch (e: SecurityException) {
                null
            }
        }.maxByOrNull { it.time } ?: return null

        val ageMillis = System.currentTimeMillis() - location.time
        if (ageMillis > MAX_CACHED_FIX_AGE.inWholeMilliseconds) return null

        return Pair(location.latitude, location.longitude)
    }
}
