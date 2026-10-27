package com.eatai.shared.data.repository

import com.eatai.shared.data.api.Constants
import com.eatai.shared.data.api.PlacesApiClient
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.Venue
import com.eatai.shared.domain.model.VenueFilter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

class VenueRepository(private val apiClient: PlacesApiClient) {

    private val cache = NearbyPlacesCache()

    // Serialises cache lookup + fetch: two overlapping searches for the same area share one API call.
    private val fetchLock = Mutex()

    suspend fun getNearbyVenues(
        location: Location,
        filter: VenueFilter,
    ): List<Venue> {
        val radiusMeters = (filter.radiusKm * 1000).toInt().coerceIn(1, Constants.PLACES_MAX_RADIUS_METERS)

        // The cache is keyed by area only: the filter is applied below, so changing it never refetches.
        val places = fetchLock.withLock {
            cache.get(location.latitude, location.longitude, radiusMeters)
                ?: apiClient.fetchNearby(location.latitude, location.longitude, radiusMeters)
                    .also { cache.put(location.latitude, location.longitude, radiusMeters, it) }
        }

        val now = Clock.System.now()
        val zone = TimeZone.currentSystemDefault()
        return places
            .mapNotNull { it.toVenue(now, zone) }
            .filter { venue -> matchesFilter(venue, filter) }
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
