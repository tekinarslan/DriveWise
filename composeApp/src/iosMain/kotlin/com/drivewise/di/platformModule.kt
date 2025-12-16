package com.drivewise.di

import com.drivewise.app.DriverFactory
import org.koin.dsl.module

fun platformModule() = module {
    single { DriverFactory() }
}
