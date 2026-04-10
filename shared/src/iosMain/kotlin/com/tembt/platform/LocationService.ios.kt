package com.tembt.platform

import com.tembt.domain.model.LocationPermissionStatus
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.darwin.NSObject

actual class LocationService : LocationServiceContract {

    private val locationManager = CLLocationManager()
    // CLLocationManager requires a delegate to deliver location updates.
    // MinimalLocationDelegate satisfies the protocol without needing callbacks here —
    // we read from locationManager.location directly in getCurrentLocation().
    private val delegate = MinimalLocationDelegate()

    init {
        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.startUpdatingLocation()
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

    @OptIn(ExperimentalForeignApi::class)
    actual override fun getCurrentLocation(): Pair<Double, Double>? {
        println("[TEMBT-DEBUG] LocationService.iOS.getCurrentLocation: chamado")
        val loc = locationManager.location
        if (loc == null) {
            println("[TEMBT-DEBUG] LocationService.iOS.getCurrentLocation: location == null — GPS não disponível")
            return null
        }
        val result = loc.coordinate.useContents { Pair(latitude, longitude) }
        if (result.first == 0.0) {
            println("[TEMBT-DEBUG] LocationService.iOS.getCurrentLocation: lat=0.0 inválido, GPS ainda não adquirido")
            return null
        }
        println("[TEMBT-DEBUG] LocationService.iOS.getCurrentLocation: retornando lat=${result.first} lng=${result.second}")
        return result
    }
}

// Satisfies the CLLocationManagerDelegate protocol so the manager delivers updates
// and populates locationManager.location. No callbacks are needed here because
// getCurrentLocation() reads the cached value directly from the manager.
private class MinimalLocationDelegate : NSObject(), CLLocationManagerDelegateProtocol
