package com.drivewise.tracking

import kotlin.math.*

data class RawGpsSample(
    val lat: Double,
    val lon: Double,
    val speedKmh: Double,
    val bearingDeg: Double?,   // platform verirse; yoksa null
    val timestampMs: Long
)

class AdaptiveSampler(
    private val baseIntervalMs: Long = 5_000L,
    private val slowIntervalMs: Long = 3_000L,
    private val minMovingSpeedKmh: Double = 1.8,          // ~1.8 km/h
    private val speedDeltaTriggerKmh: Double = 8.0,       // ani hız değişimi
    private val bearingDeltaTriggerDeg: Double = 28.0     // ani yön değişimi
) {
    private var lastSaved: RawGpsSample? = null
    private var lastSavedAtMs: Long = 0L

    /**
     * True dönerse bu sample'ı DB'ye yaz.
     */
    fun shouldRecord(sample: RawGpsSample): Boolean {
        // 1) hiç hareket yoksa kaydetme
        if (sample.speedKmh < minMovingSpeedKmh) return false

        val prev = lastSaved
        if (prev == null) {
            accept(sample)
            return true
        }

        val dt = sample.timestampMs - lastSavedAtMs

        // 2) yavaş gidiyorsa (kavşak/park/man. hareket) daha sık kaydet
        val speedKmh = sample.speedKmh
        val interval = if (speedKmh < 15.0) slowIntervalMs else baseIntervalMs

        // 3) Ani hız değişimi → hemen kaydet
        val prevKmh = prev.speedKmh
        val speedDelta = abs(speedKmh - prevKmh)
        if (speedDelta >= speedDeltaTriggerKmh) {
            accept(sample)
            return true
        }

        // 4) Ani yön değişimi → hemen kaydet (bearing varsa)
        val b1 = prev.bearingDeg
        val b2 = sample.bearingDeg
        if (b1 != null && b2 != null) {
            val delta = smallestAngleDiffDeg(b1, b2)
            if (delta >= bearingDeltaTriggerDeg) {
                accept(sample)
                return true
            }
        }

        // 5) Normal interval dolduysa kaydet
        if (dt >= interval) {
            accept(sample)
            return true
        }

        return false
    }

    private fun accept(sample: RawGpsSample) {
        lastSaved = sample
        lastSavedAtMs = sample.timestampMs
    }

    private fun smallestAngleDiffDeg(a: Double, b: Double): Double {
        var diff = (a - b) % 360.0
        if (diff < -180) diff += 360.0
        if (diff > 180) diff -= 360.0
        return abs(diff)
    }
}
