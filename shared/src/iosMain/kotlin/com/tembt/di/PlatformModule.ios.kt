package com.tembt.di

import com.tembt.platform.LocationService
import com.tembt.platform.LocationServiceContract
import org.koin.dsl.module

val iosPlatformModule = module {
    single<LocationServiceContract> { LocationService() }
}
