package com.tembt.di

import com.tembt.data.remote.TembtApiService
import com.tembt.data.remote.createHttpClient
import com.tembt.data.repository.CourtRepositoryImpl
import com.tembt.data.repository.PlayerRepositoryImpl
import com.tembt.domain.repository.CourtRepository
import com.tembt.domain.repository.PlayerRepository
import com.tembt.domain.usecase.GetCourtLocationUseCase
import com.tembt.domain.usecase.RegisterPlayerUseCase
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

    // Use cases — factory {} because they are stateless and cheap to recreate
    factory { RegisterPlayerUseCase(get(), get()) }
    factory { GetCourtLocationUseCase(get()) }

    // ViewModels — factory {} instead of viewModel {} because the viewModel DSL requires
    // a platform-specific Koin artifact; koinViewModel() on Android still provides
    // lifecycle scoping for factory registrations.
    factory { MapViewModel(get(), get()) }
    factory { WelcomeViewModel(get()) }
    factory { AppViewModel(get()) }
}
