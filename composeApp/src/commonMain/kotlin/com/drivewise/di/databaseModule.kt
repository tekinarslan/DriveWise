package com.drivewise.di

import com.drivewise.app.DatabaseProvider
import org.koin.dsl.module

val databaseModule = module {
    single { DatabaseProvider(get()).db } // get() = DriverFactory
}
