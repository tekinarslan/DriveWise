package com.drivewise.di

import com.drivewise.core.OnboardingStore
import com.drivewise.data.TrackPointRepository
import com.drivewise.feature.home.HomeScreenModel
import com.drivewise.session.DriveSessionController
import com.drivewise.tracking.provideLocationTracker
import org.koin.dsl.module

val commonModule = module {
    single { OnboardingStore(get()) } // get() = AppDatabase
    single { TrackPointRepository(get()) }
    single { provideLocationTracker() } // LocationTracker
    single { DriveSessionController(tracker = get(), repo = get()) }
    factory { HomeScreenModel(controller = get()) }
}
