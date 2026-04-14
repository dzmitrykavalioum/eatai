package com.eatai.shared.domain.usecase

import com.eatai.shared.data.repository.VenueRepository
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.Venue
import com.eatai.shared.domain.model.VenueFilter

class GetNearbyVenuesUseCase(private val repository: VenueRepository) {

    suspend operator fun invoke(location: Location, filter: VenueFilter): Result<List<Venue>> =
        runCatching { repository.getNearbyVenues(location, filter) }
}
