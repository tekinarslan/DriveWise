package com.drivewise.permission

import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse

actual class LocationPermissionChecker {
    actual fun isGranted(): Boolean {
        val status = CLLocationManager.authorizationStatus()
        return status == kCLAuthorizationStatusAuthorizedWhenInUse ||
            status == kCLAuthorizationStatusAuthorizedAlways
    }
}

actual fun provideLocationPermissionChecker(): LocationPermissionChecker =
    LocationPermissionChecker()