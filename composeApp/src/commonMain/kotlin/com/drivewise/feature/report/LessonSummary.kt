package com.drivewise.feature.report

data class LessonSummary(
    val lessonId: String,
    val pointsSaved: Long,
    val startedAtMs: Long,
    val endedAtMs: Long,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double
) {
    val durationSec: Int
        get() = ((endedAtMs - startedAtMs) / 1000L).toInt().coerceAtLeast(0)
}
