package com.drivewise.core

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun provideHttpClient(
    withLog: Boolean,
    log: (String) -> Unit
): HttpClient =
    createHttpClient(
        engine = OkHttp,
        withLog = withLog,
        log = { msg ->
            Napier.d(tag = "HTTP", message = msg)
            log(msg)
        }
    )

