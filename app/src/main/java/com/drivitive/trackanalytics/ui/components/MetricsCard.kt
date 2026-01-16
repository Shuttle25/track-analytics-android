package com.drivitive.trackanalytics.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.drivitive.trackanalytics.ui.screens.MetricItem
import com.drivitive.trackanalytics.ui.theme.Track1Color
import com.drivitive.trackanalytics.ui.theme.Track2Color

@Composable
fun MetricsCard(
    title: String,
    items: List<MetricItem>,
    track1Name: String,
    track2Name: String,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = track1Name.take(12),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Track1Color
                )
                Text(
                    text = track2Name.take(12),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Track2Color
                )
            }

            Divider()

            // Data rows
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = item.label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = item.value1,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Track1Color
                    )
                    Text(
                        text = item.value2,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Track2Color
                    )
                }
            }
        }
    }
}
