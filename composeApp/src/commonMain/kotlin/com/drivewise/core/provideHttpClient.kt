package com.drivewise.core

import io.ktor.client.HttpClient

expect fun provideHttpClient(withLog: Boolean, log: (String) -> Unit): HttpClient
