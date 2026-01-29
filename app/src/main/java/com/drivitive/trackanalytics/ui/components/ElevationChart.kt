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
import com.drivitive.trackanalytics.data.repository.TrackAnalyzer
import com.drivitive.trackanalytics.ui.theme.Track1Color
import com.drivitive.trackanalytics.ui.theme.Track2Color

@Composable
fun ElevationChart(
    track1: Track,
    track2: Track,
    modifier: Modifier = Modifier
) {
    val chartData = remember(track1, track2) {
        prepareChartData(track1, track2)
    }

    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Elevation Profile",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(color = Track1Color, label = track1.name)
                LegendItem(color = Track2Color, label = track2.name)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Chart with axes
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                val leftPadding = 50f
                val bottomPadding = 40f
                val topPadding = 8f
                val rightPadding = 8f

                val chartWidth = size.width - leftPadding - rightPadding
                val chartHeight = size.height - bottomPadding - topPadding

                // Draw axes and grid
                drawAxesAndGrid(
                    leftPadding = leftPadding,
                    topPadding = topPadding,
                    chartWidth = chartWidth,
                    chartHeight = chartHeight,
                    minEle = chartData.minElevation,
                    maxEle = chartData.maxElevation,
                    maxDist = chartData.maxDistanceKm,
                    axisColor = axisColor,
                    gridColor = gridColor
                )

                // Draw track 1
                if (chartData.elevations1.isNotEmpty()) {
                    drawElevationPath(
                        elevations = chartData.elevations1,
                        distances = chartData.distances1,
                        minEle = chartData.minElevation,
                        maxEle = chartData.maxElevation,
                        maxDist = chartData.maxDistanceKm,
                        leftPadding = leftPadding,
                        topPadding = topPadding,
                        chartWidth = chartWidth,
                        chartHeight = chartHeight,
                        color = Track1Color
                    )
                }

                // Draw track 2
                if (chartData.elevations2.isNotEmpty()) {
                    drawElevationPath(
                        elevations = chartData.elevations2,
                        distances = chartData.distances2,
                        minEle = chartData.minElevation,
                        maxEle = chartData.maxElevation,
                        maxDist = chartData.maxDistanceKm,
                        leftPadding = leftPadding,
                        topPadding = topPadding,
                        chartWidth = chartWidth,
                        chartHeight = chartHeight,
                        color = Track2Color
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Text(
            text = label.take(15),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun DrawScope.drawAxesAndGrid(
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
        textSize = 26f
        color = android.graphics.Color.GRAY
        isAntiAlias = true
    }

    // Y axis (elevation)
    drawLine(
        color = axisColor,
        start = Offset(leftPadding, topPadding),
        end = Offset(leftPadding, topPadding + chartHeight),
        strokeWidth = 2f
    )

    // X axis (distance)
    drawLine(
        color = axisColor,
        start = Offset(leftPadding, topPadding + chartHeight),
        end = Offset(leftPadding + chartWidth, topPadding + chartHeight),
        strokeWidth = 2f
    )

    // Y axis labels and horizontal grid
    val eleRange = (maxEle - minEle).coerceAtLeast(1.0)
    val ySteps = 4
    for (i in 0..ySteps) {
        val y = topPadding + chartHeight - (i.toFloat() / ySteps) * chartHeight
        val eleValue = minEle + (i.toFloat() / ySteps) * eleRange

        // Horizontal grid line
        if (i > 0 && i < ySteps) {
            drawLine(
                color = gridColor,
                start = Offset(leftPadding, y),
                end = Offset(leftPadding + chartWidth, y),
                strokeWidth = 1f
            )
        }

        // Y label (elevation in meters)
        drawContext.canvas.nativeCanvas.drawText(
            "${eleValue.toInt()}",
            5f,
            y + 8f,
            paint
        )
    }

    // Draw "m" label for Y axis
    paint.textSize = 22f
    drawContext.canvas.nativeCanvas.drawText(
        "m",
        15f,
        topPadding - 2f,
        paint
    )

    // X axis labels and vertical grid
    paint.textSize = 26f
    val xSteps = 5
    for (i in 0..xSteps) {
        val x = leftPadding + (i.toFloat() / xSteps) * chartWidth
        val distValue = (i.toFloat() / xSteps) * maxDist

        // Vertical grid line
        if (i > 0 && i < xSteps) {
            drawLine(
                color = gridColor,
                start = Offset(x, topPadding),
                end = Offset(x, topPadding + chartHeight),
                strokeWidth = 1f
            )
        }

        // X label (distance in km)
        val label = if (distValue < 10) "%.1f".format(distValue) else "${distValue.toInt()}"
        drawContext.canvas.nativeCanvas.drawText(
            label,
            x - 12f,
            topPadding + chartHeight + 25f,
            paint
        )
    }

    // Draw "km" label for X axis
    paint.textSize = 22f
    drawContext.canvas.nativeCanvas.drawText(
        "km",
        leftPadding + chartWidth - 25f,
        topPadding + chartHeight + 35f,
        paint
    )
}

private fun DrawScope.drawElevationPath(
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

private data class ChartData(
    val elevations1: List<Double>,
    val distances1: List<Double>,
    val elevations2: List<Double>,
    val distances2: List<Double>,
    val minElevation: Double,
    val maxElevation: Double,
    val maxDistanceKm: Double
)

private fun prepareChartData(track1: Track, track2: Track): ChartData {
    val dist1 = TrackAnalyzer.cumulativeDistances(track1)
    val dist2 = TrackAnalyzer.cumulativeDistances(track2)

    val ele1 = mutableListOf<Double>()
    val sampledDist1 = mutableListOf<Double>()
    track1.points.forEachIndexed { index, point ->
        point.elevation?.let { ele ->
            ele1.add(ele)
            sampledDist1.add(dist1.getOrElse(index) { 0.0 })
        }
    }

    val ele2 = mutableListOf<Double>()
    val sampledDist2 = mutableListOf<Double>()
    track2.points.forEachIndexed { index, point ->
        point.elevation?.let { ele ->
            ele2.add(ele)
            sampledDist2.add(dist2.getOrElse(index) { 0.0 })
        }
    }

    // Sample points
    val maxPoints = 120
    val (finalEle1, finalDist1) = sampleData(ele1, sampledDist1, maxPoints)
    val (finalEle2, finalDist2) = sampleData(ele2, sampledDist2, maxPoints)

    val allElevations = finalEle1 + finalEle2
    val minEle = allElevations.minOrNull() ?: 0.0
    val maxEle = allElevations.maxOrNull() ?: 100.0

    val maxDist = maxOf(
        dist1.lastOrNull() ?: 0.0,
        dist2.lastOrNull() ?: 0.0
    )

    return ChartData(
        elevations1 = finalEle1,
        distances1 = finalDist1,
        elevations2 = finalEle2,
        distances2 = finalDist2,
        minElevation = minEle,
        maxElevation = maxEle,
        maxDistanceKm = maxDist
    )
}

private fun sampleData(
    elevations: List<Double>,
    distances: List<Double>,
    maxPoints: Int
): Pair<List<Double>, List<Double>> {
    if (elevations.size <= maxPoints) {
        return Pair(elevations, distances)
    }

    val step = elevations.size / maxPoints
    val sampledEle = elevations.filterIndexed { i, _ -> i % step == 0 }
    val sampledDist = distances.filterIndexed { i, _ -> i % step == 0 }
    return Pair(sampledEle, sampledDist)
}
