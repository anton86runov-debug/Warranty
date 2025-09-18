# Agent Handbook

## Build
- ./gradlew assembleDebug

## Test
- ./gradlew testDebugUnitTest

## Lint & Static Checks
- ./gradlew ktlintCheck
- ./gradlew ci (runs ktlint, unit tests, and assembleDebug)

## Notes
- WorkManager reminders require the app to be installed; use db shell cmd jobscheduler run -f com.warranty.app 0 to trigger manually if needed.
- Import/export operations rely on the system document picker; during automated tests provide mock URIs.
