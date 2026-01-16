package com.drivitive.trackanalytics.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.drivitive.trackanalytics.data.model.*
import com.drivitive.trackanalytics.ui.components.ElevationChart
import com.drivitive.trackanalytics.ui.components.MetricsCard
import com.drivitive.trackanalytics.ui.components.OverlapCard
import com.drivitive.trackanalytics.ui.theme.Track1Color
import com.drivitive.trackanalytics.ui.theme.Track2Color
import java.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComparisonScreen(
    track1Name: String?,
    track2Name: String?,
    comparisonResult: ComparisonResult?,
    isLoading: Boolean,
    onSelectTrack1: () -> Unit,
    onSelectTrack2: () -> Unit,
    onCompare: () -> Unit,
    canCompare: Boolean
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Analytics") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Track selection
            TrackSelectionCard(
                track1Name = track1Name,
                track2Name = track2Name,
                onSelectTrack1 = onSelectTrack1,
                onSelectTrack2 = onSelectTrack2,
                onCompare = onCompare,
                canCompare = canCompare,
                isLoading = isLoading
            )

            // Results
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            comparisonResult?.let { result ->
                // Distance
                MetricsCard(
                    title = "Distance",
                    items = listOf(
                        MetricItem("Distance", "${formatDistance(result.metrics1.totalDistanceKm)} km", "${formatDistance(result.metrics2.totalDistanceKm)} km"),
                        MetricItem("Points", "${result.metrics1.pointCount}", "${result.metrics2.pointCount}")
                    ),
                    track1Name = result.track1.name,
                    track2Name = result.track2.name
                )

                // Elevation
                if (result.metrics1.elevation != null && result.metrics2.elevation != null) {
                    MetricsCard(
                        title = "Elevation",
                        items = listOf(
                            MetricItem("Min", "${result.metrics1.elevation.minElevation.toInt()} m", "${result.metrics2.elevation.minElevation.toInt()} m"),
                            MetricItem("Max", "${result.metrics1.elevation.maxElevation.toInt()} m", "${result.metrics2.elevation.maxElevation.toInt()} m"),
                            MetricItem("Ascent", "${result.metrics1.elevation.totalAscent.toInt()} m", "${result.metrics2.elevation.totalAscent.toInt()} m"),
                            MetricItem("Descent", "${result.metrics1.elevation.totalDescent.toInt()} m", "${result.metrics2.elevation.totalDescent.toInt()} m")
                        ),
                        track1Name = result.track1.name,
                        track2Name = result.track2.name
                    )

                    // Elevation chart
                    ElevationChart(
                        track1 = result.track1,
                        track2 = result.track2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                // Speed
                if (result.metrics1.speed != null && result.metrics2.speed != null) {
                    MetricsCard(
                        title = "Speed & Time",
                        items = listOf(
                            MetricItem("Duration", formatDuration(result.metrics1.speed.duration), formatDuration(result.metrics2.speed.duration)),
                            MetricItem("Moving", formatDuration(result.metrics1.speed.movingTime), formatDuration(result.metrics2.speed.movingTime)),
                            MetricItem("Avg speed", "${formatSpeed(result.metrics1.speed.avgSpeedKmh)} km/h", "${formatSpeed(result.metrics2.speed.avgSpeedKmh)} km/h"),
                            MetricItem("Max speed", "${formatSpeed(result.metrics1.speed.maxSpeedKmh)} km/h", "${formatSpeed(result.metrics2.speed.maxSpeedKmh)} km/h")
                        ),
                        track1Name = result.track1.name,
                        track2Name = result.track2.name
                    )
                }

                // Overlap
                OverlapCard(
                    overlap = result.overlap,
                    track1Name = result.track1.name,
                    track2Name = result.track2.name
                )
            }
        }
    }
}

@Composable
private fun TrackSelectionCard(
    track1Name: String?,
    track2Name: String?,
    onSelectTrack1: () -> Unit,
    onSelectTrack2: () -> Unit,
    onCompare: () -> Unit,
    canCompare: Boolean,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Tracks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSelectTrack1,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Track1Color
                    )
                ) {
                    Text(
                        text = track1Name ?: "Track 1",
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = onSelectTrack2,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Track2Color
                    )
                ) {
                    Text(
                        text = track2Name ?: "Track 2",
                        maxLines = 1
                    )
                }
            }

            Button(
                onClick = onCompare,
                enabled = canCompare && !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Compare")
            }
        }
    }
}

data class MetricItem(
    val label: String,
    val value1: String,
    val value2: String
)

private fun formatDistance(km: Double): String = "%.2f".format(km)
private fun formatSpeed(kmh: Double): String = "%.1f".format(kmh)

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()
    val seconds = duration.toSecondsPart()

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
