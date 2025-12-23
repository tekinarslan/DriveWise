package com.drivewise.di

import com.drivewise.core.OnboardingStore
import com.drivewise.data.TrackPointRepository
import com.drivewise.feature.home.HomeScreenModel
import com.drivewise.session.DriveSessionController
import com.drivewise.tracking.AdaptiveSampler
import org.koin.core.qualifier.named
import org.koin.dsl.module

val commonModule = module {
    single { OnboardingStore(get()) }
    single<TrackPointRepository> { TrackPointRepository(get()) }

    // ✅ DefaultAdaptiveSampler yok — direkt AdaptiveSampler yarat
    single { AdaptiveSampler() }

    // ✅ Controller runner'ı named("real") ile alacak
    single {
        DriveSessionController(
            repo = get(),
            sampler = get(),
            bgRunner = get(named("real"))
        )
    }

    // ✅ HomeScreenModel runner’ları alıp toggle’da controller’a set edecek
    factory {
        HomeScreenModel(
            controller = get(),
            realRunner = get(named("real")),
            fakeRunner = get(named("fake"))
        )
    }
}
