package com.eatai

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import com.eatai.di.appModule
import com.eatai.shared.data.api.PlacesConfig
import com.eatai.shared.di.platformModule
import com.eatai.shared.di.sharedModule
import com.eatai.shared.platform.initSharedAndroid
import java.security.MessageDigest
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class EatAiApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Provide application context to shared-module platform implementations
        initSharedAndroid(this)

        // Start Koin DI
        startKoin {
            androidContext(this@EatAiApplication)
            modules(sharedModule, platformModule(), appModule, module { single { placesConfig() } })
        }
    }

    /**
     * Same key as Google Maps. A key restricted to Android apps only accepts REST calls that
     * identify the app, so send the package name and signing-certificate SHA-1 with each request.
     */
    private fun placesConfig(): PlacesConfig {
        val headers = buildMap {
            put("X-Android-Package", packageName)
            signingCertSha1()?.let { put("X-Android-Cert", it) }
        }
        return PlacesConfig(apiKey = BuildConfig.MAPS_API_KEY, extraHeaders = headers)
    }

    @Suppress("DEPRECATION")
    private fun signingCertSha1(): String? {
        val signature = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                .signingInfo?.apkContentsSigners?.firstOrNull()
        } else {
            packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                .signatures?.firstOrNull()
        } ?: return null
        return MessageDigest.getInstance("SHA-1")
            .digest(signature.toByteArray())
            .joinToString("") { "%02X".format(it) }
    }
}
