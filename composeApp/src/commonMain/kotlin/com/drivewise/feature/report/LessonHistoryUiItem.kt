package com.drivewise.feature.report

data class LessonHistoryUiItem(
    val lessonId: String,
    val title: String,
    val durationSec: Int,
    val pointsSaved: Int,
    val totalKm: Double = 0.0,
    val avgSpeedKmh: Int,
    val maxSpeedKmh: Int
)

