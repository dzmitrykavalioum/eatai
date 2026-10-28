package com.eatai.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.Venue
import com.eatai.shared.domain.model.MarkerState as VenueMarkerState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

private val DEFAULT_CENTER = LatLng(52.52, 13.405) // Berlin
private const val DEFAULT_ZOOM = 15f

@Composable
actual fun MapView(
    venues: List<Venue>,
    userLocation: Location?,
    onVenueTap: (Venue) -> Unit,
    recenterTrigger: Int,
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

    LaunchedEffect(recenterTrigger) {
        if (recenterTrigger == 0) return@LaunchedEffect  // initial composition, not a button press
        userLocation?.toLatLng()?.let {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(it, DEFAULT_ZOOM))
        }
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        // The +/- buttons would sit under our my-location button; pinch zoom still works.
        uiSettings = MapUiSettings(zoomControlsEnabled = false),
    ) {
        userLocation?.let { location ->
            Marker(
                state = MarkerState(position = location.toLatLng()),
                icon = remember { userDotIcon() },
                anchor = Offset(0.5f, 0.5f),
                zIndex = 1f,  // above the venue markers
            )
        }

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

/** Blue dot with a white ring, the usual "you are here" look. */
private fun userDotIcon(): BitmapDescriptor {
    val size = 56
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val center = size / 2f
    paint.color = Color.WHITE
    canvas.drawCircle(center, center, center, paint)
    paint.color = 0xFF1A73E8.toInt()
    canvas.drawCircle(center, center, center - 8f, paint)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private fun Location.toLatLng(): LatLng = LatLng(latitude, longitude)

private fun VenueMarkerState.toHue(): Float = when (this) {
    VenueMarkerState.OPEN         -> BitmapDescriptorFactory.HUE_GREEN
    VenueMarkerState.CLOSING_SOON -> BitmapDescriptorFactory.HUE_YELLOW
    VenueMarkerState.CLOSING      -> BitmapDescriptorFactory.HUE_ORANGE
    VenueMarkerState.LAST_CALL    -> BitmapDescriptorFactory.HUE_RED
    VenueMarkerState.CLOSED       -> BitmapDescriptorFactory.HUE_AZURE
}
