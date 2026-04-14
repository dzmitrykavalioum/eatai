package com.eatai.shared.data.api

internal object Constants {
    const val OVERPASS_API_URL = "https://overpass-api.de/api/interpreter"
    const val NOMINATIM_URL = "https://nominatim.openstreetmap.org"

    // Overpass amenity tags mapped from FoodCategory
    val AMENITY_TAGS = "cafe|restaurant|bar|bakery|fast_food"

    const val DEFAULT_RADIUS_METERS = 2000
}
