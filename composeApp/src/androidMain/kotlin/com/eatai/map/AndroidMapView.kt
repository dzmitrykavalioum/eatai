package com.eatai.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.Venue
import com.eatai.shared.domain.model.MarkerState as VenueMarkerState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private val DEFAULT_CENTER = LatLng(52.52, 13.405) // Berlin
private const val DEFAULT_ZOOM = 15f

@Composable
actual fun MapView(
    venues: List<Venue>,
    userLocation: Location?,
    onVenueTap: (Venue) -> Unit,
    modifier: Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLocation?.toLatLng() ?: DEFAULT_CENTER,
            DEFAULT_ZOOM,
        )
    }

    LaunchedEffect(userLocation) {
        userLocation?.toLatLng()?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, DEFAULT_ZOOM)
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        venues.forEach { venue ->
            Marker(
                state = MarkerState(
                    position = LatLng(venue.location.latitude, venue.location.longitude),
                ),
                title = venue.name,
                snippet = venue.cuisines.joinToString().ifEmpty { null },
                icon = BitmapDescriptorFactory.defaultMarker(venue.markerState.toHue()),
                onClick = {
                    onVenueTap(venue)
                    false
                },
            )
        }
    }
}

private fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)

private fun VenueMarkerState.toHue(): Float = when (this) {
    VenueMarkerState.OPEN         -> BitmapDescriptorFactory.HUE_GREEN
    VenueMarkerState.CLOSING_SOON -> BitmapDescriptorFactory.HUE_YELLOW
    VenueMarkerState.CLOSING      -> BitmapDescriptorFactory.HUE_ORANGE
    VenueMarkerState.LAST_CALL    -> BitmapDescriptorFactory.HUE_RED
    VenueMarkerState.CLOSED       -> BitmapDescriptorFactory.HUE_AZURE
}
