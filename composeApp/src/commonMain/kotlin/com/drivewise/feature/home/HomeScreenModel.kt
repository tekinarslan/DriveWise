package com.drivewise.feature.home

import cafe.adriel.voyager.core.model.ScreenModel
import com.drivewise.background.BackgroundSessionRunner
import com.drivewise.session.DriveSessionController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeScreenModel(
    private val controller: DriveSessionController,
    private val realRunner: BackgroundSessionRunner,
    private val fakeRunner: BackgroundSessionRunner
) : ScreenModel {

    val state = controller.state

    private val _debugSimulate = MutableStateFlow(false)
    val debugSimulateFlow: StateFlow<Boolean> = _debugSimulate.asStateFlow()

    fun toggleSimulation(enabled: Boolean) {
        _debugSimulate.value = enabled
        controller.setRunner(if (enabled) fakeRunner else realRunner)
    }

    fun start() = controller.start()
    fun stop() = controller.stop()

    override fun onDispose() {
        controller.stop()
        super.onDispose()
    }
}
