package com.tembt.di

import com.tembt.platform.LocationService
import com.tembt.platform.LocationServiceContract
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidPlatformModule = module {
    single<LocationServiceContract> { LocationService(androidContext()) }
}
