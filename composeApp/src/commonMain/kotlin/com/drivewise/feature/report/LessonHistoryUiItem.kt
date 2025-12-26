package com.drivewise.feature.report

data class LessonHistoryUiItem(
    val lessonId: String,
    val title: String,
    val durationSec: Int,
    val pointsSaved: Int,
    val totalKm: Double,
    val avgSpeedKmh: Int,
    val maxSpeedKmh: Int
)

