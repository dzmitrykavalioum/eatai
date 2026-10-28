package com.eatai.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.eatai.components.CafeDetailSheet
import com.eatai.components.CafeListItem
import com.eatai.map.MapView
import com.eatai.shared.domain.model.FoodCategory
import com.eatai.viewmodel.MapViewModel

@Composable
fun MapScreen(
    viewModel: MapViewModel,
    onOpenFilters: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    // Incremented by the "my location" button; the map moves the camera back to the user on each change.
    var recenterTrigger by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isListMode) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // Leave room for the search controls above and the Search button below.
                contentPadding = PaddingValues(top = 128.dp, bottom = 80.dp),
            ) {
                items(state.venues, key = { it.id }) { venue ->
                    CafeListItem(
                        venue = venue,
                        distanceKm = null, // TODO: compute from userLocation
                        onClick = { viewModel.onVenueTap(venue) },
                    )
                }
            }
        } else {
            MapView(
                venues = state.venues,
                userLocation = state.userLocation,
                onVenueTap = viewModel::onVenueTap,
                recenterTrigger = recenterTrigger,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SearchField(
                query = state.filter.searchQuery,
                isListMode = state.isListMode,
                onQueryChange = { viewModel.onFilterChanged(state.filter.copy(searchQuery = it)) },
                onSearch = viewModel::onSearch,
                onToggleViewMode = viewModel::toggleViewMode,
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(FoodCategory.entries) { category ->
                        val selected = category in state.filter.categories
                        FilterChip(
                            selected = selected,
                            onClick = {
                                val updated = if (selected) {
                                    state.filter.categories - category
                                } else {
                                    state.filter.categories + category
                                }
                                viewModel.onFilterChanged(state.filter.copy(categories = updated))
                            },
                            label = { Text(category.label) },
                            // Opaque background so chips stay readable over the map.
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ),
                        )
                    }
                }
                FilledTonalIconButton(
                    onClick = onOpenFilters,
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Icon(Icons.Filled.Tune, contentDescription = "Filters")
                }
            }
        }

        if (!state.isListMode && state.userLocation != null) {
            SmallFloatingActionButton(
                onClick = { recenterTrigger++ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 72.dp),
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "My location")
            }
        }

        Button(
            onClick = viewModel::onSearch,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text("Search")
        }

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        state.error?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // Above the Search button and the my-location button.
                    .padding(start = 8.dp, end = 8.dp, bottom = 128.dp),
            ) {
                Text(error)
            }
        }
    }

    state.selectedVenue?.let { venue ->
        CafeDetailSheet(
            venue = venue,
            isFavorite = state.isFavorite,
            onDismiss = viewModel::onDismissDetail,
            onToggleFavorite = { viewModel.toggleFavorite(venue) },
            onGetDirections = { /* TODO: open external maps */ },
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    isListMode: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onToggleViewMode: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search cafes, cuisine…") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = onToggleViewMode) {
                    Icon(
                        imageVector = if (isListMode) Icons.Filled.Map else Icons.AutoMirrored.Filled.List,
                        contentDescription = if (isListMode) "Switch to map" else "Switch to list",
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
