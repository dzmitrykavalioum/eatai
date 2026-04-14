package com.eatai.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eatai.components.CafeDetailSheet
import com.eatai.components.CafeListItem
import com.eatai.components.FilterPanel
import com.eatai.map.MapView
import com.eatai.viewmodel.MapViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onOpenPreferences: () -> Unit) {
    val viewModel = koinViewModel<MapViewModel>()
    val state by viewModel.uiState.collectAsState()
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("EatAI") },
                actions = {
                    IconButton(onClick = viewModel::toggleViewMode) {
                        Icon(
                            imageVector = if (state.isListMode) Icons.Filled.Map else Icons.AutoMirrored.Filled.List,
                            contentDescription = if (state.isListMode) "Switch to map" else "Switch to list",
                        )
                    }
                    IconButton(onClick = onOpenPreferences) {
                        Icon(Icons.Filled.Settings, contentDescription = "Preferences")
                    }
                },
            )
        },
        sheetContent = {
            FilterPanel(
                filter = state.filter,
                onFilterChanged = viewModel::onFilterChanged,
            )
            Button(
                onClick = viewModel::onSearch,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxSize(),
            ) {
                Text("Search")
            }
        },
        sheetPeekHeight = 56.dp,
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (state.isListMode) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
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
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            if (state.error != null) {
                Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(8.dp)) {
                    Text(state.error!!)
                }
            }
        }
    }

    // Cafe detail sheet
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
