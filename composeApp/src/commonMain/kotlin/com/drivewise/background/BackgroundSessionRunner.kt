package com.drivewise.background

import com.drivewise.tracking.RawGpsSample

interface BackgroundSessionRunner {
    fun start(
        lessonId: String,
        onSample: (RawGpsSample) -> Unit
    )
    fun stop()
    fun isRunning(): Boolean
}

expect fun createBackgroundSessionRunner(): BackgroundSessionRunner
