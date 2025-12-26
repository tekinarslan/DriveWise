package com.drivewise.feature.report

import cafe.adriel.voyager.core.model.ScreenModel
import com.drivewise.repository.LessonReportRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class LessonReportScreenModel(
    private val lessonId: String,
    private val repo: LessonReportRepository
) : ScreenModel {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow(LessonReportState(loading = true, lessonId = lessonId))
    val state: StateFlow<LessonReportState> = _state

    init {
        load()
    }

    private fun load() {
        scope.launch {
            try {
                val agg = repo.loadAgg(lessonId)
                if (agg == null) {
                    _state.value = LessonReportState(
                        loading = false,
                        lessonId = lessonId,
                        error = "Bu ders için veri bulunamadı."
                    )
                    return@launch
                }

                val totalKm = repo.computeTotalKm(lessonId)
                val durationSec = if (agg.startMs != null && agg.endMs != null) {
                    ((agg.endMs - agg.startMs) / 1000L).toInt().coerceAtLeast(0)
                } else 0

                _state.value = LessonReportState(
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
            } catch (t: Throwable) {
                _state.value = LessonReportState(
                    loading = false,
                    lessonId = lessonId,
                    error = t.message ?: "Unknown error"
                )
            }
        }
    }
}
