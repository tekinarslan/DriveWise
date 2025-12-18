package com.drivewise.tracking

import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FakeLocationTracker(
    private val route: List<Pair<Double, Double>>,
    private val intervalMs: Long = 1000L
) : LocationTracker {

    private var job: Job? = null

    // runtime speed state (km/h)
    private var currentSpeedKmh = 28.0 + Random.nextDouble(4.0)

    override fun start(onSample: (RawGpsSample) -> Unit) {
        stop()

        job = CoroutineScope(Dispatchers.Default).launch {
            if (route.size < 2) return@launch

            var i = 0

            while (isActive) {
                val (lat, lon) = route[i]
                val (nextLat, nextLon) =
                    route[(i + 1).coerceAtMost(route.lastIndex)]

                // Bearing hesapla
                val bearing = bearingDeg(lat, lon, nextLat, nextLon)

                // 🟡 Gerçekçi hız simülasyonu
                currentSpeedKmh = nextSpeed(
                    current = currentSpeedKmh,
                    turning = isTurning(lat, lon, nextLat, nextLon),
                    atIntersection = isIntersection(i)
                )

                onSample(
                    RawGpsSample(
                        lat = lat,
                        lon = lon,
                        speedKmh = currentSpeedKmh,
                        bearingDeg = bearing,
                        timestampMs = Clock.System.now().toEpochMilliseconds()
                    )
                )

                i = (i + 1) % route.size
                delay(intervalMs)
            }
        }
    }

    override fun stop() {
        job?.cancel()
        job = null
    }

    // ---------------------------------------------------
    // 🧠 Simülasyon yardımcıları
    // ---------------------------------------------------

    /** Dönüş var mı? (bearing farkı büyükse) */
    private fun isTurning(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Boolean {
        val bearing = bearingDeg(lat1, lon1, lat2, lon2)
        return bearing < 40 || bearing > 320 // keskin dönüş hissi
    }

    /** Route index’ine göre “kavşak” simülasyonu */
    private fun isIntersection(index: Int): Boolean =
        index % 6 == 0 // her 6 noktada bir kavşak gibi düşün

    /** Hız evrimi */
    private fun nextSpeed(
        current: Double,
        turning: Boolean,
        atIntersection: Boolean
    ): Double {
        var target = current

        // 🔴 Kavşakta yavaşla
        if (atIntersection) {
            target = Random.nextDouble(10.0, 18.0)
        }
        // 🟠 Dönüşte yavaşla
        else if (turning) {
            target = Random.nextDouble(18.0, 25.0)
        }
        // 🟢 Normal sürüş
        else {
            target = Random.nextDouble(30.0, 50.0)
        }

        // hız değişimini yumuşat (ani zıplamasın)
        val delta = (target - current) * 0.3
        val next = current + delta

        // clamp
        return next.coerceIn(0.0, 55.0)
    }

    // ---------------------------------------------------
    // Matematik
    // ---------------------------------------------------

    private fun bearingDeg(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val phi1 = lat1.toRadians()
        val phi2 = lat2.toRadians()
        val dLon = (lon2 - lon1).toRadians()

        val y = sin(dLon) * cos(phi2)
        val x = cos(phi1) * sin(phi2) -
                sin(phi1) * cos(phi2) * cos(dLon)

        val brng = atan2(y, x).toDegrees()
        return (brng + 360.0) % 360.0
    }
}

// --- helpers ---
private fun Double.toRadians(): Double = this * PI / 180.0
private fun Double.toDegrees(): Double = this * 180.0 / PI
