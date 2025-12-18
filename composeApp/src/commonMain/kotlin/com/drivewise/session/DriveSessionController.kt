package com.drivewise.session

import com.drivewise.data.TrackPointRepository
import com.drivewise.tracking.AdaptiveSampler
import com.drivewise.tracking.LocationTracker
import com.drivewise.tracking.RawGpsSample
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DriveSessionController(
    tracker: LocationTracker,
    private val repo: TrackPointRepository,
    private val sampler: AdaptiveSampler = AdaptiveSampler(
        baseIntervalMs = 5_000L,
        slowIntervalMs = 3_000L
    )
) {
    // Tracker runtime’da değişebilsin (Fake vs Real)
    private var tracker: LocationTracker = tracker

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(DriveSessionState())
    val state: StateFlow<DriveSessionState> = _state

    private var tickJob: Job? = null

    private var lastRaw: RawGpsSample? = null

    private var speedSumKmh = 0.0
    private var speedCount = 0

    fun setTracker(newTracker: LocationTracker) {
        val wasRunning = _state.value.isRunning
        if (wasRunning) stop()
        tracker = newTracker
    }

    fun start() {
        if (_state.value.isRunning) return

        val lessonId = "lesson_${Clock.System.now().toEpochMilliseconds()}"

        // reset runtime vars
        lastRaw = null
        speedSumKmh = 0.0
        speedCount = 0

        _state.value = DriveSessionState(
            isRunning = true,
            lessonId = lessonId,
            gpsReady = false
        )

        startTicker()
        startTracker(lessonId)
    }

    fun stop() {
        if (!_state.value.isRunning) return

        tracker.stop()
        stopTicker()

        _state.value = _state.value.copy(isRunning = false)
    }

    fun dispose() {
        stop()
        scope.cancel()
    }

    private fun startTicker() {
        tickJob?.cancel()
        tickJob = scope.launch {
            while (isActive) {
                delay(1000)

                val s = _state.value
                if (!s.isRunning) continue

                val now = Clock.System.now().toEpochMilliseconds()
                val gpsReadyNow = s.lastSampleAtMs?.let { now - it <= 5_000L } ?: false

                _state.value = s.copy(
                    elapsedSeconds = s.elapsedSeconds + 1,
                    gpsReady = gpsReadyNow
                )
            }
        }
    }

    private fun stopTicker() {
        tickJob?.cancel()
        tickJob = null
    }

    private fun startTracker(lessonId: String) {
        tracker.start { sample ->
            onSample(lessonId, sample)
        }
    }

    private fun onSample(lessonId: String, sample: RawGpsSample) {
        val currentSpeedKmh = (sample.speedKmh).coerceAtLeast(0.0)

        // KM: haversine delta
        val prev = lastRaw
        val deltaKm = if (prev != null) haversineKm(prev.lat, prev.lon, sample.lat, sample.lon) else 0.0
        lastRaw = sample

        // avg/max speed
        speedSumKmh += currentSpeedKmh
        speedCount += 1
        val avgSpeed = speedSumKmh / speedCount

        // DB insert only when sampler says so
        var pointsSaved = _state.value.pointsSaved
        if (sampler.shouldRecord(sample)) {
            repo.insert(lessonId, sample)
            pointsSaved += 1
        }

        val s = _state.value
        _state.value = s.copy(
            lastSampleAtMs = sample.timestampMs,
            gpsReady = true,
            km = s.km + deltaKm,
            currentSpeedKmh = currentSpeedKmh,
            avgSpeedKmh = avgSpeed,
            maxSpeedKmh = max(s.maxSpeedKmh, currentSpeedKmh),
            pointsSaved = pointsSaved
        )
    }
}

// ---------- CMP-safe helpers ----------
private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadiusKm = 6371.0

    val dLat = (lat2 - lat1).toRadians()
    val dLon = (lon2 - lon1).toRadians()

    val rLat1 = lat1.toRadians()
    val rLat2 = lat2.toRadians()

    val a =
        sin(dLat / 2).pow(2) +
                cos(rLat1) * cos(rLat2) * sin(dLon / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
}

private fun Double.toRadians(): Double = this * PI / 180.0
