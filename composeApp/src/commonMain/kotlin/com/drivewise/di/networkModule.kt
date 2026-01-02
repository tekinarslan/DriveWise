package com.drivewise.di

import com.drivewise.core.provideHttpClient
import com.drivewise.core.provideOrsApiKey
import org.koin.dsl.module
import com.drivewise.matching.OrsSnapClient

val networkModule = module {

    single {
        provideHttpClient(
            withLog = true,
            log = { /* optional extra sink */ }
        )
    }

    single {
        OrsSnapClient(
            http = get(),
            apiKey = provideOrsApiKey()
        )
    }
}
