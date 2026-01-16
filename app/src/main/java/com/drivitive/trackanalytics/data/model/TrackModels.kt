package com.drivitive.trackanalytics.data.model

import java.time.Duration
import java.time.LocalDateTime

data class TrackPoint(
    val latitude: Double,
    val longitude: Double,
    val elevation: Double?,
    val time: LocalDateTime?
)

data class Track(
    val name: String,
    val points: List<TrackPoint>
) {
    val hasElevation: Boolean
        get() = points.any { it.elevation != null }

    val hasTimestamps: Boolean
        get() = points.any { it.time != null }
}

data class ElevationMetrics(
    val minElevation: Double,
    val maxElevation: Double,
    val totalAscent: Double,
    val totalDescent: Double
) {
    val elevationRange: Double
        get() = maxElevation - minElevation
}

data class SpeedMetrics(
    val duration: Duration,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val movingTime: Duration,
    val avgMovingSpeedKmh: Double
)

data class TrackMetrics(
    val trackName: String,
    val totalDistanceKm: Double,
    val pointCount: Int,
    val elevation: ElevationMetrics?,
    val speed: SpeedMetrics?
)

data class OverlapResult(
    val track1OverlapKm: Double,
    val track1OverlapPercent: Double,
    val track2OverlapKm: Double,
    val track2OverlapPercent: Double,
    val sharedDistanceKm: Double,
    val track1UniqueKm: Double,
    val track2UniqueKm: Double
)

data class ComparisonResult(
    val track1: Track,
    val track2: Track,
    val metrics1: TrackMetrics,
    val metrics2: TrackMetrics,
    val overlap: OverlapResult
)
