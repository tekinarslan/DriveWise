package com.drivewise.session

import com.drivewise.background.BackgroundSessionRunner
import com.drivewise.data.TrackPointRepository
import com.drivewise.tracking.AdaptiveSampler
import com.drivewise.tracking.RawGpsSample
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

sealed class DriveSessionEvent {
    data class Finished(val lessonId: String) : DriveSessionEvent()
}

@OptIn(ExperimentalTime::class)
class DriveSessionController(
    private val repo: TrackPointRepository,
    private val sampler: AdaptiveSampler,
    private var bgRunner: BackgroundSessionRunner
) {
    private val _state = MutableStateFlow(DriveSessionState())
    val state: StateFlow<DriveSessionState> = _state

    private val _events = MutableSharedFlow<DriveSessionEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<DriveSessionEvent> = _events

    // running aggregates
    private var sumSpeed = 0.0
    private var speedCount = 0
    private var maxSpeed = 0.0

    fun setRunner(runner: BackgroundSessionRunner) {
        val wasRunning = state.value.isRunning
        if (wasRunning) stop()
        this.bgRunner = runner
    }

    fun start() {
        if (_state.value.isRunning) return

        val lessonId = "lesson_${nowMs()}"
        resetAggregates()

        _state.value = DriveSessionState(
            isRunning = true,
            lessonId = lessonId,
            startedAtMs = nowMs(),
            lastTimestampMs = null
        )

        bgRunner.start(lessonId) { sample ->
            onSample(lessonId, sample)
        }
    }

    fun stop(navigateToReport: Boolean = true) {
        val lessonId = _state.value.lessonId
        if (!_state.value.isRunning || lessonId.isNullOrBlank()) return

        bgRunner.stop()
        _state.update { it.copy(isRunning = false) }

        if (navigateToReport) {
            _events.tryEmit(DriveSessionEvent.Finished(lessonId))
        }
    }

    fun isRunning(): Boolean = _state.value.isRunning

    private fun onSample(lessonId: String, s: RawGpsSample) {
        // elapsed
        val startedAt = _state.value.startedAtMs ?: s.timestampMs
        val elapsedSec = ((s.timestampMs - startedAt) / 1000L).toInt().coerceAtLeast(0)

        // km accumulation (between last point and current point)
        val lastLat = _state.value.lastLat
        val lastLon = _state.value.lastLon
        val deltaKm = if (lastLat != null && lastLon != null) {
            haversineKm(lastLat, lastLon, s.lat, s.lon)
        } else 0.0

        // speed aggregates (km/h in domain)
        val speed = s.speedKmh.coerceAtLeast(0.0)
        sumSpeed += speed
        speedCount += 1
        if (speed > maxSpeed) maxSpeed = speed

        // DB insert (sampler decides)
        var savedInc = 0
        if (sampler.shouldRecord(s)) {
            repo.insert(
                lessonId = lessonId,
                sample = RawGpsSample(
                    lat = s.lat,
                    lon = s.lon,
                    speedKmh = speed,
                    bearingDeg = s.bearingDeg,
                    timestampMs = s.timestampMs
                ),
            )
            savedInc = 1
        }

        val avg = if (speedCount > 0) sumSpeed / speedCount else 0.0

        _state.update {
            it.copy(
                lastTimestampMs = s.timestampMs,
                elapsedSeconds = elapsedSec,
                km = (it.km + deltaKm),

                currentSpeedKmh = speed,
                avgSpeedKmh = avg,
                maxSpeedKmh = maxSpeed,

                pointsSaved = it.pointsSaved + savedInc,
                lastLat = s.lat,
                lastLon = s.lon
            )
        }
    }

    private fun resetAggregates() {
        sumSpeed = 0.0
        speedCount = 0
        maxSpeed = 0.0
    }

    private fun nowMs(): Long = Clock.System.now().toEpochMilliseconds()

    // CMP-safe haversine
    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()
        val a = sin(dLat / 2).pow(2) +
                cos(lat1.toRadians()) * cos(lat2.toRadians()) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun Double.toRadians(): Double = this * PI / 180.0
}
