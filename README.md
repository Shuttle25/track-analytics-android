# Track Analytics Android

Android app for comparing OsmAnd GPX tracks.

## Features

- **Compare two GPX tracks** side-by-side
- **Distance & elevation metrics** — total distance, elevation gain/loss
- **Speed & timing** — duration, average/max speed, moving time
- **Route overlap analysis** — find shared and unique segments
- **Elevation profile charts**
- **Share from OsmAnd** — receive GPX files via share intent
- **File picker** — select GPX files from device storage

## Screenshots

The app displays:
- Track selection buttons
- Metrics comparison cards (distance, elevation, speed)
- Visual progress bars for route overlap
- Elevation profile chart

## Building

Open the project in Android Studio and build, or use command line:

```bash
./gradlew assembleDebug
```

APK will be in `app/build/outputs/apk/debug/`

## Requirements

- Android 8.0 (API 26) or higher
- Android Studio Hedgehog or newer (for development)

## Usage

1. Open the app
2. Tap "Track 1" to select first GPX file
3. Tap "Track 2" to select second GPX file
4. Tap "Compare" to analyze

Or share a GPX file from OsmAnd directly to Track Analytics.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- Vico (charts)
- XmlPullParser (GPX parsing)

## License

MIT
