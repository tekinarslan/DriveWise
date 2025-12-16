package com.drivewise.permission

import androidx.compose.runtime.Composable

enum class LocationPermissionStatus {
    GRANTED,
    DENIED,
    DENIED_PERMANENTLY
}

interface LocationPermissionRequester {
    fun request()
    fun openSettings()
}

/**
 * Platform implementation returns a requester that can trigger the permission flow.
 */
@Composable
expect fun rememberLocationPermissionRequester(
    onResult: (LocationPermissionStatus) -> Unit
): LocationPermissionRequester
