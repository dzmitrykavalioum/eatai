package com.eatai.shared.data.repository

import com.eatai.shared.data.api.OverpassApiClient
import com.eatai.shared.data.dto.OverpassElement
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.MarkerState
import com.eatai.shared.domain.model.Venue
import com.eatai.shared.domain.model.VenueFilter

class VenueRepository(private val apiClient: OverpassApiClient) {

    suspend fun getNearbyVenues(
        location: Location,
        filter: VenueFilter,
    ): List<Venue> {
        val radiusMeters = (filter.radiusKm * 1000).toInt()
        val response = apiClient.fetchVenues(location.latitude, location.longitude, radiusMeters)
        return response.elements
            .filter { it.effectiveLat != null && it.effectiveLon != null }
            .mapNotNull { it.toVenue() }
            .filter { venue -> matchesFilter(venue, filter) }
    }

    private fun OverpassElement.toVenue(): Venue? {
        val lat = effectiveLat ?: return null
        val lon = effectiveLon ?: return null
        val name = tags["name"] ?: return null  // skip unnamed venues

        val openingHours = tags["opening_hours"]
        val minutesRemaining = openingHours?.let { parseRemainingMinutes(it) }
        val markerState = MarkerState.fromMinutesRemaining(minutesRemaining)

        val cuisines = buildList {
            tags["cuisine"]?.split(";")?.forEach { add(it.trim()) }
            tags["amenity"]?.let { add(it) }
        }

        return Venue(
            id = "${type}_$id",
            name = name,
            location = Location(lat, lon),
            address = buildAddress(tags),
            phone = tags["phone"] ?: tags["contact:phone"],
            website = tags["website"] ?: tags["contact:website"],
            openingHours = openingHours,
            cuisines = cuisines,
            rating = null, // Google Places (future)
            markerState = markerState,
        )
    }

    private fun buildAddress(tags: Map<String, String>): String {
        val parts = listOfNotNull(
            tags["addr:housenumber"],
            tags["addr:street"],
            tags["addr:city"],
        )
        return if (parts.isNotEmpty()) parts.joinToString(", ") else tags["addr:full"] ?: ""
    }

    /**
     * Very simplified opening_hours parser for MVP.
     * Returns minutes until closing, or null if the venue is closed / hours unknown.
     * TODO: replace with a proper OSM opening_hours library.
     */
    private fun parseRemainingMinutes(openingHours: String): Long? {
        // "24/7" → always open, return a large number
        if (openingHours.trim() == "24/7") return 600L

        // For MVP, return null (unknown) for complex schedules.
        // A proper parser (e.g. opening-hours library) should be integrated here.
        return null
    }

    private fun matchesFilter(venue: Venue, filter: VenueFilter): Boolean {
        if (filter.searchQuery.isNotBlank()) {
            val q = filter.searchQuery.lowercase()
            if (!venue.name.lowercase().contains(q) &&
                venue.cuisines.none { it.lowercase().contains(q) }
            ) return false
        }
        return true
    }
}
