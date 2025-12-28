package com.drivewise.data

import com.drivewise.tracking.RawGpsSample
import com.drivewise.app.db.AppDatabase
import com.drivewise.app.db.TrackPoint
import com.drivewise.feature.report.LessonSummary

class TrackPointRepository(
    private val db: AppDatabase
) {

    fun insert(lessonId: String, sample: RawGpsSample) {
        db.trackpointQueries.createPoint(
            lesson_id = lessonId,
            lat = sample.lat,
            lon = sample.lon,
            speed_kmh = sample.speedKmh,
            bearing_deg = sample.bearingDeg,
            timestamp_ms = sample.timestampMs
        )
    }

    fun count(lessonId: String): Long =
        db.trackpointQueries.countByLesson(lessonId).executeAsOne()

    fun lessonSummaries(): List<LessonSummary> {
        return db.trackpointQueries.lessonSummaries().executeAsList().map {
            LessonSummary(
                lessonId = it.lessonId,
                pointsSaved = it.pointsSaved,
                startedAtMs = it.startedAtMs ?: 0L,
                endedAtMs = it.endedAtMs ?: 0L,
                avgSpeedKmh = it.avgSpeedKmh ?: 0.0,
                maxSpeedKmh = it.maxSpeedKmh ?: 0.0
            )
        }
    }

    fun lessonSummaryById(lessonId: String): LessonSummary? {
        return db.trackpointQueries.lessonSummaryById(lessonId).executeAsOneOrNull()?.let {
            LessonSummary(
                lessonId = it.lessonId,
                pointsSaved = it.pointsSaved,
                startedAtMs = it.startedAtMs?: 0L,
                endedAtMs = it.endedAtMs?: 0L,
                avgSpeedKmh = it.avgSpeedKmh ?: 0.0,
                maxSpeedKmh = it.maxSpeedKmh ?: 0.0
            )
        }
    }

    fun deleteLesson(lessonId: String) {
        db.trackpointQueries.deleteLessonPoints(lessonId)
    }

    fun pointsByLesson(lessonId: String): List<TrackPoint> {
        return db.trackpointQueries
            .pointsByLesson(lesson_id = lessonId)
            .executeAsList()
    }
}
