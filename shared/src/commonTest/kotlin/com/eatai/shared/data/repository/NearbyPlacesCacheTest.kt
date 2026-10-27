package com.eatai.shared.data.repository

import com.eatai.shared.data.dto.Place
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NearbyPlacesCacheTest {

    private var now = 0L
    private val cache = NearbyPlacesCache(
        nowMillis = { now },
        ttlMillis = 15 * 60_000L,
        maxEntries = 3,
        toleranceMeters = 100.0,
    )

    private val places = listOf(Place(id = "a"))

    // 0.0005° of latitude ≈ 55 m; 0.002° ≈ 222 m.
    private val lat = 53.9
    private val lon = 27.56

    @Test
    fun `hit for same area`() {
        cache.put(lat, lon, 2000, places)
        assertEquals(places, cache.get(lat, lon, 2000))
    }

    @Test
    fun `hit within 100 metres, miss beyond`() {
        cache.put(lat, lon, 2000, places)
        assertNotNull(cache.get(lat + 0.0005, lon, 2000))
        assertNull(cache.get(lat + 0.002, lon, 2000))
    }

    @Test
    fun `miss for a different radius`() {
        cache.put(lat, lon, 2000, places)
        assertNull(cache.get(lat, lon, 2001))
    }

    @Test
    fun `expires after ttl`() {
        cache.put(lat, lon, 2000, places)
        now = 15 * 60_000L
        assertNotNull(cache.get(lat, lon, 2000))
        now = 15 * 60_000L + 1
        assertNull(cache.get(lat, lon, 2000))
    }

    @Test
    fun `evicts the least recently used entry`() {
        cache.put(lat + 0.0, lon, 1000, places)
        cache.put(lat + 0.1, lon, 1000, places)
        cache.put(lat + 0.2, lon, 1000, places)
        cache.get(lat + 0.0, lon, 1000)             // touch the oldest
        cache.put(lat + 0.3, lon, 1000, places)     // evicts lat + 0.1

        assertNotNull(cache.get(lat + 0.0, lon, 1000))
        assertNull(cache.get(lat + 0.1, lon, 1000))
        assertNotNull(cache.get(lat + 0.2, lon, 1000))
        assertNotNull(cache.get(lat + 0.3, lon, 1000))
    }

    @Test
    fun `put replaces a shadowed entry instead of duplicating it`() {
        cache.put(lat, lon, 2000, listOf(Place(id = "old")))
        cache.put(lat + 0.0005, lon, 2000, listOf(Place(id = "new")))
        assertEquals("new", cache.get(lat, lon, 2000)?.single()?.id)
    }
}
