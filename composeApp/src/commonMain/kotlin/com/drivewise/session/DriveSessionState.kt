package com.drivewise.session

data class DriveSessionState(
    val isRunning: Boolean = false,
    val lessonId: String? = null,

    val elapsedSeconds: Int = 0,

    val km: Double = 0.0,

    val currentSpeedKmh: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,

    val gpsReady: Boolean = false,
    val lastSampleAtMs: Long? = null,

    val pointsSaved: Long = 0
)
