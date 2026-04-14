package com.eatai.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.Venue

/**
 * Displays a map centered on [userLocation] with [venues] as color-coded markers.
 * Each platform provides an [actual] implementation:
 *  - Android: MapLibre Android SDK via AndroidView
 *  - Web:     MapLibre GL JS via JS interop + HTML div overlay
 *  - iOS:     MapLibre iOS SDK (future)
 */
@Composable
expect fun MapView(
    venues: List<Venue>,
    userLocation: Location?,
    onVenueTap: (Venue) -> Unit,
    modifier: Modifier = Modifier,
)
