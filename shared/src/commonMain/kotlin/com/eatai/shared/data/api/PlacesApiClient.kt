package com.eatai.shared.data.api

import com.eatai.shared.data.dto.Circle
import com.eatai.shared.data.dto.LocationRestriction
import com.eatai.shared.data.dto.Place
import com.eatai.shared.data.dto.PlaceLocation
import com.eatai.shared.data.dto.PlacesErrorResponse
import com.eatai.shared.data.dto.PlacesResponse
import com.eatai.shared.data.dto.SearchNearbyRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

class PlacesApiClient(
    private val httpClient: HttpClient,
    private val config: PlacesConfig,
) {

    /**
     * Fetches venues within [radiusMeters] of [lat],[lon] with one Nearby Search request.
     *
     * Every call is billed, so this never retries: a failure is reported to the caller as is.
     */
    suspend fun fetchNearby(
        lat: Double,
        lon: Double,
        radiusMeters: Int = Constants.DEFAULT_RADIUS_METERS,
    ): List<Place> {
        val request = SearchNearbyRequest(
            includedTypes = Constants.PLACES_INCLUDED_TYPES,
            maxResultCount = Constants.PLACES_MAX_RESULT_COUNT,
            locationRestriction = LocationRestriction(
                Circle(
                    center = PlaceLocation(lat, lon),
                    radius = radiusMeters.coerceIn(1, Constants.PLACES_MAX_RADIUS_METERS).toDouble(),
                ),
            ),
        )

        val response = httpClient.post(Constants.PLACES_SEARCH_NEARBY_URL) {
            header("X-Goog-Api-Key", config.apiKey)
            header("X-Goog-FieldMask", Constants.PLACES_FIELD_MASK)
            config.extraHeaders.forEach { (name, value) -> header(name, value) }
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (!response.status.isSuccess()) {
            throw PlacesApiException(response.status.value, describeError(response.bodyAsText()))
        }
        return response.body<PlacesResponse>().places
    }

    private fun describeError(body: String): String =
        runCatching { errorJson.decodeFromString<PlacesErrorResponse>(body).error?.message }
            .getOrNull()
            ?: body.take(200)

    private companion object {
        val errorJson = Json { ignoreUnknownKeys = true }
    }
}

class PlacesApiException(val statusCode: Int, detail: String) :
    Exception("Places API error $statusCode: $detail")
