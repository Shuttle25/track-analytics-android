package com.shuttle25.trackanalytics

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.shuttle25.trackanalytics.data.model.ComparisonResult
import com.shuttle25.trackanalytics.data.model.Track
import com.shuttle25.trackanalytics.data.parser.GpxParser
import com.shuttle25.trackanalytics.data.repository.TrackAnalyzer
import com.shuttle25.trackanalytics.data.repository.TrackStore
import com.shuttle25.trackanalytics.ui.screens.ComparisonScreen
import com.shuttle25.trackanalytics.ui.theme.TrackAnalyticsTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TrackAnalyticsTheme {
                var track1 by remember { mutableStateOf(TrackStore.track1) }
                var track2 by remember { mutableStateOf(TrackStore.track2) }
                var comparisonResult by remember { mutableStateOf<ComparisonResult?>(null) }
                var isLoading by remember { mutableStateOf(false) }
                var selectingTrack by remember { mutableIntStateOf(0) }

                // For intent dialog
                var pendingUri by remember { mutableStateOf<Uri?>(null) }
                var showTrackDialog by remember { mutableStateOf(false) }

                // Sync with TrackStore
                LaunchedEffect(track1) { TrackStore.track1 = track1 }
                LaunchedEffect(track2) { TrackStore.track2 = track2 }

                val filePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri ->
                    uri?.let { selectedUri ->
                        loadTrack(selectedUri) { track ->
                            when (selectingTrack) {
                                1 -> {
                                    track1 = track
                                    comparisonResult = null
                                }
                                2 -> {
                                    track2 = track
                                    comparisonResult = null
                                }
                            }
                        }
                    }
                }

                // Handle incoming intent (share from OsmAnd)
                LaunchedEffect(Unit) {
                    getUriFromIntent(intent)?.let { uri ->
                        pendingUri = uri
                        showTrackDialog = true
                    }
                }

                // Track selection dialog
                if (showTrackDialog && pendingUri != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showTrackDialog = false
                            pendingUri = null
                        },
                        title = { Text("Load as") },
                        text = { Text("Select which track slot to use") },
                        confirmButton = {
                            TextButton(onClick = {
                                pendingUri?.let { uri ->
                                    loadTrack(uri) { track ->
                                        track1 = track
                                        comparisonResult = null
                                    }
                                }
                                showTrackDialog = false
                                pendingUri = null
                            }) {
                                Text("Track 1")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                pendingUri?.let { uri ->
                                    loadTrack(uri) { track ->
                                        track2 = track
                                        comparisonResult = null
                                    }
                                }
                                showTrackDialog = false
                                pendingUri = null
                            }) {
                                Text("Track 2")
                            }
                        }
                    )
                }

                ComparisonScreen(
                    track1Name = track1?.name,
                    track2Name = track2?.name,
                    comparisonResult = comparisonResult,
                    isLoading = isLoading,
                    onSelectTrack1 = {
                        selectingTrack = 1
                        filePickerLauncher.launch(arrayOf("*/*"))
                    },
                    onSelectTrack2 = {
                        selectingTrack = 2
                        filePickerLauncher.launch(arrayOf("*/*"))
                    },
                    onCompare = {
                        val t1 = track1
                        val t2 = track2
                        if (t1 != null && t2 != null) {
                            isLoading = true
                            lifecycleScope.launch {
                                comparisonResult = compareTracksAsync(t1, t2)
                                isLoading = false
                            }
                        }
                    },
                    canCompare = track1 != null && track2 != null
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        recreate()
    }

    private fun getUriFromIntent(intent: Intent?): Uri? {
        return when (intent?.action) {
            Intent.ACTION_SEND -> intent.getParcelableExtra(Intent.EXTRA_STREAM)
            Intent.ACTION_VIEW -> intent.data
            else -> null
        }
    }

    private fun loadTrack(uri: Uri, onLoaded: (Track) -> Unit) {
        lifecycleScope.launch {
            try {
                val track = withContext(Dispatchers.IO) {
                    GpxParser.parse(this@MainActivity, uri)
                }
                onLoaded(track)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error loading GPX: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private suspend fun compareTracksAsync(track1: Track, track2: Track): ComparisonResult {
        return withContext(Dispatchers.Default) {
            val metrics1 = TrackAnalyzer.calculateMetrics(track1)
            val metrics2 = TrackAnalyzer.calculateMetrics(track2)
            val overlap = TrackAnalyzer.analyzeOverlap(track1, track2)

            ComparisonResult(
                track1 = track1,
                track2 = track2,
                metrics1 = metrics1,
                metrics2 = metrics2,
                overlap = overlap
            )
        }
    }
}
