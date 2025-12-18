package com.drivewise.data

import com.drivewise.app.db.AppDatabase
import com.drivewise.tracking.RawGpsSample

class TrackPointRepository(
    private val db: AppDatabase
) {
    private val q = db.trackpointQueries

    fun insert(lessonId: String, sample: RawGpsSample) {
        println("DB insert: lesson=$lessonId lat=${sample.lat} lon=${sample.lon} speed=${sample.speedKmh} ts=${sample.timestampMs}")

        q.createPoint(
            lesson_id = lessonId,
            lat = sample.lat,
            lon = sample.lon,
            speed_kmh = sample.speedKmh,
            bearing_deg = sample.bearingDeg,
            timestamp_ms = sample.timestampMs
        )
    }

    fun count(lessonId: String): Long = q.countByLesson(lessonId).executeAsOne()
}
