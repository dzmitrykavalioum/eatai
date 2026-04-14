package com.eatai.shared.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    single<Settings> {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("eatai_prefs", Context.MODE_PRIVATE)
        )
    }
}
