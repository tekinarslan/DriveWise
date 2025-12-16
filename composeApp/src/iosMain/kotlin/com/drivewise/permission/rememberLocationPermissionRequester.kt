package com.drivewise.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import platform.CoreLocation.*
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.Foundation.NSURL
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

@Composable
actual fun rememberLocationPermissionRequester(
    onResult: (LocationPermissionStatus) -> Unit
): LocationPermissionRequester {

    val manager = remember { CLLocationManager() }

    // Keep delegate alive
    val delegate = remember {
        object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
                println("iOS auth changed: ${CLLocationManager.authorizationStatus()}")
                onResult(mapStatus(CLLocationManager.authorizationStatus()))
            }

            // iOS <14 fallback (still useful)
            override fun locationManager(
                manager: CLLocationManager,
                didChangeAuthorizationStatus: CLAuthorizationStatus
            ) {
                onResult(mapStatus(didChangeAuthorizationStatus))
            }
        }
    }

    DisposableEffect(Unit) {
        manager.delegate = delegate
        onDispose { manager.delegate = null }
    }

    return remember {
        object : LocationPermissionRequester {
            override fun request() {
                val status = CLLocationManager.authorizationStatus()
                when (status) {
                    kCLAuthorizationStatusNotDetermined -> {
                        dispatch_async(dispatch_get_main_queue()) {
                            manager.requestWhenInUseAuthorization()
                        }
                    }
                    kCLAuthorizationStatusAuthorizedWhenInUse,
                    kCLAuthorizationStatusAuthorizedAlways -> {
                        onResult(LocationPermissionStatus.GRANTED)
                    }
                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted -> {
                        onResult(LocationPermissionStatus.DENIED_PERMANENTLY)
                    }
                    else -> onResult(LocationPermissionStatus.DENIED)
                }
            }


            override fun openSettings() {
                val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
                UIApplication.sharedApplication.openURL(url)
            }
        }
    }
}

private fun mapStatus(status: CLAuthorizationStatus): LocationPermissionStatus =
    when (status) {
        kCLAuthorizationStatusAuthorizedWhenInUse,
        kCLAuthorizationStatusAuthorizedAlways -> LocationPermissionStatus.GRANTED
        kCLAuthorizationStatusDenied,
        kCLAuthorizationStatusRestricted -> LocationPermissionStatus.DENIED_PERMANENTLY
        else -> LocationPermissionStatus.DENIED
    }
