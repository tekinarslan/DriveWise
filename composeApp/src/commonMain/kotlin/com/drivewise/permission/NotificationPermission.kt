package com.drivewise.permission

import androidx.compose.runtime.Composable

enum class NotificationPermissionStatus {
    GRANTED,
    DENIED,
    NOT_SUPPORTED
}

interface NotificationPermissionRequester {
    fun request()
    fun openSettings()
}

expect fun isNotificationPermissionRequired(): Boolean
expect fun areNotificationsEnabled(): Boolean

@Composable
expect fun rememberNotificationPermissionRequester(
    onResult: (NotificationPermissionStatus) -> Unit
): NotificationPermissionRequester
