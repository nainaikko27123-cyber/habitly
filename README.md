# Habitly ✅

An Android app for building better habits, with a habit tracker, calendar, stopwatch, notes and alarms in one place.

## Features
- **Habits:** create habits and check them off every day
- **Calendar:** see your progress day by day
- **Stopwatch** with laps
- **Notes:** quick-add notes and checklists, with tags
- **Alarms & reminders:** get reminded to keep your streak
- **Dark mode**

## Built with
Kotlin · Jetpack Compose (Material 3) · MVVM · Room database · Navigation Compose · AlarmManager

## Project structure
```
app/src/main/java/.../
├── data/        Room database, entities and repositories
├── viewmodel/   screen state and logic
├── ui/          Compose screens and components
├── reminders/   alarms and notifications
└── util/
```

## Run it
Open the project in Android Studio and press **Run**. The minimum SDK is set in `app/build.gradle.kts`.

## License
MIT. Free to use, change and share. See [LICENSE](LICENSE).
