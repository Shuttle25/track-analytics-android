package com.drivitive.trackanalytics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.drivitive.trackanalytics.data.model.Track
import com.drivitive.trackanalytics.data.model.TrackMetrics
import com.drivitive.trackanalytics.data.repository.TrackAnalyzer
import java.time.Duration

@Composable
fun SingleTrackCard(
    track: Track,
    metrics: TrackMetrics,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = track.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )

            // Distance section
            SingleMetricSection(title = "Distance") {
                SingleMetricRow("Total distance", "${formatDistance(metrics.totalDistanceKm)} km")
                SingleMetricRow("Points", "${metrics.pointCount}")
            }

            // Elevation section
            metrics.elevation?.let { ele ->
                SingleMetricSection(title = "Elevation") {
                    SingleMetricRow("Min elevation", "${ele.minElevation.toInt()} m")
                    SingleMetricRow("Max elevation", "${ele.maxElevation.toInt()} m")
                    SingleMetricRow("Elevation range", "${ele.elevationRange.toInt()} m")
                    SingleMetricRow("Total ascent", "${ele.totalAscent.toInt()} m")
                    SingleMetricRow("Total descent", "${ele.totalDescent.toInt()} m")
                }

                // Single track elevation chart
                SingleTrackElevationChart(
                    track = track,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            // Speed section
            metrics.speed?.let { spd ->
                SingleMetricSection(title = "Speed & Time") {
                    SingleMetricRow("Duration", formatDuration(spd.duration))
                    SingleMetricRow("Moving time", formatDuration(spd.movingTime))
                    SingleMetricRow("Average speed", "${formatSpeed(spd.avgSpeedKmh)} km/h")
                    SingleMetricRow("Avg moving speed", "${formatSpeed(spd.avgMovingSpeedKmh)} km/h")
                    SingleMetricRow("Max speed", "${formatSpeed(spd.maxSpeedKmh)} km/h")
                }
            }
        }
    }
}

@Composable
private fun SingleMetricSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

@Composable
private fun SingleMetricRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SingleTrackElevationChart(
    track: Track,
    color: Color,
    modifier: Modifier = Modifier
) {
    val chartData = remember(track) {
        prepareSingleChartData(track)
    }

    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier) {
        val leftPadding = 50f
        val bottomPadding = 40f
        val topPadding = 16f
        val rightPadding = 16f

        val chartWidth = size.width - leftPadding - rightPadding
        val chartHeight = size.height - bottomPadding - topPadding

        // Draw grid and axes
        drawChartGrid(
            leftPadding = leftPadding,
            topPadding = topPadding,
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            minEle = chartData.minElevation,
            maxEle = chartData.maxElevation,
            maxDist = chartData.totalDistanceKm,
            axisColor = axisColor,
            gridColor = gridColor
        )

        // Draw elevation path
        if (chartData.elevations.isNotEmpty()) {
            drawSingleElevationPath(
                elevations = chartData.elevations,
                distances = chartData.distances,
                minEle = chartData.minElevation,
                maxEle = chartData.maxElevation,
                maxDist = chartData.totalDistanceKm,
                leftPadding = leftPadding,
                topPadding = topPadding,
                chartWidth = chartWidth,
                chartHeight = chartHeight,
                color = color
            )
        }
    }
}

private fun DrawScope.drawChartGrid(
    leftPadding: Float,
    topPadding: Float,
    chartWidth: Float,
    chartHeight: Float,
    minEle: Double,
    maxEle: Double,
    maxDist: Double,
    axisColor: Color,
    gridColor: Color
) {
    val paint = android.graphics.Paint().apply {
        textSize = 28f
        this.color = android.graphics.Color.GRAY
        isAntiAlias = true
    }

    // Y axis
    drawLine(
        color = axisColor,
        start = Offset(leftPadding, topPadding),
        end = Offset(leftPadding, topPadding + chartHeight),
        strokeWidth = 2f
    )

    // X axis
    drawLine(
        color = axisColor,
        start = Offset(leftPadding, topPadding + chartHeight),
        end = Offset(leftPadding + chartWidth, topPadding + chartHeight),
        strokeWidth = 2f
    )

    // Y axis labels and grid lines
    val eleRange = (maxEle - minEle).coerceAtLeast(1.0)
    val ySteps = 4
    for (i in 0..ySteps) {
        val y = topPadding + chartHeight - (i.toFloat() / ySteps) * chartHeight
        val eleValue = minEle + (i.toFloat() / ySteps) * eleRange

        // Grid line
        if (i > 0 && i < ySteps) {
            drawLine(
                color = gridColor,
                start = Offset(leftPadding, y),
                end = Offset(leftPadding + chartWidth, y),
                strokeWidth = 1f
            )
        }

        // Label
        drawContext.canvas.nativeCanvas.drawText(
            "${eleValue.toInt()}m",
            5f,
            y + 10f,
            paint
        )
    }

    // X axis labels and grid lines
    val xSteps = 4
    for (i in 0..xSteps) {
        val x = leftPadding + (i.toFloat() / xSteps) * chartWidth
        val distValue = (i.toFloat() / xSteps) * maxDist

        // Grid line
        if (i > 0 && i < xSteps) {
            drawLine(
                color = gridColor,
                start = Offset(x, topPadding),
                end = Offset(x, topPadding + chartHeight),
                strokeWidth = 1f
            )
        }

        // Label
        drawContext.canvas.nativeCanvas.drawText(
            "%.1f".format(distValue),
            x - 15f,
            topPadding + chartHeight + 30f,
            paint
        )
    }

    // Axis titles
    paint.textSize = 24f
    drawContext.canvas.nativeCanvas.drawText(
        "km",
        leftPadding + chartWidth - 20f,
        topPadding + chartHeight + 35f,
        paint
    )
}

private fun DrawScope.drawSingleElevationPath(
    elevations: List<Double>,
    distances: List<Double>,
    minEle: Double,
    maxEle: Double,
    maxDist: Double,
    leftPadding: Float,
    topPadding: Float,
    chartWidth: Float,
    chartHeight: Float,
    color: Color
) {
    if (elevations.isEmpty() || maxDist <= 0) return

    val eleRange = (maxEle - minEle).coerceAtLeast(1.0)

    val path = Path()
    elevations.forEachIndexed { index, elevation ->
        val x = leftPadding + (distances[index] / maxDist * chartWidth).toFloat()
        val y = topPadding + chartHeight - ((elevation - minEle) / eleRange * chartHeight).toFloat()

        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 3f)
    )
}

private data class SingleChartData(
    val elevations: List<Double>,
    val distances: List<Double>,
    val minElevation: Double,
    val maxElevation: Double,
    val totalDistanceKm: Double
)

private fun prepareSingleChartData(track: Track): SingleChartData {
    val cumulativeDist = TrackAnalyzer.cumulativeDistances(track)
    val totalDist = cumulativeDist.lastOrNull() ?: 0.0

    val elevations = mutableListOf<Double>()
    val distances = mutableListOf<Double>()

    track.points.forEachIndexed { index, point ->
        point.elevation?.let { ele ->
            elevations.add(ele)
            distances.add(cumulativeDist.getOrElse(index) { 0.0 })
        }
    }

    // Sample if too many points
    val maxPoints = 150
    val sampledEle: List<Double>
    val sampledDist: List<Double>

    if (elevations.size > maxPoints) {
        val step = elevations.size / maxPoints
        sampledEle = elevations.filterIndexed { i, _ -> i % step == 0 }
        sampledDist = distances.filterIndexed { i, _ -> i % step == 0 }
    } else {
        sampledEle = elevations
        sampledDist = distances
    }

    val minEle = sampledEle.minOrNull() ?: 0.0
    val maxEle = sampledEle.maxOrNull() ?: 100.0

    return SingleChartData(
        elevations = sampledEle,
        distances = sampledDist,
        minElevation = minEle,
        maxElevation = maxEle,
        totalDistanceKm = totalDist
    )
}

private fun formatDistance(km: Double): String = "%.2f".format(km)
private fun formatSpeed(kmh: Double): String = "%.1f".format(kmh)

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutesPart()
    val seconds = duration.toSecondsPart()

    return when {
        hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}
