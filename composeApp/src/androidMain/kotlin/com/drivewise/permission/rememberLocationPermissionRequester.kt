package com.drivewise.permission

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

@Composable
actual fun rememberLocationPermissionRequester(
    onResult: (LocationPermissionStatus) -> Unit
): LocationPermissionRequester {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val permission = Manifest.permission.ACCESS_FINE_LOCATION

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onResult(LocationPermissionStatus.GRANTED)
        } else {
            // If user checked "Don't ask again" -> shouldShowRationale = false
            val permanentlyDenied =
                activity != null && !ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)

            onResult(
                if (permanentlyDenied) LocationPermissionStatus.DENIED_PERMANENTLY
                else LocationPermissionStatus.DENIED
            )
        }
    }

    return remember {
        object : LocationPermissionRequester {
            override fun request() {
                val alreadyGranted =
                    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

                if (alreadyGranted) {
                    onResult(LocationPermissionStatus.GRANTED)
                } else {
                    launcher.launch(permission)
                }
            }

            override fun openSettings() {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }
}
