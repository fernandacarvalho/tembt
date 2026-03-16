package com.tembt.platform

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.tembt.domain.model.LocationPermissionStatus

private const val PREF_NAME = "tembt_prefs"
private const val KEY_LOCATION_ASKED = "location_permission_asked"

actual class LocationService(private val context: Context) : LocationServiceContract {

    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

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

    actual override fun markPermissionRequested() {
        prefs.edit().putBoolean(KEY_LOCATION_ASKED, true).apply()
    }

    @SuppressLint("MissingPermission")
    actual override fun getCurrentLocation(): Pair<Double, Double>? {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        return location?.let { Pair(it.latitude, it.longitude) }
    }
}
