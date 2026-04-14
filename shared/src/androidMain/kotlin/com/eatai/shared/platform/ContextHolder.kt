package com.eatai.shared.platform

import android.content.Context

internal object ContextHolder {
    lateinit var applicationContext: Context
}

/**
 * Call this in [Application.onCreate] before starting Koin.
 * Required so that platform `actual` classes in the shared module
 * can access Android APIs without depending on composeApp.
 */
fun initSharedAndroid(context: Context) {
    ContextHolder.applicationContext = context.applicationContext
}
