package com.eatai.map

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.MarkerState
import com.eatai.shared.domain.model.Venue
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

// OpenFreeMap — full OSM detail (streets, buildings, house numbers, POIs).
// Free, no API key required. https://openfreemap.org
private const val OSM_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

@SuppressLint("MissingPermission")
@Composable
actual fun MapView(
    venues: List<Venue>,
    userLocation: Location?,
    onVenueTap: (Venue) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context).also { it.onCreate(null) } }

    // Wire the MapView to the host lifecycle.
    // Without onStart/onResume the GL surface is never set up, causing a SIGSEGV
    // in libmaplibre.so's RenderThread.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START  -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE  -> mapView.onPause()
                Lifecycle.Event.ON_STOP   -> mapView.onStop()
                else -> { /* nothing */ }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDestroy()
        }
    }

    AndroidView(
        // factory is called once — set up the map and load the style here
        factory = {
            mapView.apply {
                getMapAsync { map ->
                    map.setStyle(OSM_STYLE_URL) {
                        val target = userLocation
                            ?.let { LatLng(it.latitude, it.longitude) }
                            ?: LatLng(52.52, 13.405) // default: Berlin

                        map.cameraPosition = CameraPosition.Builder()
                            .target(target)
                            .zoom(15.0)
                            .build()

                        addVenueMarkers(map, venues, onVenueTap)
                    }
                }
            }
        },
        modifier = modifier,
    )
}

private fun addVenueMarkers(
    map: MapLibreMap,
    venues: List<Venue>,
    onVenueTap: (Venue) -> Unit,
) {
    map.clear()
    venues.forEach { _ ->
        // TODO: add coloured symbol markers per venue MarkerState
        // Use map.getStyle()?.addLayer(SymbolLayer) with a GeoJSON source
    }

    map.addOnMapClickListener { latLng ->
        val tapped = venues.minByOrNull { venue ->
            val dLat = venue.location.latitude - latLng.latitude
            val dLon = venue.location.longitude - latLng.longitude
            dLat * dLat + dLon * dLon
        }
        if (tapped != null) {
            val dLat = tapped.location.latitude - latLng.latitude
            val dLon = tapped.location.longitude - latLng.longitude
            if (dLat * dLat + dLon * dLon < 0.0001) onVenueTap(tapped)
        }
        true
    }
}

fun MarkerState.toColorHex(): String = when (this) {
    MarkerState.OPEN         -> "#4CAF50"
    MarkerState.CLOSING_SOON -> "#FFEB3B"
    MarkerState.CLOSING      -> "#FF9800"
    MarkerState.LAST_CALL    -> "#F44336"
    MarkerState.CLOSED       -> "#9E9E9E"
}
