package com.drivewise.di

import android.content.Context
import com.drivewise.app.DriverFactory
import org.koin.dsl.module

fun platformModule(context: Context) = module {
    single<Context> { context }
    single { DriverFactory(context) }
}
