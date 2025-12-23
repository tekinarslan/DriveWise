package com.drivewise.session

data class DriveSessionState(
    val isRunning: Boolean = false,
    val lessonId: String? = null,

    val startedAtMs: Long? = null,
    val lastTimestampMs: Long? = null,
    val elapsedSeconds: Int = 0,

    val pointsSaved: Int = 0,
    val km: Double = 0.0,

    val currentSpeedKmh: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,

    val lastLat: Double? = null,
    val lastLon: Double? = null,
    val gpsReady: Boolean = true
)
