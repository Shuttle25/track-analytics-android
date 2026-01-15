package com.shuttle25.trackanalytics.data.repository

import com.shuttle25.trackanalytics.data.model.Track

/**
 * Singleton to persist tracks across activity recreation.
 * Survives as long as the app process is alive.
 */
object TrackStore {
    var track1: Track? = null
    var track2: Track? = null

    fun clear() {
        track1 = null
        track2 = null
    }
}
