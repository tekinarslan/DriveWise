package com.drivewise.background

import com.drivewise.tracking.RawGpsSample
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.*
import platform.Foundation.timeIntervalSince1970
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
actual fun createBackgroundSessionRunner(): BackgroundSessionRunner =
    IosBackgroundSessionRunner()

@OptIn(ExperimentalForeignApi::class)
private class IosBackgroundSessionRunner : BackgroundSessionRunner {

    private val manager = CLLocationManager()
    private var running = false
    private var onSample: ((RawGpsSample) -> Unit)? = null

    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {

        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val last = didUpdateLocations.lastOrNull() as? CLLocation ?: return

            val speedKmh = if (last.speed >= 0.0) last.speed * 3.6 else 0.0
            val bearing = if (last.course >= 0.0) last.course else null
            val tsMs = (last.timestamp.timeIntervalSince1970 * 1000.0).toLong()

            val coord = last.coordinate
            val lat = coord.useContents { latitude }
            val lon = coord.useContents { longitude }

            onSample?.invoke(
                RawGpsSample(
                    lat = lat,
                    lon = lon,
                    speedKmh = speedKmh,
                    bearingDeg = bearing,
                    timestampMs = tsMs
                )
            )
        }
    }

    override fun start(lessonId: String, onSample: (RawGpsSample) -> Unit) {
        if (running) return
        running = true
        this.onSample = onSample

        manager.delegate = delegate
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.distanceFilter = 0.0
        manager.pausesLocationUpdatesAutomatically = false
        manager.allowsBackgroundLocationUpdates = true
        // opsiyonel: manager.showsBackgroundLocationIndicator = true

        manager.startUpdatingLocation()
    }

    override fun stop() {
        if (!running) return
        running = false

        manager.stopUpdatingLocation()
        onSample = null
    }

    override fun isRunning(): Boolean = running
}
