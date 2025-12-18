package com.drivewise.feature.home

import cafe.adriel.voyager.core.model.ScreenModel
import com.drivewise.session.DriveSessionController
import com.drivewise.tracking.DebugRoutes
import com.drivewise.tracking.FakeLocationTracker
import com.drivewise.tracking.LocationTracker
import com.drivewise.tracking.provideLocationTracker
import kotlinx.coroutines.flow.StateFlow

class HomeScreenModel(
    private val controller: DriveSessionController
) : ScreenModel {

    val state = controller.state // StateFlow<DriveSessionState>

    private val realTracker: LocationTracker = provideLocationTracker()
    private val fakeTracker: LocationTracker = FakeLocationTracker(
        route = DebugRoutes.berlinShort,
        intervalMs = 1000L
    )

    var debugSimulate: Boolean = false
        private set

    fun toggleSimulation(enabled: Boolean) {
        debugSimulate = enabled
        controller.setTracker(if (enabled) fakeTracker else realTracker)
    }

    fun start() = controller.start()
    fun stop() = controller.stop()

    override fun onDispose() {
        controller.stop()
        super.onDispose()
    }
}
