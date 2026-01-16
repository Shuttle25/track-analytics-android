package com.drivitive.trackanalytics.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.drivitive.trackanalytics.data.model.OverlapResult
import com.drivitive.trackanalytics.ui.theme.Track1Color
import com.drivitive.trackanalytics.ui.theme.Track2Color

@Composable
fun OverlapCard(
    overlap: OverlapResult,
    track1Name: String,
    track2Name: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Route Overlap",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Track 1 overlap
            Column {
                Text(
                    text = track1Name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Track1Color
                )
                LinearProgressIndicator(
                    progress = (overlap.track1OverlapPercent / 100).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Track1Color,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Shared: ${"%.1f".format(overlap.track1OverlapKm)} km (${"%.0f".format(overlap.track1OverlapPercent)}%)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Unique: ${"%.1f".format(overlap.track1UniqueKm)} km",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Track 2 overlap
            Column {
                Text(
                    text = track2Name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Track2Color
                )
                LinearProgressIndicator(
                    progress = (overlap.track2OverlapPercent / 100).toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Track2Color,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Shared: ${"%.1f".format(overlap.track2OverlapKm)} km (${"%.0f".format(overlap.track2OverlapPercent)}%)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Unique: ${"%.1f".format(overlap.track2UniqueKm)} km",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Divider()

            Text(
                text = "Approximate shared route: ${"%.2f".format(overlap.sharedDistanceKm)} km",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
