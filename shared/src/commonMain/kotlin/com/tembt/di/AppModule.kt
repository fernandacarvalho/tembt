package com.tembt.di

import com.tembt.data.remote.TembtApiService
import com.tembt.data.remote.createHttpClient
import com.tembt.data.repository.CourtRepositoryImpl
import com.tembt.data.repository.FeatureFlagRepositoryImpl
import com.tembt.data.repository.LocationRepositoryImpl
import com.tembt.data.repository.PlayerRepositoryImpl
import com.tembt.data.repository.PlayersRepositoryImpl
import com.tembt.data.repository.TournamentRepositoryImpl
import com.tembt.data.repository.WindowRepositoryImpl
import com.tembt.domain.repository.CourtRepository
import com.tembt.domain.repository.FeatureFlagRepository
import com.tembt.domain.repository.LocationRepository
import com.tembt.domain.repository.PlayerRepository
import com.tembt.domain.repository.PlayersRepository
import com.tembt.domain.repository.TournamentRepository
import com.tembt.domain.repository.WindowRepository
import com.tembt.domain.usecase.ApplyStationaryBackoffUseCase
import com.tembt.domain.usecase.CalculateDistanceUseCase
import com.tembt.domain.usecase.GetEnabledTabsUseCase
import com.tembt.domain.usecase.GetTournamentsUseCase
import com.tembt.domain.usecase.SessionTabConfig
import com.tembt.domain.usecase.CheckinUseCase
import com.tembt.domain.usecase.GetCourtLocation
import com.tembt.domain.usecase.GetCourtLocationUseCase
import com.tembt.domain.usecase.GetLocationUpdateIntervalUseCase
import com.tembt.domain.usecase.GetPlayersAtCourt
import com.tembt.domain.usecase.GetPlayersAtCourtUseCase
import com.tembt.domain.usecase.GetWindowUseCase
import com.tembt.domain.usecase.IsCourtOpenUseCase
import com.tembt.domain.usecase.LocationMonitoringCoordinator
import com.tembt.domain.usecase.RegisterPlayerUseCase
import com.tembt.domain.usecase.SendLocation
import com.tembt.domain.usecase.SendLocationUseCase
import com.tembt.presentation.app.AppViewModel
import com.tembt.presentation.map.MapViewModel
import com.tembt.presentation.schedule.ScheduleViewModel
import com.tembt.presentation.tournament.TournamentViewModel
import com.tembt.presentation.welcome.WelcomeViewModel
import org.koin.dsl.module

val appModule = module {
    // HTTP client (single instance shared across services)
    single { createHttpClient() }

    // API service
    single { TembtApiService(get()) }

    // Repositories
    single<PlayerRepository> { PlayerRepositoryImpl(get()) }
    single<CourtRepository> { CourtRepositoryImpl(get()) }
    single<PlayersRepository> { PlayersRepositoryImpl(get()) }
    single<WindowRepository> { WindowRepositoryImpl(get()) }
    single<TournamentRepository> { TournamentRepositoryImpl(get()) }
    single<LocationRepository> { LocationRepositoryImpl(get()) }
    single<FeatureFlagRepository> { FeatureFlagRepositoryImpl() }

    // Session-scoped tab config — single so the resolved tabs stay frozen for the whole process
    single { SessionTabConfig(get()) }

    // Use cases — factory {} because they are stateless and cheap to recreate
    factory { RegisterPlayerUseCase(get(), get()) }
    factory<GetCourtLocation> { GetCourtLocationUseCase(get()) }
    factory<GetPlayersAtCourt> { GetPlayersAtCourtUseCase(get()) }
    factory<SendLocation> { SendLocationUseCase(get(), get(), get()) }
    factory { GetWindowUseCase(get()) }
    factory { CheckinUseCase(get()) }
    factory { CalculateDistanceUseCase() }
    factory { GetLocationUpdateIntervalUseCase() }
    factory { IsCourtOpenUseCase() }
    factory { ApplyStationaryBackoffUseCase(get()) }
    factory { GetTournamentsUseCase(get()) }
    factory { GetEnabledTabsUseCase(get()) }
    factory {
        LocationMonitoringCoordinator(
            isCourtOpen                  = get(),
            calculateDistance            = get(),
            getInterval                  = get(),
            applyStationaryBackoff       = get(),
            sendLocation                 = get(),
            locationService              = get(),
            courtRepository              = get(),
            windowRepository             = get(),
            playerStorage                = get(),
            courtScheduleStorage         = get(),
            locationMonitoringStateStorage = get()
        )
    }

    // ViewModels — factory {} instead of viewModel {} because the viewModel DSL requires
    // a platform-specific Koin artifact; koinViewModel() on Android still provides
    // lifecycle scoping for factory registrations.
    factory { MapViewModel(get(), get(), get(), get()) }
    factory { WelcomeViewModel(get()) }
    factory { AppViewModel(get(), get()) }
    factory { ScheduleViewModel(get(), get(), get()) }
    factory { TournamentViewModel(get()) }
}
