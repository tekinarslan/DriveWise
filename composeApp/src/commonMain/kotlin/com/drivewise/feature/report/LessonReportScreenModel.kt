package com.drivewise.feature.report

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.drivewise.data.TrackPointRepository
import com.drivewise.matching.Downsample
import com.drivewise.matching.LatLon
import com.drivewise.matching.OrsSnapClient
import com.drivewise.matching.SnapPipeline
import com.drivewise.repository.LessonReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RouteMatchState(
    val loading: Boolean = false,
    val error: String? = null,
    val polyline: List<LatLon> = emptyList()
)

class LessonReportScreenModel(
    private val lessonId: String,
    private val repo: LessonReportRepository,
    private val trackRepo: TrackPointRepository,
    private val ors: OrsSnapClient
) : ScreenModel {

    private val _state = MutableStateFlow(LessonReportState(loading = true, lessonId = lessonId))
    val state: StateFlow<LessonReportState> = _state

    private val _route = MutableStateFlow(RouteMatchState())
    val route: StateFlow<RouteMatchState> = _route

    init {
        load()
        buildMatchedRoute()
    }

    fun refreshAll() {
        load()
        buildMatchedRoute(force = true)
    }

    private fun load() {
        screenModelScope.launch {
            runCatching {
                _state.value = LessonReportState(loading = true, lessonId = lessonId)

                val agg = repo.loadAgg(lessonId) ?: return@runCatching LessonReportState(
                    loading = false,
                    lessonId = lessonId,
                    error = "Bu ders için veri bulunamadı."
                )

                val totalKm = repo.computeTotalKm(lessonId)
                val durationSec = if (agg.startMs != null && agg.endMs != null) {
                    ((agg.endMs - agg.startMs) / 1000L).toInt().coerceAtLeast(0)
                } else 0

                LessonReportState(
                    loading = false,
                    lessonId = lessonId,
                    pointsCount = agg.pointsCount.toInt(),
                    durationSec = durationSec,
                    totalKm = totalKm,
                    avgSpeedKmh = agg.avgSpeedKmh ?: 0.0,
                    maxSpeedKmh = agg.maxSpeedKmh ?: 0.0,
                    startMs = agg.startMs,
                    endMs = agg.endMs
                )
            }.onSuccess { st ->
                _state.value = st
            }.onFailure { t ->
                _state.value = LessonReportState(
                    loading = false,
                    lessonId = lessonId,
                    error = t.message ?: "Unknown error"
                )
            }
        }
    }

    private var matchStarted = false

    fun buildMatchedRoute(force: Boolean = false) {
        if (matchStarted && !force) return
        matchStarted = true

        screenModelScope.launch {
            runCatching {
                _route.value = RouteMatchState(loading = true)

                val raw = trackRepo.pointsByLesson(lessonId)
                    .map { LatLon(lat = it.lat, lon = it.lon) }

                if (raw.size < 2) return@runCatching emptyList()

                val ds1 = Downsample.minDistance(raw, minMeters = 15.0)
                val ds2 = if (ds1.size > 900) Downsample.everyNth(ds1, n = 2) else ds1

                SnapPipeline.snapWithChunking(
                    client = ors,
                    profile = "driving-car",
                    points = ds2,
                    radiusMeters = 35,
                    chunkSize = 200
                )
            }.onSuccess { snapped ->
                _route.value = RouteMatchState(loading = false, polyline = snapped)
            }.onFailure { e ->
                _route.value = RouteMatchState(loading = false, error = e.message ?: "Matching failed")
            }
        }
    }
}
