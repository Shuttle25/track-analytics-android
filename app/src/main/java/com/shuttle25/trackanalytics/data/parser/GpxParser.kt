package com.shuttle25.trackanalytics.data.parser

import android.content.Context
import android.net.Uri
import com.shuttle25.trackanalytics.data.model.Track
import com.shuttle25.trackanalytics.data.model.TrackPoint
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object GpxParser {

    private val dateTimeFormatters = listOf(
        DateTimeFormatter.ISO_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
    )

    fun parse(context: Context, uri: Uri): Track {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open file: $uri")

        return inputStream.use { parse(it, getFileName(context, uri)) }
    }

    fun parse(inputStream: InputStream, name: String = "Track"): Track {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = false
        val parser = factory.newPullParser()
        parser.setInput(inputStream, "UTF-8")

        val points = mutableListOf<TrackPoint>()
        var trackName = name

        var eventType = parser.eventType
        var currentLat: Double? = null
        var currentLon: Double? = null
        var currentEle: Double? = null
        var currentTime: LocalDateTime? = null
        var inTrackPoint = false
        var inRoutePoint = false

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "trkpt", "rtept" -> {
                            inTrackPoint = parser.name == "trkpt"
                            inRoutePoint = parser.name == "rtept"
                            currentLat = parser.getAttributeValue(null, "lat")?.toDoubleOrNull()
                            currentLon = parser.getAttributeValue(null, "lon")?.toDoubleOrNull()
                            currentEle = null
                            currentTime = null
                        }
                        "ele" -> {
                            if (inTrackPoint || inRoutePoint) {
                                currentEle = parser.nextText().toDoubleOrNull()
                            }
                        }
                        "time" -> {
                            if (inTrackPoint || inRoutePoint) {
                                currentTime = parseDateTime(parser.nextText())
                            }
                        }
                        "name" -> {
                            if (!inTrackPoint && !inRoutePoint) {
                                val possibleName = parser.nextText()
                                if (possibleName.isNotBlank()) {
                                    trackName = possibleName
                                }
                            }
                        }
                    }
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "trkpt", "rtept" -> {
                            if (currentLat != null && currentLon != null) {
                                points.add(
                                    TrackPoint(
                                        latitude = currentLat!!,
                                        longitude = currentLon!!,
                                        elevation = currentEle,
                                        time = currentTime
                                    )
                                )
                            }
                            inTrackPoint = false
                            inRoutePoint = false
                        }
                    }
                }
            }
            eventType = parser.next()
        }

        if (points.isEmpty()) {
            throw IllegalArgumentException("No track points found in GPX file")
        }

        return Track(name = trackName, points = points)
    }

    private fun parseDateTime(text: String): LocalDateTime? {
        for (formatter in dateTimeFormatters) {
            try {
                return LocalDateTime.parse(text.trim(), formatter)
            } catch (_: DateTimeParseException) {
                // Try next formatter
            }
        }
        return null
    }

    private fun getFileName(context: Context, uri: Uri): String {
        var name = "Track"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex("_display_name")
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)?.removeSuffix(".gpx") ?: name
            }
        }
        return name
    }
}
