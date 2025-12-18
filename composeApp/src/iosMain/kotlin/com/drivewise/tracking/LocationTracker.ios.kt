package com.drivewise.tracking

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.*
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
private class IosLocationTracker : LocationTracker {

    private val manager = CLLocationManager()
    private var onSample: ((RawGpsSample) -> Unit)? = null

    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {

        override fun locationManager(
            manager: CLLocationManager,
            didUpdateLocations: List<*>
        ) {
            val last = didUpdateLocations.lastOrNull() as? CLLocation ?: return

            val coord = last.coordinate
            val lat = coord.useContents { latitude }
            val lon = coord.useContents { longitude }

            val speed = if (last.speed >= 0.0) last.speed else 0.0
            val bearing = if (last.course >= 0.0) last.course else null

            val tsMs = (last.timestamp.timeIntervalSince1970 * 1000.0).toLong()

            onSample?.invoke(
                RawGpsSample(
                    lat = lat,
                    lon = lon,
                    speedKmh = speed,
                    bearingDeg = bearing,
                    timestampMs = tsMs
                )
            )
        }
    }

    override fun start(onSample: (RawGpsSample) -> Unit) {
        this.onSample = onSample

        manager.delegate = delegate
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.distanceFilter = 0.0 // daha sık update
        manager.pausesLocationUpdatesAutomatically = false

        // permission UI’da verildiği için direkt başlatıyoruz
        manager.startUpdatingLocation()
    }

    override fun stop() {
        manager.stopUpdatingLocation()
        this.onSample = null
    }
}

actual fun provideLocationTracker(): LocationTracker = IosLocationTracker()
