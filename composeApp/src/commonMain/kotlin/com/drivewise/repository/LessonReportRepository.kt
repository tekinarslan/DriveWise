package com.drivewise.repository

import com.drivewise.app.db.AppDatabase
import kotlin.math.*

class LessonReportRepository(
    private val db: AppDatabase
) {
    private val q = db.trackpointQueries

    data class Agg(
        val lessonId: String,
        val pointsCount: Long,
        val startMs: Long?,
        val endMs: Long?,
        val avgSpeedKmh: Double?,
        val maxSpeedKmh: Double?
    )

    fun loadAgg(lessonId: String): Agg? {
        val row = q.lessonAgg(lesson_id = lessonId).executeAsOneOrNull() ?: return null

        return Agg(
            lessonId = row.lessonId,
            pointsCount = row.pointsSaved,
            startMs = row.startedAtMs,
            endMs = row.endedAtMs,
            avgSpeedKmh = row.avgSpeedKmh,
            maxSpeedKmh = row.maxSpeedKmh
        )
    }

    fun loadPoints(lessonId: String) =
        q.pointsByLesson(lesson_id = lessonId).executeAsList()

    fun computeTotalKm(lessonId: String): Double {
        val pts = loadPoints(lessonId)
        if (pts.size < 2) return 0.0

        var km = 0.0
        for (i in 1 until pts.size) {
            val a = pts[i - 1]
            val b = pts[i]
            km += haversineKm(a.lat, a.lon, b.lat, b.lon)
        }
        return km
    }

    private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val a = sin(dLat / 2).pow(2) +
            cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
