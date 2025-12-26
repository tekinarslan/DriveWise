package com.drivewise.feature.report

data class LessonReportState(
    val loading: Boolean = true,
    val lessonId: String = "",
    val pointsCount: Int = 0,
    val durationSec: Int = 0,
    val totalKm: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val startMs: Long? = null,
    val endMs: Long? = null,
    val error: String? = null,
)