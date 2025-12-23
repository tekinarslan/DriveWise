package com.drivewise.permission

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.app.NotificationManagerCompat
import org.koin.compose.koinInject

actual fun isNotificationPermissionRequired(): Boolean =
    Build.VERSION.SDK_INT >= 33

actual fun areNotificationsEnabled(): Boolean {
    // POST_NOTIFICATIONS olsa bile user bildirimleri kapatabilir
    // bunu da kontrol etmek için NotificationManagerCompat:
    return true // çağrıyı composable'da context ile yapacağız
}

@Composable
actual fun rememberNotificationPermissionRequester(
    onResult: (NotificationPermissionStatus) -> Unit
): NotificationPermissionRequester {
    val context: Context = koinInject()

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        onResult(if (granted) NotificationPermissionStatus.GRANTED else NotificationPermissionStatus.DENIED)
    }

    return remember {
        object : NotificationPermissionRequester {
            override fun request() {
                if (!isNotificationPermissionRequired()) {
                    onResult(NotificationPermissionStatus.NOT_SUPPORTED)
                    return
                }
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }

            override fun openSettings() {
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }
}

// Android'de gerçek kontrolü context ile yapalım (helper)
fun areNotificationsEnabled(context: Context): Boolean =
    NotificationManagerCompat.from(context).areNotificationsEnabled()
