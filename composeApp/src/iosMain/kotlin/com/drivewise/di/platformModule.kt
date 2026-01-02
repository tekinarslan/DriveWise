package com.drivewise.di

import com.drivewise.app.DriverFactory
import com.drivewise.background.BackgroundSessionRunner
import com.drivewise.background.FakeBackgroundSessionRunner
import com.drivewise.background.createBackgroundSessionRunner
import com.drivewise.tracking.DebugRoutes
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun platformModule() = module {
    single { DriverFactory() }
    single { com.drivewise.app.DatabaseProvider(get()) }
    single { get<com.drivewise.app.DatabaseProvider>().db }

    single<BackgroundSessionRunner>(named("real")) { createBackgroundSessionRunner() }

    single<BackgroundSessionRunner>(named("fake")) {
        FakeBackgroundSessionRunner(
            route = DebugRoutes.berlinCharlottenburg10k,
            intervalMs = 1000L
        )
    }
}
