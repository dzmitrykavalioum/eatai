package com.eatai.shared.data.repository

import com.eatai.shared.data.api.Constants
import com.eatai.shared.data.dto.Place
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.datetime.Clock

/**
 * In-memory cache of Nearby Search results, so repeated searches don't hit the billed API.
 *
 * An entry is reused when the radius is identical and the new centre is within
 * [toleranceMeters] of the entry's centre. Entries expire after [ttlMillis]; the least recently
 * used one is evicted once [maxEntries] is exceeded.
 *
 * Not thread-safe: the caller serialises access (see [VenueRepository]).
 */
internal class NearbyPlacesCache(
    private val nowMillis: () -> Long = { Clock.System.now().toEpochMilliseconds() },
    private val ttlMillis: Long = Constants.CACHE_TTL_MINUTES * 60_000L,
    private val maxEntries: Int = Constants.CACHE_MAX_ENTRIES,
    private val toleranceMeters: Double = Constants.CACHE_TOLERANCE_METERS,
) {
    private class Entry(
        val lat: Double,
        val lon: Double,
        val radiusMeters: Int,
        val fetchedAtMillis: Long,
        val places: List<Place>,
    )

    // Least recently used first.
    private val entries = mutableListOf<Entry>()

    fun get(lat: Double, lon: Double, radiusMeters: Int): List<Place>? {
        val now = nowMillis()
        entries.removeAll { now - it.fetchedAtMillis > ttlMillis }
        val hit = entries
            .filter { it.radiusMeters == radiusMeters }
            .map { it to distanceMeters(lat, lon, it.lat, it.lon) }
            .filter { (_, distance) -> distance <= toleranceMeters }
            .minByOrNull { (_, distance) -> distance }
            ?.first
            ?: return null
        entries.remove(hit)
        entries.add(hit)
        return hit.places
    }

    fun put(lat: Double, lon: Double, radiusMeters: Int, places: List<Place>) {
        // Replace an entry this one would shadow instead of keeping both.
        entries.removeAll {
            it.radiusMeters == radiusMeters && distanceMeters(lat, lon, it.lat, it.lon) <= toleranceMeters
        }
        entries.add(Entry(lat, lon, radiusMeters, nowMillis(), places))
        while (entries.size > maxEntries) entries.removeAt(0)
    }

    private fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6_371_000.0
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1.toRadians()) * cos(lat2.toRadians()) * sin(dLon / 2) * sin(dLon / 2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }

    private fun Double.toRadians() = this * (kotlin.math.PI / 180.0)
}
