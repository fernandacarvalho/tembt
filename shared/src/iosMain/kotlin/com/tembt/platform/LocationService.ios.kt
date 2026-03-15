package com.tembt.platform

import com.tembt.domain.model.LocationPermissionStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted

actual class LocationService : LocationServiceContract {

    private val locationManager = CLLocationManager()

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

    // iOS derives this from CLLocationManager.authorizationStatus; no extra tracking needed.
    actual override fun markPermissionRequested() = Unit
}
