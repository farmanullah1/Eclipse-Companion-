# Build and Installation Guide

## Prerequisites
- Android SDK 36 (Minimum API 24 — Android 7.0+)
- JDK 17 or higher
- Gradle 8.13+ (compatible with Kotlin 2.2.10)

## Build Commands
To compile and build debug APK:
```bash
gradle assembleDebug
```

To run all unit tests and regression tests:
```bash
gradle testDebugUnitTest
```

Output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

## Verification
- Completely offline: No internet permission in AndroidManifest.xml.
- Tests verify independent 10-band EQ editing, battery state range checks, and audio peak limiting.
