package com.tembt.di

import com.tembt.data.remote.TembtApiService
import com.tembt.data.remote.createHttpClient
import com.tembt.data.repository.CourtRepositoryImpl
import com.tembt.data.repository.PlayerRepositoryImpl
import com.tembt.data.repository.PlayersRepositoryImpl
import com.tembt.domain.repository.CourtRepository
import com.tembt.domain.repository.PlayerRepository
import com.tembt.domain.repository.PlayersRepository
import com.tembt.domain.usecase.GetCourtLocationUseCase
import com.tembt.domain.usecase.GetPlayersAtCourtUseCase
import com.tembt.domain.usecase.RegisterPlayerUseCase
import com.tembt.domain.usecase.SendLocationUseCase
import com.tembt.presentation.app.AppViewModel
import com.tembt.presentation.map.MapViewModel
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

    // Use cases — factory {} because they are stateless and cheap to recreate
    factory { RegisterPlayerUseCase(get(), get()) }
    factory { GetCourtLocationUseCase(get()) }
    factory { GetPlayersAtCourtUseCase(get()) }
    factory { SendLocationUseCase(get(), get(), get()) }

    // ViewModels — factory {} instead of viewModel {} because the viewModel DSL requires
    // a platform-specific Koin artifact; koinViewModel() on Android still provides
    // lifecycle scoping for factory registrations.
    factory { MapViewModel(get(), get(), get(), get()) }
    factory { WelcomeViewModel(get()) }
    factory { AppViewModel(get()) }
}
