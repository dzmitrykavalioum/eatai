package com.eatai

import android.app.Application
import com.eatai.di.appModule
import com.eatai.shared.di.platformModule
import com.eatai.shared.di.sharedModule
import com.eatai.shared.platform.initSharedAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.maplibre.android.MapLibre

class EatAiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Provide application context to shared-module platform implementations
        initSharedAndroid(this)

        // Initialize MapLibre (required before any MapView is created)
        MapLibre.getInstance(this)

        // Start Koin DI
        startKoin {
            androidContext(this@EatAiApplication)
            modules(sharedModule, platformModule(), appModule)
        }
    }
}
