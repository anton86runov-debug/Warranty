# Warranty Android App

Warranty is an Android application built with Kotlin, Jetpack Compose, Room, WorkManager, and Hilt to help you track warranty coverage for your purchases.

## Features
- Create, update, and delete warranty entries with optional category, store, and price details.
- Automatic calculation of remaining days and status (active, expiring soon, expired).
- Daily background reminders (30/14/7/1 day before expiry) powered by WorkManager notifications.
- Compose UI with search, filtering, and per-item reminder toggles.
- JSON export/import with ISO8601 dates written to the Downloads collection.
- Room database with type-safe migrations covered by unit tests.

## Getting Started
1. **Requirements**
   - Android Studio Ladybug (or newer) with JDK 17+
   - Android SDK Platform 34
2. **Clone & open**
   `ash
   git clone <repo-url>
   cd Warranty
   `
   Open the project in Android Studio or use the Gradle wrapper from the terminal.

## Build, Test, Lint
All Gradle commands are available via the wrapper:
- Build debug APK: ./gradlew assembleDebug
- Run JVM unit tests: ./gradlew testDebugUnitTest
- Run ktlint: ./gradlew ktlintCheck
- Combined CI task: ./gradlew ci

## Architecture
- **Presentation**: Jetpack Compose + MVVM view models scoped with Hilt.
- **Domain**: Use-cases encapsulating business rules, including warranty status computation and backup flows.
- **Data**: Room database with converters, repository implementation, and JSON backup helper.
- **Background**: WorkManager periodic worker scheduled via Hilt-injected scheduler.

## Export & Import
- Export triggers a JSON dump of all warranties into the public Downloads folder (or legacy path on API < 29).
- Import uses the system document picker, supports merge or replace modes, and re-schedules reminders afterwards.

## Notifications
The app creates a "Warranty reminders" channel. On Android 13+ the app requests the POST_NOTIFICATIONS permission at first launch. Reminders fire daily and notify for items due in 30/14/7/1 day, respecting the per-item reminder toggle.

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md).
