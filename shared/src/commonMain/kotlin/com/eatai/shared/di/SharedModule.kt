package com.eatai.shared.di

import com.eatai.shared.data.api.PlacesApiClient
import com.eatai.shared.data.preferences.AppPreferences
import com.eatai.shared.data.repository.FavoritesRepository
import com.eatai.shared.data.repository.VenueRepository
import com.eatai.shared.domain.usecase.GetNearbyVenuesUseCase
import com.eatai.shared.platform.LocationProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val sharedModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }
    }

    // PlacesConfig (API key) is provided by each platform entry point.
    single { PlacesApiClient(get(), get()) }
    single { VenueRepository(get()) }
    single { FavoritesRepository(get()) }
    single { AppPreferences(get()) }
    single { GetNearbyVenuesUseCase(get()) }

    // Location — platform-specific actual implementation
    single { LocationProvider() }

    // Settings is provided by platformModule() — see PlatformModule.kt
}
