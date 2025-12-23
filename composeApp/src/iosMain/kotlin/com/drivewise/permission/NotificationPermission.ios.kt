package com.drivewise.permission

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.UserNotifications.*

actual fun isNotificationPermissionRequired(): Boolean = true

actual fun areNotificationsEnabled(): Boolean = true // async kontrol istersen ekleriz

@Composable
actual fun rememberNotificationPermissionRequester(
    onResult: (NotificationPermissionStatus) -> Unit
): NotificationPermissionRequester {

    val center = UNUserNotificationCenter.currentNotificationCenter()

    return remember {
        object : NotificationPermissionRequester {
            override fun request() {
                center.requestAuthorizationWithOptions(
                    options = (UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge)
                ) { granted, _ ->
                    onResult(if (granted) NotificationPermissionStatus.GRANTED else NotificationPermissionStatus.DENIED)
                }
            }

            override fun openSettings() {
                val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return
                UIApplication.sharedApplication.openURL(url)
            }
        }
    }
}
