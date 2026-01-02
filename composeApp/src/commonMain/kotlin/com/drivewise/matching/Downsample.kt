package com.drivewise.matching

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

data class LatLon(val lat: Double, val lon: Double)

object Downsample {

    /**
     * 1) Her N. noktayı al (ucuz)
     */
    fun everyNth(points: List<LatLon>, n: Int): List<LatLon> {
        if (n <= 1) return points
        if (points.isEmpty()) return emptyList()
        val out = ArrayList<LatLon>(points.size / n + 2)
        for (i in points.indices) {
            if (i % n == 0) out += points[i]
        }
        if (out.lastOrNull() != points.last()) out += points.last()
        return out
    }

    /**
     * 2) Min metre hareket yoksa atla (daha iyi MVP)
     * minMeters = 8..15 iyi başlangıç
     */
    fun minDistance(points: List<LatLon>, minMeters: Double): List<LatLon> {
        if (points.isEmpty()) return emptyList()
        val out = ArrayList<LatLon>(points.size)
        var last = points.first()
        out += last
        for (i in 1 until points.size) {
            val p = points[i]
            if (approxMeters(last, p) >= minMeters) {
                out += p
                last = p
            }
        }
        if (out.lastOrNull() != points.last()) out += points.last()
        return out
    }

    private fun approxMeters(a: LatLon, b: LatLon): Double {
        val dLat = (b.lat - a.lat) * 111_111.0
        val dLon = (b.lon - a.lon) * 111_111.0 * cos(a.lat * PI / 180.0).coerceAtLeast(0.1)
        return sqrt(dLat * dLat + dLon * dLon)
    }
}
