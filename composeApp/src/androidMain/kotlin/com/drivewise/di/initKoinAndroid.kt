package com.drivewise.di

import android.content.Context
import org.koin.core.context.startKoin

fun initKoinAndroid(context: Context) {
    startKoin {
        modules(
            platformModule(context),
            databaseModule,
            commonModule,
            permissionModule
        )
    }
}
