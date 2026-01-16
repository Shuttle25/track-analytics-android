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
import androidx.compose.ui.graphics.drawscope.Stroke
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

            // Chart
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                val width = size.width
                val height = size.height
                val padding = 8f

                if (chartData.elevations1.isNotEmpty()) {
                    drawElevationPath(
                        elevations = chartData.elevations1,
                        minEle = chartData.minElevation,
                        maxEle = chartData.maxElevation,
                        width = width,
                        height = height,
                        padding = padding,
                        color = Track1Color
                    )
                }

                if (chartData.elevations2.isNotEmpty()) {
                    drawElevationPath(
                        elevations = chartData.elevations2,
                        minEle = chartData.minElevation,
                        maxEle = chartData.maxElevation,
                        width = width,
                        height = height,
                        padding = padding,
                        color = Track2Color
                    )
                }
            }

            // Min/Max labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${chartData.minElevation.toInt()}m",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${chartData.maxElevation.toInt()}m",
                    style = MaterialTheme.typography.bodySmall
                )
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawElevationPath(
    elevations: List<Double>,
    minEle: Double,
    maxEle: Double,
    width: Float,
    height: Float,
    padding: Float,
    color: Color
) {
    if (elevations.isEmpty()) return

    val eleRange = (maxEle - minEle).coerceAtLeast(1.0)
    val chartWidth = width - 2 * padding
    val chartHeight = height - 2 * padding

    val path = Path()
    elevations.forEachIndexed { index, elevation ->
        val x = padding + (index.toFloat() / (elevations.size - 1).coerceAtLeast(1)) * chartWidth
        val y = padding + chartHeight - ((elevation - minEle) / eleRange * chartHeight).toFloat()

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
    val elevations2: List<Double>,
    val minElevation: Double,
    val maxElevation: Double
)

private fun prepareChartData(track1: Track, track2: Track): ChartData {
    val ele1 = track1.points.mapNotNull { it.elevation }.sample(100)
    val ele2 = track2.points.mapNotNull { it.elevation }.sample(100)

    val allElevations = ele1 + ele2
    val minEle = allElevations.minOrNull() ?: 0.0
    val maxEle = allElevations.maxOrNull() ?: 100.0

    return ChartData(
        elevations1 = ele1,
        elevations2 = ele2,
        minElevation = minEle,
        maxElevation = maxEle
    )
}

private fun List<Double>.sample(maxPoints: Int): List<Double> {
    if (size <= maxPoints) return this
    val step = size / maxPoints
    return filterIndexed { index, _ -> index % step == 0 }
}
