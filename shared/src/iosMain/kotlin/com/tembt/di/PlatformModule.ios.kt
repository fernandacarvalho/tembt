package com.tembt.di

import com.tembt.platform.LocationService
import com.tembt.platform.LocationServiceContract
import com.tembt.platform.PlayerStorage
import com.tembt.platform.PlayerStorageImpl
import org.koin.dsl.module

val iosPlatformModule = module {
    single<LocationServiceContract> { LocationService() }
    single<PlayerStorage> { PlayerStorageImpl() }
}
