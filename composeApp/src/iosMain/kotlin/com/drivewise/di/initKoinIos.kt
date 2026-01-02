package com.drivewise.di

import org.koin.core.context.startKoin

fun initKoinIos() {
    startKoin {
        modules(
            platformModule(),
            databaseModule,
            commonModule,
            networkModule,
            permissionModule
        )
    }
}
