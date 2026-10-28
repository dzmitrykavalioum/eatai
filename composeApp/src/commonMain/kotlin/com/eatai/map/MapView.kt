package com.eatai.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.Venue

/**
 * Displays a map centered on [userLocation] with [venues] as color-coded markers and a dot at
 * [userLocation] itself. Each increment of [recenterTrigger] moves the camera back to the user.
 * Each platform provides an [actual] implementation:
 *  - Android: Google Maps SDK via maps-compose
 *  - Web:     Google Maps JS API via JS interop + HTML div overlay
 *  - iOS:     Google Maps SDK (future)
 */
@Composable
expect fun MapView(
    venues: List<Venue>,
    userLocation: Location?,
    onVenueTap: (Venue) -> Unit,
    recenterTrigger: Int,
    modifier: Modifier = Modifier,
)
