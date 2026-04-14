package com.eatai.shared.domain.model

data class VenueFilter(
    val categories: Set<FoodCategory> = emptySet(),
    val searchQuery: String = "",
    val availability: Availability = Availability.ANY,
    val radiusKm: Float = 2f,
)

enum class FoodCategory(val label: String) {
    COFFEE("Coffee"),
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    DESSERTS("Desserts"),
    VEGAN("Vegan"),
    BAKERY("Bakery"),
    FAST_FOOD("Fast Food"),
}

enum class Availability(val label: String) {
    ANY("Any time"),
    OPEN_NOW("Open now"),
    OPEN_IN_1H("Open in 1h"),
    OPEN_IN_2H("Open in 2h"),
    OPEN_IN_3H("Open in 3h"),
}
