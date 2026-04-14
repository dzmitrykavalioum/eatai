package com.eatai.shared.platform

import com.eatai.shared.domain.model.Location
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

actual class LocationProvider actual constructor() {

    private val _locationFlow = MutableStateFlow<Location?>(null)
    actual val locationFlow: Flow<Location?> = _locationFlow

    actual suspend fun getCurrentLocation(): Location? {
        // TODO: wire up the browser Geolocation API via Kotlin/Wasm JS interop.
        // Example:
        //   window.navigator.geolocation.getCurrentPosition(
        //       successCallback = { pos ->
        //           _locationFlow.value = Location(pos.coords.latitude, pos.coords.longitude)
        //       },
        //       errorCallback = { /* handle */ }
        //   )
        return _locationFlow.value
    }
}
