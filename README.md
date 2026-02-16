# UltraFit Tracker (Android Studio Project)

This repository now contains an Android Studio Kotlin app for Samsung Ultra 24 users to track:

- Exercise training programme notes
- Daily step counts
- Water intake per day
- Sleep hours per night
- Daily calories burned (estimated from editable height, weight and steps)

It also integrates with **Health Connect** to read today's step count and sleep sessions.

## Project structure

- `settings.gradle`
- `build.gradle`
- `gradle.properties`
- `app/build.gradle`
- `app/src/main/java/com/drmurali/ultrafit/*`
- `app/src/main/res/*`
- `app/src/main/AndroidManifest.xml`

## Open in Android Studio

1. Open Android Studio.
2. Select **Open** and choose this repository root.
3. Let Gradle sync.
4. Run on an Android device with Health Connect installed.

## Health Connect notes

- On first sync, the app requests read access for:
  - Steps
  - Sleep sessions
- If Health Connect is unavailable, the app shows a status message.
