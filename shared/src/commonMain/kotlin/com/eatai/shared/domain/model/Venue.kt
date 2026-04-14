package com.eatai.shared.domain.model

data class Venue(
    val id: String,
    val name: String,
    val location: Location,
    val address: String,
    val phone: String?,
    val website: String?,
    val openingHours: String?,   // raw OSM opening_hours tag value
    val cuisines: List<String>,
    val rating: Float?,          // placeholder; populated via Google Places (future)
    val markerState: MarkerState,
)
