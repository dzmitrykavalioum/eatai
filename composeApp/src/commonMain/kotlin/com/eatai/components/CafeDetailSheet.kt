package com.eatai.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eatai.shared.domain.model.Venue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeDetailSheet(
    venue: Venue,
    isFavorite: Boolean,
    onDismiss: () -> Unit,
    onToggleFavorite: () -> Unit,
    onGetDirections: (Venue) -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

            // Header row: name + favorite toggle
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(venue.name, style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Status badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                MarkerColorIndicator(state = venue.markerState)
                Text(venue.markerState.toLabel(), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(12.dp))

            // Address
            if (venue.address.isNotBlank()) {
                DetailRow(label = "Address", value = venue.address)
            }

            // Opening hours
            venue.openingHours?.let { DetailRow(label = "Hours", value = it) }

            // Phone
            venue.phone?.let { DetailRow(label = "Phone", value = it) }

            // Website
            venue.website?.let { DetailRow(label = "Website", value = it) }

            // Cuisine tags
            if (venue.cuisines.isNotEmpty()) {
                DetailRow(label = "Cuisine", value = venue.cuisines.joinToString(", "))
            }

            // Rating placeholder (Google Places — future)
            DetailRow(label = "Rating", value = venue.rating?.toString() ?: "N/A")

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { onGetDirections(venue) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Get Directions")
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
