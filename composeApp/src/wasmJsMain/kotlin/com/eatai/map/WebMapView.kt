package com.eatai.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.Venue

@Composable
actual fun MapView(
    venues: List<Venue>,
    userLocation: Location?,
    onVenueTap: (Venue) -> Unit,
    modifier: Modifier,
) {
    LaunchedEffect(userLocation, venues) {
        val lat = userLocation?.latitude ?: 52.52
        val lon = userLocation?.longitude ?: 13.405
        initMapLibreMap(lat, lon, venues, onVenueTap)
    }

    // The MapLibre GL JS map renders in a <div id="maplibre-container"> defined in index.html.
    // This composable just ensures the map is initialized/updated when state changes.
    // The div sits behind the Compose canvas; use CSS z-index to layer correctly.
    Box(modifier = modifier.fillMaxSize()) {
        if (venues.isEmpty()) {
            Text("Loading map…", modifier = Modifier.align(Alignment.Center))
        }
    }
}

/**
 * Calls MapLibre GL JS via Kotlin/Wasm JS interop to initialize or update the map.
 * The map is rendered in a <div id="maplibre-container"> outside the Compose canvas.
 *
 * TODO: implement full JS interop when the Web target is being tested in a browser.
 * See index.html and the MapLibre GL JS documentation for the required JS bindings.
 */
private fun initMapLibreMap(
    lat: Double,
    lon: Double,
    venues: List<Venue>,
    onVenueTap: (Venue) -> Unit,
) {
    // JS interop entry point — implement using @JsExport / external declarations
    // Example:
    //   js("window.initEatAiMap(lat, lon, markersJson)")
}
