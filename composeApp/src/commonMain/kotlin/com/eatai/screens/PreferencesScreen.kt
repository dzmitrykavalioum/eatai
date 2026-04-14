package com.eatai.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eatai.shared.data.preferences.AppPreferences
import com.eatai.shared.data.repository.FavoritesRepository
import kotlin.math.roundToInt
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(onBack: () -> Unit) {
    val prefs = koinInject<AppPreferences>()
    val favoritesRepo = koinInject<FavoritesRepository>()

    var radius by remember { mutableFloatStateOf(prefs.defaultRadiusKm) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Preferences") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Default search radius
            Text("Default Search Radius", style = MaterialTheme.typography.titleMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Slider(
                    value = radius,
                    onValueChange = {
                        radius = it
                        prefs.defaultRadiusKm = it
                    },
                    valueRange = 0.5f..10f,
                    modifier = Modifier.weight(1f),
                )
                Text("${radius.roundToInt()} km", modifier = Modifier.widthIn(min = 40.dp))
            }

            HorizontalDivider()

            // Dark mode — placeholder
            Text("Dark Mode", style = MaterialTheme.typography.titleMedium)
            Text(
                "Coming soon",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider()

            // Account — placeholder
            Text("Account", style = MaterialTheme.typography.titleMedium)
            Text(
                "Login / Register coming soon",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            HorizontalDivider()

            // Clear favorites
            Text("Data", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { /* TODO: clear favorites via favoritesRepo */ }) {
                Text("Clear all favorites", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
