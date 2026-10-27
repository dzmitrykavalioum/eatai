package com.eatai.shared.data.api

internal object Constants {
    const val PLACES_SEARCH_NEARBY_URL = "https://places.googleapis.com/v1/places:searchNearby"
    const val NOMINATIM_URL = "https://nominatim.openstreetmap.org"

    // Places API (New) "Table A" types. Nearby Search returns at most MAX_RESULT_COUNT places.
    val PLACES_INCLUDED_TYPES = listOf("cafe", "restaurant", "bar", "bakery", "fast_food_restaurant")
    const val PLACES_MAX_RESULT_COUNT = 20
    const val PLACES_MAX_RADIUS_METERS = 50_000

    // The field mask drives billing: rating / currentOpeningHours / phone / website are the higher SKU.
    const val PLACES_FIELD_MASK =
        "places.id,places.displayName,places.location,places.formattedAddress,places.types," +
            "places.rating,places.currentOpeningHours,places.nationalPhoneNumber,places.websiteUri"

    const val DEFAULT_RADIUS_METERS = 2000

    // Nearby-search cache: results are reused for the same radius and a centre within this distance.
    const val CACHE_TOLERANCE_METERS = 100.0
    const val CACHE_TTL_MINUTES = 15L
    const val CACHE_MAX_ENTRIES = 20

    // Reported as "minutes until close" for venues that are open around the clock.
    const val ALWAYS_OPEN_MINUTES = 600L
}
