package com.drivewise.background

import com.drivewise.tracking.RawGpsSample
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class FakeBackgroundSessionRunner(
    private val route: List<Pair<Double, Double>>,
    private val intervalMs: Long = 1000L,
    private val minCruiseKmh: Double = 30.0,
    private val maxCruiseKmh: Double = 50.0,
    private val jitterMeters: Double = 3.0
) : BackgroundSessionRunner {

    private var job: Job? = null
    private var running = false

    private var currentSpeedKmh = Random.nextDouble(minCruiseKmh, minCruiseKmh + 5.0)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun start(lessonId: String, onSample: (RawGpsSample) -> Unit) {
        if (running) return
        running = true

        job = scope.launch {
            if (route.size < 2) return@launch

            var seg = 0
            var progressMeters = 0.0

            while (isActive) {
                val (aLat, aLon) = route[seg]
                val (bLat, bLon) = route[(seg + 1) % route.size]

                val segLenM = distanceMeters(aLat, aLon, bLat, bLon).coerceAtLeast(1.0)

                val turning = isSharpTurn(route, seg)
                val atIntersection = (seg % 6 == 0)

                currentSpeedKmh = nextSpeed(currentSpeedKmh, turning, atIntersection)

                val stepMeters = (currentSpeedKmh / 3.6) * (intervalMs / 1000.0)
                progressMeters += stepMeters

                while (progressMeters >= segLenM) {
                    progressMeters -= segLenM
                    seg = (seg + 1) % route.size
                }

                val (cLat, cLon) = route[seg]
                val (dLat, dLon) = route[(seg + 1) % route.size]
                val newLenM = distanceMeters(cLat, cLon, dLat, dLon).coerceAtLeast(1.0)
                val t = (progressMeters / newLenM).coerceIn(0.0, 1.0)

                var lat = lerp(cLat, dLat, t)
                var lon = lerp(cLon, dLon, t)

                val (jl, jn) = addJitter(lat, lon, jitterMeters)
                lat = jl; lon = jn

                val bearing = bearingDeg(cLat, cLon, dLat, dLon)

                onSample(
                    RawGpsSample(
                        lat = lat,
                        lon = lon,
                        speedKmh = currentSpeedKmh,
                        bearingDeg = bearing,
                        timestampMs = Clock.System.now().toEpochMilliseconds()
                    )
                )

                delay(intervalMs)
            }
        }
    }

    override fun stop() {
        running = false
        job?.cancel()
        job = null
        scope.coroutineContext.cancelChildren()
    }

    override fun isRunning(): Boolean = running

    private fun nextSpeed(current: Double, turning: Boolean, atIntersection: Boolean): Double {
        val target = when {
            atIntersection -> Random.nextDouble(8.0, 18.0)
            turning -> Random.nextDouble(18.0, 28.0)
            else -> Random.nextDouble(minCruiseKmh, maxCruiseKmh)
        }
        return (current + (target - current) * 0.25).coerceIn(0.0, 55.0)
    }

    private fun isSharpTurn(route: List<Pair<Double, Double>>, seg: Int): Boolean {
        if (route.size < 3) return false
        val prev = route[(seg - 1 + route.size) % route.size]
        val a = route[seg]
        val b = route[(seg + 1) % route.size]

        val br1 = bearingDeg(prev.first, prev.second, a.first, a.second)
        val br2 = bearingDeg(a.first, a.second, b.first, b.second)

        var d = (br1 - br2).absoluteValue
        if (d > 180.0) d = 360.0 - d
        return d > 35.0
    }

    private fun lerp(a: Double, b: Double, t: Double) = a + (b - a) * t

    private fun addJitter(lat: Double, lon: Double, meters: Double): Pair<Double, Double> {
        if (meters <= 0) return lat to lon
        val dLat = Random.nextDouble(-meters, meters) / 111_111.0
        val dLon = Random.nextDouble(-meters, meters) /
            (111_111.0 * cos(lat.toRadians()).coerceAtLeast(0.1))
        return (lat + dLat) to (lon + dLon)
    }

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000.0
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()
        val a = sin(dLat / 2).pow(2) +
            cos(lat1.toRadians()) * cos(lat2.toRadians()) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun bearingDeg(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val phi1 = lat1.toRadians()
        val phi2 = lat2.toRadians()
        val dLon = (lon2 - lon1).toRadians()
        val y = sin(dLon) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(dLon)
        val brng = atan2(y, x).toDegrees()
        return (brng + 360.0) % 360.0
    }

    private fun Double.toRadians() = this * PI / 180.0
    private fun Double.toDegrees() = this * 180.0 / PI
}
