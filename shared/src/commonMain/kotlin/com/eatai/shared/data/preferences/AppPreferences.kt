package com.eatai.shared.data.preferences

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set

class AppPreferences(private val settings: Settings) {

    var defaultRadiusKm: Float
        get() = settings.getFloat(KEY_RADIUS, DEFAULT_RADIUS)
        set(value) = settings.set(KEY_RADIUS, value)

    var preferredCuisines: List<String>
        get() = settings.getStringOrNull(KEY_CUISINES)
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?: emptyList()
        set(value) = settings.set(KEY_CUISINES, value.joinToString(","))

    companion object {
        private const val KEY_RADIUS = "pref_radius_km"
        private const val KEY_CUISINES = "pref_cuisines"
        private const val DEFAULT_RADIUS = 2f
    }
}
