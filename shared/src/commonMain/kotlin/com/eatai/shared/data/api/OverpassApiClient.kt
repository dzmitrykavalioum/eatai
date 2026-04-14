package com.eatai.shared.data.api

import com.eatai.shared.data.dto.OverpassResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters

class OverpassApiClient(private val httpClient: HttpClient) {

    /**
     * Queries Overpass API for cafes/restaurants within [radiusMeters] of [lat],[lon].
     */
    suspend fun fetchVenues(
        lat: Double,
        lon: Double,
        radiusMeters: Int = Constants.DEFAULT_RADIUS_METERS,
    ): OverpassResponse {
        val query = buildQuery(lat, lon, radiusMeters)
        return httpClient.submitForm(
            url = Constants.OVERPASS_API_URL,
            formParameters = Parameters.build { append("data", query) },
        ).body()
    }

    private fun buildQuery(lat: Double, lon: Double, radius: Int): String = """
        [out:json][timeout:25];
        (
          node["amenity"~"${Constants.AMENITY_TAGS}"](around:$radius,$lat,$lon);
          way["amenity"~"${Constants.AMENITY_TAGS}"](around:$radius,$lat,$lon);
        );
        out center tags;
    """.trimIndent()
}
