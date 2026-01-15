package com.shuttle25.trackanalytics.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.shuttle25.trackanalytics.data.model.Track
import com.shuttle25.trackanalytics.data.repository.TrackAnalyzer
import com.shuttle25.trackanalytics.ui.theme.Track1Color
import com.shuttle25.trackanalytics.ui.theme.Track2Color

@Composable
fun ElevationChart(
    track1: Track,
    track2: Track,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    remember(track1, track2) {
        val distances1 = TrackAnalyzer.cumulativeDistances(track1)
        val elevations1 = track1.points.mapNotNull { it.elevation }

        val distances2 = TrackAnalyzer.cumulativeDistances(track2)
        val elevations2 = track2.points.mapNotNull { it.elevation }

        // Sample data to reduce points for smoother rendering
        val sampledData1 = sampleData(distances1, elevations1, 100)
        val sampledData2 = sampleData(distances2, elevations2, 100)

        modelProducer.runTransaction {
            lineSeries {
                series(sampledData1.first, sampledData1.second)
                series(sampledData2.first, sampledData2.second)
            }
        }
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

            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                remember {
                                    LineCartesianLayer.LineFill.single(
                                        com.patrykandpatrick.vico.core.common.Fill(Track1Color.hashCode())
                                    )
                                }
                            ),
                            LineCartesianLayer.rememberLine(
                                remember {
                                    LineCartesianLayer.LineFill.single(
                                        com.patrykandpatrick.vico.core.common.Fill(Track2Color.hashCode())
                                    )
                                }
                            )
                        )
                    ),
                    startAxis = rememberStartAxis(),
                    bottomAxis = rememberBottomAxis()
                ),
                modelProducer = modelProducer,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
        }
    }
}

private fun sampleData(
    distances: List<Double>,
    values: List<Double>,
    maxPoints: Int
): Pair<List<Number>, List<Number>> {
    if (distances.size <= maxPoints || values.size <= maxPoints) {
        val minSize = minOf(distances.size, values.size)
        return distances.take(minSize) to values.take(minSize)
    }

    val step = distances.size / maxPoints
    val sampledDistances = mutableListOf<Number>()
    val sampledValues = mutableListOf<Number>()

    for (i in distances.indices step step) {
        if (i < values.size) {
            sampledDistances.add(distances[i])
            sampledValues.add(values[i])
        }
    }

    return sampledDistances to sampledValues
}
