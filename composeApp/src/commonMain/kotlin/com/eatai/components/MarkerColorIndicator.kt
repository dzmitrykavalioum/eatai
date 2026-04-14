package com.eatai.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eatai.shared.domain.model.MarkerState

@Composable
fun MarkerColorIndicator(state: MarkerState, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(12.dp)
            .background(color = state.toColor(), shape = CircleShape),
    )
}

fun MarkerState.toColor(): Color = when (this) {
    MarkerState.OPEN -> Color(0xFF4CAF50)
    MarkerState.CLOSING_SOON -> Color(0xFFFFEB3B)
    MarkerState.CLOSING -> Color(0xFFFF9800)
    MarkerState.LAST_CALL -> Color(0xFFF44336)
    MarkerState.CLOSED -> Color(0xFF9E9E9E)
}

fun MarkerState.toLabel(): String = when (this) {
    MarkerState.OPEN -> "Open"
    MarkerState.CLOSING_SOON -> "Closing soon"
    MarkerState.CLOSING -> "Closing"
    MarkerState.LAST_CALL -> "Last call"
    MarkerState.CLOSED -> "Closed"
}
