package com.drivewise.di

import org.koin.dsl.module
import com.drivewise.permission.LocationPermissionChecker
import com.drivewise.permission.provideLocationPermissionChecker

val permissionModule = module {
    single<LocationPermissionChecker> { provideLocationPermissionChecker() }
}