package com.eatai.shared.platform

import com.eatai.shared.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Platform-specific location provider.
 * Each target provides its own [actual] implementation.
 */
expect class LocationProvider() {
    /** Emits the latest device location. Starts updates on first collect. */
    val locationFlow: Flow<Location?>

    /** Returns a single location reading, or null if unavailable. */
    suspend fun getCurrentLocation(): Location?
}
