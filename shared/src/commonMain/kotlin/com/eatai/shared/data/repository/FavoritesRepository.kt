package com.eatai.shared.data.repository

import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.MarkerState
import com.eatai.shared.domain.model.Venue
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Stores favorites locally using multiplatform-settings (no account required).
 * Serializes a minimal snapshot of each Venue to JSON.
 * When the backend is available, this will sync with the remote favorites API.
 */
class FavoritesRepository(private val settings: Settings) {

    private val json = Json { ignoreUnknownKeys = true }
    private val _favorites = MutableStateFlow(loadFromSettings())

    val favorites: Flow<List<FavoriteEntry>> = _favorites.asStateFlow()

    fun isFavorite(venueId: String): Boolean =
        _favorites.value.any { it.id == venueId }

    fun toggle(venue: Venue) {
        val current = _favorites.value.toMutableList()
        val existing = current.indexOfFirst { it.id == venue.id }
        if (existing >= 0) current.removeAt(existing)
        else current.add(FavoriteEntry.fromVenue(venue))
        _favorites.value = current
        persist(current)
    }

    private fun persist(entries: List<FavoriteEntry>) {
        settings.putString(KEY_FAVORITES, json.encodeToString(entries))
    }

    private fun loadFromSettings(): List<FavoriteEntry> {
        val raw = settings.getStringOrNull(KEY_FAVORITES) ?: return emptyList()
        return runCatching { json.decodeFromString<List<FavoriteEntry>>(raw) }
            .getOrDefault(emptyList())
    }

    companion object {
        private const val KEY_FAVORITES = "favorites_list"
    }
}

@kotlinx.serialization.Serializable
data class FavoriteEntry(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val address: String,
) {
    companion object {
        fun fromVenue(v: Venue) = FavoriteEntry(
            id = v.id,
            name = v.name,
            lat = v.location.latitude,
            lon = v.location.longitude,
            address = v.address,
        )
    }

    fun toVenue() = Venue(
        id = id,
        name = name,
        location = Location(lat, lon),
        address = address,
        phone = null,
        website = null,
        openingHours = null,
        cuisines = emptyList(),
        rating = null,
        markerState = MarkerState.CLOSED,
    )
}
