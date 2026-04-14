package com.eatai.shared.platform

import android.annotation.SuppressLint
import com.eatai.shared.domain.model.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class LocationProvider actual constructor() {

    private val fusedClient =
        LocationServices.getFusedLocationProviderClient(ContextHolder.applicationContext)

    private val _locationFlow = MutableStateFlow<Location?>(null)
    actual val locationFlow: Flow<Location?> = _locationFlow

    /**
     * Returns the current device location once, or null if:
     *  - location permission was denied, or
     *  - the device has no recent location fix.
     *
     * Permissions (ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION) must be
     * granted before calling this. MainActivity handles the runtime request.
     */
    @SuppressLint("MissingPermission")
    actual suspend fun getCurrentLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            fusedClient
                .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { androidLocation ->
                    val result = androidLocation?.let {
                        Location(it.latitude, it.longitude)
                    }
                    _locationFlow.value = result
                    cont.resume(result)
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
                .addOnCanceledListener {
                    cont.resume(null)
                }
        }
}
