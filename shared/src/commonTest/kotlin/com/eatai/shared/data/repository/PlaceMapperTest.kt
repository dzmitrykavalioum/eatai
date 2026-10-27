package com.eatai.shared.data.repository

import com.eatai.shared.data.dto.Place
import com.eatai.shared.data.dto.PlaceLocation
import com.eatai.shared.data.dto.PlaceOpeningHours
import com.eatai.shared.data.dto.PlaceText
import com.eatai.shared.domain.model.MarkerState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone

class PlaceMapperTest {

    // 2026-10-27 is a Tuesday.
    private val now = Instant.parse("2026-10-27T18:00:00Z")

    private fun place(hours: PlaceOpeningHours?) = Place(
        id = "p1",
        displayName = PlaceText("Cafe"),
        location = PlaceLocation(53.9, 27.56),
        types = listOf("cafe", "point_of_interest", "coffee_shop", "establishment"),
        currentOpeningHours = hours,
    )

    private fun state(hours: PlaceOpeningHours?) = place(hours).toVenue(now, TimeZone.UTC)!!.markerState

    @Test
    fun `marker state follows time until nextCloseTime`() {
        fun open(close: String) = PlaceOpeningHours(openNow = true, nextCloseTime = close)
        assertEquals(MarkerState.OPEN, state(open("2026-10-27T21:00:00Z")))         // 3 h
        assertEquals(MarkerState.CLOSING_SOON, state(open("2026-10-27T19:30:00Z"))) // 90 min
        assertEquals(MarkerState.CLOSING, state(open("2026-10-27T18:30:00Z")))      // 30 min
        assertEquals(MarkerState.LAST_CALL, state(open("2026-10-27T18:10:00Z")))    // 10 min
    }

    @Test
    fun `closed, unknown and already-passed closing time are CLOSED`() {
        assertEquals(MarkerState.CLOSED, state(null))
        assertEquals(MarkerState.CLOSED, state(PlaceOpeningHours(openNow = false)))
        assertEquals(MarkerState.CLOSED, state(PlaceOpeningHours(openNow = null)))
        // Cached result whose closing time has passed since it was fetched.
        assertEquals(MarkerState.CLOSED, state(PlaceOpeningHours(true, "2026-10-27T17:00:00Z")))
    }

    @Test
    fun `open without closing time is open around the clock`() {
        assertEquals(MarkerState.OPEN, state(PlaceOpeningHours(openNow = true)))
    }

    @Test
    fun `picks today's weekday line and drops generic types`() {
        val days = listOf("Monday: 8-22", "Tuesday: 9-23", "Wednesday: 10-24", "Thursday: x", "Friday: x", "Saturday: x", "Sunday: x")
        val venue = place(PlaceOpeningHours(openNow = true, weekdayDescriptions = days)).toVenue(now, TimeZone.UTC)!!
        assertEquals("Tuesday: 9-23", venue.openingHours)
        assertEquals(listOf("cafe", "coffee shop"), venue.cuisines)
    }

    @Test
    fun `place without a name or location is skipped`() {
        assertNull(Place(id = "x", location = PlaceLocation(1.0, 1.0)).toVenue(now, TimeZone.UTC))
        assertNull(Place(id = "x", displayName = PlaceText("n")).toVenue(now, TimeZone.UTC))
    }
}
