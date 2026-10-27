package com.eatai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatai.shared.data.preferences.AppPreferences
import com.eatai.shared.data.repository.FavoritesRepository
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.Venue
import com.eatai.shared.domain.model.VenueFilter
import com.eatai.shared.domain.usecase.GetNearbyVenuesUseCase
import com.eatai.shared.platform.LocationProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MapViewModel(
    private val getNearbyVenues: GetNearbyVenuesUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val locationProvider: LocationProvider,
    private val appPreferences: AppPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    // At most one search runs at a time; a new one supersedes the previous.
    private var searchJob: Job? = null

    init {
        loadUserLocation()
    }

    fun onFilterChanged(filter: VenueFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun onApplyFilter(filter: VenueFilter) {
        onFilterChanged(filter)
        onSearch()
    }

    fun onSearch() {
        val location = _uiState.value.userLocation ?: return
        searchJob?.cancel()
        _uiState.update { it.copy(isLoading = true, error = null) }
        searchJob = viewModelScope.launch {
            getNearbyVenues(location, _uiState.value.filter)
                .onSuccess { venues ->
                    _uiState.update { it.copy(venues = venues, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    fun onVenueTap(venue: Venue) {
        _uiState.update {
            it.copy(selectedVenue = venue, isFavorite = favoritesRepository.isFavorite(venue.id))
        }
    }

    fun onDismissDetail() {
        _uiState.update { it.copy(selectedVenue = null) }
    }

    fun toggleFavorite(venue: Venue) {
        favoritesRepository.toggle(venue)
        _uiState.update { it.copy(isFavorite = favoritesRepository.isFavorite(venue.id)) }
    }

    fun toggleViewMode() {
        _uiState.update { it.copy(isListMode = !it.isListMode) }
    }

    private fun loadUserLocation() {
        viewModelScope.launch {
            val location = locationProvider.getCurrentLocation()
            val savedRadius = appPreferences.defaultRadiusKm
            _uiState.update {
                it.copy(
                    userLocation = location,
                    filter = it.filter.copy(radiusKm = savedRadius),
                )
            }
            if (location != null) onSearch()
        }
    }
}

data class MapUiState(
    val venues: List<Venue> = emptyList(),
    val filter: VenueFilter = VenueFilter(),
    val userLocation: Location? = null,
    val selectedVenue: Venue? = null,
    val isFavorite: Boolean = false,
    val isLoading: Boolean = false,
    val isListMode: Boolean = false,
    val error: String? = null,
)
