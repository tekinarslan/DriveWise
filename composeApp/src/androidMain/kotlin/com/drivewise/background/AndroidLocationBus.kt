package com.drivewise.background

import com.drivewise.tracking.RawGpsSample
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

internal object AndroidLocationBus {
    val samples = MutableSharedFlow<RawGpsSample>(extraBufferCapacity = 64)
}
