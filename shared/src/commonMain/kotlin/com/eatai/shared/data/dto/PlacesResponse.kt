package com.eatai.shared.data.dto

import kotlinx.serialization.Serializable

// Request body of places:searchNearby. No defaults: the JSON encoder omits default values.
@Serializable
data class SearchNearbyRequest(
    val includedTypes: List<String>,
    val maxResultCount: Int,
    val rankPreference: String,
    val locationRestriction: LocationRestriction,
)

@Serializable
data class LocationRestriction(val circle: Circle)

@Serializable
data class Circle(val center: PlaceLocation, val radius: Double)

@Serializable
data class PlacesResponse(
    val places: List<Place> = emptyList(),
)

@Serializable
data class Place(
    val id: String,
    val displayName: PlaceText? = null,
    val location: PlaceLocation? = null,
    val formattedAddress: String? = null,
    val types: List<String> = emptyList(),
    val rating: Float? = null,
    val currentOpeningHours: PlaceOpeningHours? = null,
    val nationalPhoneNumber: String? = null,
    val websiteUri: String? = null,
)

@Serializable
data class PlaceText(val text: String)

@Serializable
data class PlaceLocation(val latitude: Double, val longitude: Double)

@Serializable
data class PlaceOpeningHours(
    val openNow: Boolean? = null,
    /** RFC 3339 timestamp; absent for venues that are open 24/7. */
    val nextCloseTime: String? = null,
    /** Monday-first, e.g. "Monday: 8:00 AM – 10:00 PM". */
    val weekdayDescriptions: List<String> = emptyList(),
)

@Serializable
data class PlacesErrorResponse(val error: PlacesError? = null)

@Serializable
data class PlacesError(val message: String? = null, val status: String? = null)
