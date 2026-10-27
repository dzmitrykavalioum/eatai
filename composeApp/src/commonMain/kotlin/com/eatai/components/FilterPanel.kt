package com.eatai.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eatai.shared.domain.model.Availability
import com.eatai.shared.domain.model.FoodCategory
import com.eatai.shared.domain.model.VenueFilter
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterPanel(
    filter: VenueFilter,
    onFilterChanged: (VenueFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Food categories
        Text("Category")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FoodCategory.entries.forEach { category ->
                val selected = category in filter.categories
                FilterChip(
                    selected = selected,
                    onClick = {
                        val updated = if (selected)
                            filter.categories - category
                        else
                            filter.categories + category
                        onFilterChanged(filter.copy(categories = updated))
                    },
                    label = { Text(category.label) },
                )
            }
        }

        // Availability
        Text("Availability")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Availability.entries.forEach { avail ->
                FilterChip(
                    selected = filter.availability == avail,
                    onClick = { onFilterChanged(filter.copy(availability = avail)) },
                    label = { Text(avail.label) },
                )
            }
        }

        // Search radius slider
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Radius", modifier = Modifier.widthIn(min = 60.dp))
            Slider(
                value = filter.radiusKm,
                onValueChange = { onFilterChanged(filter.copy(radiusKm = it)) },
                valueRange = 0.5f..10f,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            )
            Text("${filter.radiusKm.roundToInt()} km", modifier = Modifier.widthIn(min = 40.dp))
        }
    }
}
