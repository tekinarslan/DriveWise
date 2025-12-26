package com.drivewise.data

import com.drivewise.feature.report.LessonHistoryUiItem
import com.drivewise.feature.report.LessonSummary
import com.drivewise.util.DateTimeFormatter
import kotlin.math.roundToInt

object LessonHistoryMapper {
    fun map(
        lessonSummary: LessonSummary,
        languageCode: String
    ): LessonHistoryUiItem {
        val durationSec =
            ((lessonSummary.endedAtMs - lessonSummary.startedAtMs) / 1000L).toInt().coerceAtLeast(0)

        return LessonHistoryUiItem(
            lessonId = lessonSummary.lessonId,
            title = DateTimeFormatter.formatLessonTitle(
                startedAtMs = lessonSummary.startedAtMs,
                languageCode = languageCode
            ),
            durationSec = durationSec,
            pointsSaved = lessonSummary.pointsSaved.toInt(),
            avgSpeedKmh = lessonSummary.avgSpeedKmh.roundToInt(),
            maxSpeedKmh = lessonSummary.maxSpeedKmh.roundToInt()
        )
    }
}
