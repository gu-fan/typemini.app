# TypeMini Android

TypeMini is a native Android typing practice app focused on calm English word training. It keeps the reference site's token-by-token typing flow, but reshapes the product for Android with a light/dark theme, local history, and Compose-first UI.

## Stack

- Kotlin
- Jetpack Compose
- Material 3
- Room
- Navigation Compose
- ViewModel + StateFlow

## Local development

Requirements:

- JDK 17
- Android SDK with `platforms;android-35`
- Android build-tools `35.0.1`

Common commands:

```bash
./gradlew test
./gradlew assembleDebug
./gradlew bundleRelease
./gradlew lint test assembleDebug bundleRelease
```

If you want to run instrumentation tests, start an emulator or connect a device first:

```bash
./gradlew connectedDebugAndroidTest
```

To boot the emulator with a clean start, install the debug build, and launch the app:

```bash
# List available Android Virtual Devices
~/Library/Android/sdk/emulator/emulator -list-avds

# Shut down any running emulator instance
adb emu kill

# Start an emulator without loading a snapshot
~/Library/Android/sdk/emulator/emulator -avd <AVD_NAME> -no-snapshot-load

# Wait for the emulator to come online
adb wait-for-device

# Install the debug build
./gradlew installDebug

# Launch the app
adb shell am start -n com.typemini.app/.MainActivity
```

If the emulator window has rendering issues, try software rendering:

```bash
adb emu kill
~/Library/Android/sdk/emulator/emulator -avd Pixel_8a_API_35 -no-snapshot-load -gpu swiftshader_indirect
adb wait-for-device
./gradlew installDebug
adb shell am start -n com.typemini.app/.MainActivity
```

## Build outputs

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release bundle: `app/build/outputs/bundle/release/app-release.aab`

## CI

GitHub Actions workflow:

- Installs JDK 17 and Android SDK components
- Runs `lint`, `test`, `assembleDebug`, and `bundleRelease`
- Uploads APK and AAB artifacts
