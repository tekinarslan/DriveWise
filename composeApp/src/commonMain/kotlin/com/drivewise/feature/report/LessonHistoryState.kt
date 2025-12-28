package com.drivewise.feature.report

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.drivewise.app.db.TrackPoint
import com.drivewise.core.OnboardingStore
import com.drivewise.data.LessonHistoryMapper
import com.drivewise.data.TrackPointRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LessonHistoryState(
    val loading: Boolean = false,
    val error: String? = null,
    val lessons: List<LessonHistoryUiItem> = emptyList()
)

class LessonHistoryModel(
    private val repo: TrackPointRepository,
    private val onboardingStore: OnboardingStore
) : ScreenModel {

    private val _state = MutableStateFlow(LessonHistoryState())
    val state: StateFlow<LessonHistoryState> = _state

    init {
        refresh()
    }

    fun refresh() {
        screenModelScope.launch {
            runCatching {
                _state.value = _state.value.copy(loading = true, error = null)
                repo.lessonSummaries()
            }.onSuccess { summaries ->

                val lang = onboardingStore.getLanguage().code

                val uiItems = summaries
                    .filter { it.startedAtMs > 0L && it.endedAtMs > 0L }
                    .map { summary ->

                        // 1️⃣ lesson’a ait tüm noktaları al
                        val points = repo.pointsByLesson(summary.lessonId)

                        // 2️⃣ total km hesapla
                        val totalKm = computeTotalKm(points)

                        // 3️⃣ mapper’a parametre olarak ver
                        LessonHistoryMapper.map(
                            lessonSummary = summary,
                            totalKm = totalKm,
                            languageCode = lang
                        )
                    }

                _state.value = LessonHistoryState(
                    loading = false,
                    lessons = uiItems,
                    error = null
                )
            }.onFailure { e ->
                _state.value = LessonHistoryState(
                    loading = false,
                    lessons = emptyList(),
                    error = (e.message ?: "Unknown error")
                )
            }
        }
    }

    // ---- KM HESABI (commonMain uyumlu) ----
    private fun computeTotalKm(points: List<TrackPoint>): Double {
        if (points.size < 2) return 0.0

        var km = 0.0
        var prev = points.first()

        for (i in 1 until points.size) {
            val cur = points[i]
            km += haversineKm(
                prev.lat, prev.lon,
                cur.lat, cur.lon
            )
            prev = cur
        }
        return km
    }

    private fun haversineKm(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val r = 6371.0
        val dLat = (lat2 - lat1) * kotlin.math.PI / 180.0
        val dLon = (lon2 - lon1) * kotlin.math.PI / 180.0

        val a =
            kotlin.math.sin(dLat / 2).let { it * it } +
                    kotlin.math.cos(lat1 * kotlin.math.PI / 180.0) *
                    kotlin.math.cos(lat2 * kotlin.math.PI / 180.0) *
                    kotlin.math.sin(dLon / 2).let { it * it }

        val c = 2 * kotlin.math.atan2(
            kotlin.math.sqrt(a),
            kotlin.math.sqrt(1 - a)
        )

        return r * c
    }
}
