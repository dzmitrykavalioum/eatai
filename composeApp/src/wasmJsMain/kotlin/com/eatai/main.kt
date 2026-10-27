package com.eatai

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.eatai.di.appModule
import com.eatai.shared.data.api.PlacesConfig
import com.eatai.shared.di.platformModule
import com.eatai.shared.di.sharedModule
import kotlinx.browser.document
import org.koin.core.context.startKoin
import org.koin.dsl.module

// Injected into index.html at build time (see composeApp/build.gradle.kts).
private fun readMapsApiKey(): String = js("window.__EATAI_MAPS_KEY || ''")

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(
            sharedModule,
            platformModule(),
            appModule,
            // The web key is restricted by HTTP referrer, which the browser sends itself.
            module { single { PlacesConfig(apiKey = readMapsApiKey()) } },
        )
    }

    ComposeViewport(document.body!!) {
        App()
    }
}
