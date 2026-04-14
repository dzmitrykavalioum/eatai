package com.eatai

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {

    // Registers a permission request launcher.
    // When the user taps Allow/Deny, the result arrives here.
    // The ViewModel's init block already called getCurrentLocation();
    // re-triggering is not needed — the OS will provide a location on
    // next resume after permissions are granted.
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* result handled by ViewModel on next resume */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ask for location permissions on first launch.
        // Android shows the system dialog once; after that it no-ops.
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )

        setContent {
            App()
        }
    }
}
