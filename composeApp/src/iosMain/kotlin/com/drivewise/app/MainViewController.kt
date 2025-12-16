package com.drivewise.app

import androidx.compose.ui.window.ComposeUIViewController
import com.drivewise.di.initKoinIos

fun MainViewController() = ComposeUIViewController {
    initKoinIos()
    App()
}
