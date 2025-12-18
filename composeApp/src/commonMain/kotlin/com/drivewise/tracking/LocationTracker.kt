package com.drivewise.tracking

interface LocationTracker {
    fun start(onSample: (RawGpsSample) -> Unit)
    fun stop()
}

expect fun provideLocationTracker(): LocationTracker
