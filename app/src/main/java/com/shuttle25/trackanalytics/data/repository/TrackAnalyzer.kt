package com.shuttle25.trackanalytics.data.repository

import com.shuttle25.trackanalytics.data.model.*
import java.time.Duration
import kotlin.math.*

object TrackAnalyzer {

    private const val EARTH_RADIUS_KM = 6371.0
    private const val ELEVATION_THRESHOLD_M = 2.0
    private const val MIN_MOVING_SPEED_KMH = 1.0
    private const val MAX_REALISTIC_SPEED_KMH = 200.0

    fun calculateMetrics(track: Track): TrackMetrics {
        return TrackMetrics(
            trackName = track.name,
            totalDistanceKm = calculateTotalDistance(track),
            pointCount = track.points.size,
            elevation = calculateElevationMetrics(track),
            speed = calculateSpeedMetrics(track)
        )
    }

    fun calculateTotalDistance(track: Track): Double {
        if (track.points.size < 2) return 0.0

        var total = 0.0
        for (i in 1 until track.points.size) {
            total += haversineDistance(track.points[i - 1], track.points[i])
        }
        return total
    }

    fun cumulativeDistances(track: Track): List<Double> {
        val distances = mutableListOf(0.0)
        for (i in 1 until track.points.size) {
            val dist = haversineDistance(track.points[i - 1], track.points[i])
            distances.add(distances.last() + dist)
        }
        return distances
    }

    private fun calculateElevationMetrics(track: Track): ElevationMetrics? {
        if (!track.hasElevation) return null

        val elevations = track.points.mapNotNull { it.elevation }
        if (elevations.isEmpty()) return null

        var totalAscent = 0.0
        var totalDescent = 0.0
        var prevElevation = elevations.first()

        for (elevation in elevations.drop(1)) {
            val diff = elevation - prevElevation
            if (abs(diff) >= ELEVATION_THRESHOLD_M) {
                if (diff > 0) {
                    totalAscent += diff
                } else {
                    totalDescent += abs(diff)
                }
                prevElevation = elevation
            }
        }

        return ElevationMetrics(
            minElevation = elevations.min(),
            maxElevation = elevations.max(),
            totalAscent = totalAscent,
            totalDescent = totalDescent
        )
    }

    private fun calculateSpeedMetrics(track: Track): SpeedMetrics? {
        if (!track.hasTimestamps || track.points.size < 2) return null

        val timedPoints = track.points
            .filter { it.time != null }
            .sortedBy { it.time }

        if (timedPoints.size < 2) return null

        val startTime = timedPoints.first().time!!
        val endTime = timedPoints.last().time!!
        val duration = Duration.between(startTime, endTime)

        if (duration.isZero) return null

        val totalDistance = calculateTotalDistance(track)
        val avgSpeed = (totalDistance / duration.seconds) * 3600

        var maxSpeed = 0.0
        var movingSeconds = 0L
        var movingDistance = 0.0

        for (i in 1 until timedPoints.size) {
            val p1 = timedPoints[i - 1]
            val p2 = timedPoints[i]

            val segmentSeconds = Duration.between(p1.time, p2.time).seconds
            if (segmentSeconds <= 0) continue

            val segmentDist = haversineDistance(p1, p2)
            val segmentSpeed = (segmentDist / segmentSeconds) * 3600

            if (segmentSpeed > MAX_REALISTIC_SPEED_KMH) continue

            maxSpeed = maxOf(maxSpeed, segmentSpeed)

            if (segmentSpeed >= MIN_MOVING_SPEED_KMH) {
                movingSeconds += segmentSeconds
                movingDistance += segmentDist
            }
        }

        val avgMovingSpeed = if (movingSeconds > 0) {
            (movingDistance / movingSeconds) * 3600
        } else 0.0

        return SpeedMetrics(
            duration = duration,
            avgSpeedKmh = avgSpeed,
            maxSpeedKmh = maxSpeed,
            movingTime = Duration.ofSeconds(movingSeconds),
            avgMovingSpeedKmh = avgMovingSpeed
        )
    }

    fun analyzeOverlap(
        track1: Track,
        track2: Track,
        thresholdMeters: Double = 50.0
    ): OverlapResult {
        val thresholdKm = thresholdMeters / 1000.0

        var track1TotalKm = 0.0
        var track1OverlapKm = 0.0

        for (i in 1 until track1.points.size) {
            val p1 = track1.points[i - 1]
            val p2 = track1.points[i]
            val segmentDist = haversineDistance(p1, p2)
            track1TotalKm += segmentDist

            val midPoint = TrackPoint(
                latitude = (p1.latitude + p2.latitude) / 2,
                longitude = (p1.longitude + p2.longitude) / 2,
                elevation = null,
                time = null
            )

            if (findNearestDistance(midPoint, track2) <= thresholdKm) {
                track1OverlapKm += segmentDist
            }
        }

        var track2TotalKm = 0.0
        var track2OverlapKm = 0.0

        for (i in 1 until track2.points.size) {
            val p1 = track2.points[i - 1]
            val p2 = track2.points[i]
            val segmentDist = haversineDistance(p1, p2)
            track2TotalKm += segmentDist

            val midPoint = TrackPoint(
                latitude = (p1.latitude + p2.latitude) / 2,
                longitude = (p1.longitude + p2.longitude) / 2,
                elevation = null,
                time = null
            )

            if (findNearestDistance(midPoint, track1) <= thresholdKm) {
                track2OverlapKm += segmentDist
            }
        }

        val track1OverlapPct = if (track1TotalKm > 0) (track1OverlapKm / track1TotalKm * 100) else 0.0
        val track2OverlapPct = if (track2TotalKm > 0) (track2OverlapKm / track2TotalKm * 100) else 0.0

        return OverlapResult(
            track1OverlapKm = track1OverlapKm,
            track1OverlapPercent = track1OverlapPct,
            track2OverlapKm = track2OverlapKm,
            track2OverlapPercent = track2OverlapPct,
            sharedDistanceKm = (track1OverlapKm + track2OverlapKm) / 2,
            track1UniqueKm = track1TotalKm - track1OverlapKm,
            track2UniqueKm = track2TotalKm - track2OverlapKm
        )
    }

    private fun findNearestDistance(point: TrackPoint, track: Track): Double {
        return track.points.minOf { haversineDistance(point, it) }
    }

    private fun haversineDistance(p1: TrackPoint, p2: TrackPoint): Double {
        val lat1Rad = Math.toRadians(p1.latitude)
        val lat2Rad = Math.toRadians(p2.latitude)
        val deltaLat = Math.toRadians(p2.latitude - p1.latitude)
        val deltaLon = Math.toRadians(p2.longitude - p1.longitude)

        val a = sin(deltaLat / 2).pow(2) +
                cos(lat1Rad) * cos(lat2Rad) * sin(deltaLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }
}
