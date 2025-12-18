package com.drivewise.tracking

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private class AndroidLocationTracker : LocationTracker, KoinComponent {

    private val context: Context by inject()
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(context) }

    private var callback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    override fun start(onSample: (RawGpsSample) -> Unit) {
        // Permission kontrolünü UI flow’da yaptığın için burada istemiyoruz.
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000L // 1sn callback -> sampler neyi kaydedeceğine karar verir
        ).setMinUpdateIntervalMillis(1000L)
            .build()

        callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc: Location = result.lastLocation ?: return

                val bearing = if (loc.hasBearing()) loc.bearing.toDouble() else null
                val speedKmh = (loc.speed * 3.6).coerceAtLeast(0.0)

                onSample(
                    RawGpsSample(
                        lat = loc.latitude,
                        lon = loc.longitude,
                        speedKmh = speedKmh,
                        bearingDeg = bearing,
                        timestampMs = loc.time
                    )
                )
            }
        }

        fused.requestLocationUpdates(request, callback!!, Looper.getMainLooper())
    }

    override fun stop() {
        callback?.let { fused.removeLocationUpdates(it) }
        callback = null
    }
}

actual fun provideLocationTracker(): LocationTracker = AndroidLocationTracker()
