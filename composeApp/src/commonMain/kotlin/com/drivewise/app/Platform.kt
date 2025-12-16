package com.drivewise.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform