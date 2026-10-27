package com.eatai.shared.domain.usecase

import com.eatai.shared.data.repository.VenueRepository
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.Venue
import com.eatai.shared.domain.model.VenueFilter
import kotlin.coroutines.cancellation.CancellationException

class GetNearbyVenuesUseCase(private val repository: VenueRepository) {

    suspend operator fun invoke(location: Location, filter: VenueFilter): Result<List<Venue>> =
        runCatching { repository.getNearbyVenues(location, filter) }
            // A cancelled search is not a failure to report (the ViewModel cancels superseded searches).
            .onFailure { if (it is CancellationException) throw it }
}
