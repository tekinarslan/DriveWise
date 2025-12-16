package com.drivewise.permission

expect class LocationPermissionChecker {
    fun isGranted(): Boolean
}

expect fun provideLocationPermissionChecker(): LocationPermissionChecker

