package com.eatai.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.MarkerState
import com.eatai.shared.domain.model.Venue

@Composable
actual fun MapView(
    venues: List<Venue>,
    userLocation: Location?,
    onVenueTap: (Venue) -> Unit,
    recenterTrigger: Int,
    modifier: Modifier,
) {
    LaunchedEffect(userLocation) {
        userLocation?.let { setUserLocationJs(it.latitude, it.longitude) }
    }

    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger > 0) recenterJs()
    }

    LaunchedEffect(userLocation, venues) {
        val lat = userLocation?.latitude ?: 52.52
        val lon = userLocation?.longitude ?: 13.405
        val markersJson = venues.toMarkersJson()
        initGoogleMap(lat, lon, markersJson)
    }

    // The Google Map renders in <div id="google-map-container"> defined in index.html,
    // sitting behind the Compose canvas (CSS z-index in index.html).
    Box(modifier = modifier.fillMaxSize()) {
        if (venues.isEmpty()) {
            Text("Loading map…", modifier = Modifier.align(Alignment.Center))
        }
    }
}

/** Calls into the JS bridge in index.html to (re)initialize the Google Map. */
private fun initGoogleMap(lat: Double, lon: Double, markersJson: String) {
    initEatAiMapJs(lat, lon, markersJson)
}

@JsFun("(lat, lon) => window.setEatAiUserLocation(lat, lon)")
private external fun setUserLocationJs(lat: Double, lon: Double)

@JsFun("() => window.recenterEatAiMap()")
private external fun recenterJs()

@JsFun("(lat, lon, markersJson) => window.initEatAiMap(lat, lon, markersJson)")
private external fun initEatAiMapJs(lat: Double, lon: Double, markersJson: String)

private fun List<Venue>.toMarkersJson(): String =
    joinToString(prefix = "[", postfix = "]") { it.toMarkerJson() }

private fun Venue.toMarkerJson(): String {
    val color = markerState.toColorHex()
    return """{"id":"${id.jsEscape()}","lat":${location.latitude},"lon":${location.longitude},""" +
        """"name":"${name.jsEscape()}","color":"$color"}"""
}

private fun MarkerState.toColorHex(): String = when (this) {
    MarkerState.OPEN         -> "#4CAF50"
    MarkerState.CLOSING_SOON -> "#FFEB3B"
    MarkerState.CLOSING      -> "#FF9800"
    MarkerState.LAST_CALL    -> "#F44336"
    MarkerState.CLOSED       -> "#9E9E9E"
}

private fun String.jsEscape(): String =
    replace("\\", "\\\\").replace("\"", "\\\"")
