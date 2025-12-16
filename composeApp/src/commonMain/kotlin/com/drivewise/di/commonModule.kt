package com.drivewise.di

import com.drivewise.core.OnboardingStore
import org.koin.dsl.module

val commonModule = module {
    single { OnboardingStore(get()) } // get() = AppDatabase
}
