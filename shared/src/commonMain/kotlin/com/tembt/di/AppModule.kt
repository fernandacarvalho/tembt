package com.tembt.di

import com.tembt.presentation.map.MapViewModel
import org.koin.dsl.module

val appModule = module {
    // factory {} is used instead of viewModel {} because the viewModel DSL requires
    // a platform-specific Koin artifact. koinViewModel() on Android correctly promotes
    // this factory registration to a lifecycle-scoped ViewModel.
    factory { MapViewModel(get()) }
}
