package com.tembt.di

import com.tembt.platform.CourtMonitoringScheduler
import com.tembt.platform.CourtMonitoringSchedulerImpl
import com.tembt.platform.CourtScheduleStorage
import com.tembt.platform.CourtScheduleStorageImpl
import com.tembt.platform.DeviceIdentityProvider
import com.tembt.platform.LocationMonitoringStateStorage
import com.tembt.platform.LocationMonitoringStateStorageImpl
import com.tembt.platform.LocationService
import com.tembt.platform.LocationServiceContract
import com.tembt.platform.PlayerStorage
import com.tembt.platform.PlayerStorageImpl
import org.koin.dsl.module

val iosPlatformModule = module {
    single<LocationServiceContract> { LocationService() }
    single<PlayerStorage> { PlayerStorageImpl() }
    single<DeviceIdentityProvider> { get<PlayerStorage>() }
    single<CourtScheduleStorage> { CourtScheduleStorageImpl() }
    single<CourtMonitoringScheduler> { CourtMonitoringSchedulerImpl() }
    single<LocationMonitoringStateStorage> { LocationMonitoringStateStorageImpl() }
}
