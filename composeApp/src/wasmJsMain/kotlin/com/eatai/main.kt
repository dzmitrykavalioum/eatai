package com.eatai

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.eatai.di.appModule
import com.eatai.shared.di.platformModule
import com.eatai.shared.di.sharedModule
import kotlinx.browser.document
import org.koin.core.context.startKoin

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin {
        modules(sharedModule, platformModule(), appModule)
    }

    ComposeViewport(document.body!!) {
        App()
    }
}
