package com.eatai.shared.data.repository

import com.eatai.shared.data.api.Constants
import com.eatai.shared.data.dto.Place
import com.eatai.shared.domain.model.Location
import com.eatai.shared.domain.model.MarkerState
import com.eatai.shared.domain.model.Venue
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// Generic Places types that say nothing about the kind of venue.
private val genericTypes = setOf("point_of_interest", "establishment", "food", "store", "food_store")

/**
 * Maps a Places result to a [Venue]. Time-dependent parts (marker colour, today's hours) are
 * computed against [now], so results served from the cache stay current.
 */
internal fun Place.toVenue(now: Instant, zone: TimeZone): Venue? {
    val name = displayName?.text ?: return null  // skip unnamed venues
    val position = location ?: return null

    return Venue(
        id = id,
        name = name,
        location = Location(position.latitude, position.longitude),
        address = formattedAddress.orEmpty(),
        phone = nationalPhoneNumber,
        website = websiteUri,
        openingHours = todaysHours(now, zone),
        cuisines = types.filter { it !in genericTypes }.map { it.replace('_', ' ') },
        rating = rating,
        markerState = MarkerState.fromMinutesRemaining(minutesUntilClose(now)),
    )
}

/** Minutes until closing, or null when closed / hours unknown. */
internal fun Place.minutesUntilClose(now: Instant): Long? {
    val hours = currentOpeningHours ?: return null
    if (hours.openNow != true) return null
    // Open with no closing time: open around the clock.
    val closeAt = hours.nextCloseTime ?: return Constants.ALWAYS_OPEN_MINUTES
    val close = runCatching { Instant.parse(closeAt) }.getOrNull() ?: return null
    return (close - now).inWholeMinutes
}

private fun Place.todaysHours(now: Instant, zone: TimeZone): String? {
    val descriptions = currentOpeningHours?.weekdayDescriptions.orEmpty()
    // Places lists Monday first, which matches DayOfWeek's ordinal.
    val day = now.toLocalDateTime(zone).dayOfWeek.ordinal
    return descriptions.getOrNull(day)
}
