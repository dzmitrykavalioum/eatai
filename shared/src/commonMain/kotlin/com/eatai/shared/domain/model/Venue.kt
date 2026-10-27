package com.eatai.shared.domain.model

data class Venue(
    val id: String,
    val name: String,
    val location: Location,
    val address: String,
    val phone: String?,
    val website: String?,
    val openingHours: String?,   // today's hours, e.g. "Monday: 8:00 AM – 10:00 PM"
    val cuisines: List<String>,
    val rating: Float?,          // Google Places rating
    val markerState: MarkerState,
)
