package com.drivewise.di

import android.content.Context
import com.drivewise.app.DriverFactory
import org.koin.dsl.module

import com.drivewise.background.BackgroundSessionRunner
import com.drivewise.background.createBackgroundSessionRunner
import com.drivewise.background.FakeBackgroundSessionRunner
import com.drivewise.tracking.DebugRoutes
import org.koin.core.qualifier.named

fun platformModule(context: Context) = module {
    single { context.applicationContext }
    single { DriverFactory(context) }
    single { com.drivewise.app.DatabaseProvider(get()) }
    single { get<com.drivewise.app.DatabaseProvider>().db }

    single<BackgroundSessionRunner>(named("real")) { createBackgroundSessionRunner() }

    single<BackgroundSessionRunner>(named("fake")) {
        FakeBackgroundSessionRunner(
            route = DebugRoutes.berlinTourList,
            intervalMs = 1000L
        )
    }
}
