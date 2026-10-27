package com.eatai.shared.data.api

/**
 * Credentials for the Places API, provided by each platform entry point.
 *
 * [extraHeaders] carries the app-identity headers needed when the key is restricted by application
 * (Android: `X-Android-Package` / `X-Android-Cert`).
 */
class PlacesConfig(
    val apiKey: String,
    val extraHeaders: Map<String, String> = emptyMap(),
)
