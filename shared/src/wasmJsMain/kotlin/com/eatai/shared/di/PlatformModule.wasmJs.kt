package com.eatai.shared.di

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    // TODO: replace MapSettings (in-memory) with a localStorage-backed implementation
    // when wasmJs target is being actively tested in a browser.
    // Option: com.russhwolf.settings.StorageSettings() if the wasmJs artifact supports it.
    single<Settings> { MapSettings() }
}
