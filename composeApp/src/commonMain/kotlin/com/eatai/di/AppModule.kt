package com.eatai.di

import com.eatai.viewmodel.MapViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    viewModel {
        MapViewModel(
            getNearbyVenues = get(),
            favoritesRepository = get(),
            locationProvider = get(),
            appPreferences = get(),
        )
    }
}
