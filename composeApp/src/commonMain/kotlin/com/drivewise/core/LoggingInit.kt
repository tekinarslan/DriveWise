package com.drivewise.core

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

object LoggingInit {
    fun initLogging() {
        Napier.base(DebugAntilog())
        Napier.i("Napier initialized ✅")
    }
}
