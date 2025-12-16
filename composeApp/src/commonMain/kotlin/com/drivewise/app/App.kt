package com.drivewise.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import com.drivewise.core.OnboardingStore
import com.drivewise.design.theme.DriveWiseTheme
import com.drivewise.feature.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun App() {
    val store: OnboardingStore = koinInject()

    DriveWiseTheme {
        Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Navigator(SplashScreen(store))
        }
    }
}

