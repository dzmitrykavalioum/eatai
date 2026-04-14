package com.eatai.shared.domain.model

/**
 * Color-coded status of a venue based on remaining open time.
 * See SPEC.md §3 for the full color/time breakdown.
 */
enum class MarkerState {
    /** Open, closes in more than 2 hours. */
    OPEN,

    /** Open, closes in 1–2 hours. */
    CLOSING_SOON,

    /** Open, closes in less than 1 hour. */
    CLOSING,

    /** Open, closes in less than 15 minutes. */
    LAST_CALL,

    /** Currently closed, or opening hours unknown. */
    CLOSED;

    companion object {
        /**
         * Resolves a [MarkerState] from a remaining-open duration in minutes.
         * Pass null when hours are unknown → [CLOSED].
         */
        fun fromMinutesRemaining(minutesRemaining: Long?): MarkerState = when {
            minutesRemaining == null || minutesRemaining <= 0 -> CLOSED
            minutesRemaining < 15 -> LAST_CALL
            minutesRemaining < 60 -> CLOSING
            minutesRemaining < 120 -> CLOSING_SOON
            else -> OPEN
        }
    }
}
