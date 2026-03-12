# Test Specification

## Test Environment

- Target app: TypeMini
- Application ID: `com.typemini.app`
- Primary phone: `V2157A`
- Phone Android version: 13
- Secondary device: Android Emulator API 35
- Min SDK: 26
- Target SDK: 35
- Preferred build commands:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

## Device Connection Modes

### USB

Use standard USB debugging when available.

Check connection:

```bash
adb devices -l
```

Expected physical device identifiers may include:

- model `V2157A`
- product `PD2157`

### Wi-Fi Debugging

The phone has already been confirmed to work over wireless ADB.

Known reachable address:

- `192.168.50.60:5555`

Check connection:

```bash
adb devices -l
```

Expected wireless device entry:

```text
192.168.50.60:5555
```

### Emulator

Instrumentation tests may also run on an API 35 emulator.

Check connection:

```bash
adb devices -l
```

Expected emulator entry may include:

```text
emulator-5554
```

## USB To Wi-Fi ADB Setup

If USB is available and wireless debugging needs to be re-established:

1. Confirm the phone is visible over USB.
2. Read the phone Wi-Fi IP from `wlan0`.
3. Switch `adbd` to TCP mode.
4. Connect to the device over port `5555`.

Reference commands:

```bash
adb devices -l
adb shell ip -f inet addr show wlan0
adb tcpip 5555
adb connect 192.168.50.60:5555
adb devices -l
```

Successful result should include:

- the USB-connected device
- the wireless device `192.168.50.60:5555`

## Build And Install

### Build debug APK

```bash
./gradlew assembleDebug
```

Expected artifact:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Install to the Wi-Fi phone

Use direct `adb` install when the target must be the physical phone:

```bash
adb -s 192.168.50.60:5555 install -r app/build/outputs/apk/debug/app-debug.apk
```

Successful output should include:

```text
Success
```

### Install with Gradle

When only one target device is connected, install with:

```bash
./gradlew :app:installDebug
```

If multiple devices are connected, prefer the explicit `adb -s` install command above.

## Automated Verification

### Unit tests

Run:

```bash
./gradlew testDebugUnitTest
```

Current unit-test coverage includes:

- typing-session token splitting
- wrong input handling
- space mode and auto mode progression
- backspace state restoration
- uppercase and punctuation support
- course overview resume logic
- unit summary aggregation from article results

Relevant test files:

- `app/src/test/java/com/typemini/app/domain/session/TypingSessionTest.kt`
- `app/src/test/java/com/typemini/app/data/repository/CourseProgressTest.kt`
- `app/src/test/java/com/typemini/app/feature/practice/PracticeViewportTest.kt`

### Instrumentation tests

Run:

```bash
./gradlew connectedDebugAndroidTest
```

Preferred device for instrumentation:

- API 35 emulator

Current instrumentation coverage includes:

- app launches to the home screen
- home screen can open a unit
- unit screen can open the first article

Relevant test file:

- `app/src/androidTest/java/com/typemini/app/AppNavigationTest.kt`

## Manual Regression Flow

Run this flow after any navigation, course-progress, keyboard, or launcher change.

### Home and launcher entry

1. Launch the app from the launcher.
2. Confirm the first screen is the home screen.
3. Confirm visible text includes:
   - `English Practice`
   - `Continue lesson`
   - `History`
4. Re-open the app from the launcher while already deep in the app.
5. Confirm the app returns to the home screen.

### Unit navigation

1. From home, open `Daily Basics`.
2. Confirm the unit screen shows the article list.
3. Press the top-left back action or system back.
4. Confirm navigation returns to home, not to a practice screen.

### Practice navigation

1. From home, open `Daily Basics`.
2. Open `Morning Routine`.
3. Confirm the practice header shows `Daily Basics · Article 1`.
4. Press system back.
5. Confirm navigation returns to the unit screen.

### Practice completion flow

1. Start `Daily Basics · Article 1`.
2. Type through the article until completion.
3. Confirm the result screen appears.
4. Confirm early articles show `Next article`.
5. Continue until the last article in the unit is completed.
6. Confirm the result flow leads to `Unit summary`.
7. Confirm the summary screen shows:
   - completed article count
   - average WPM
   - best WPM
   - accuracy
   - next action for the next unit or home

### Custom keyboard

1. Open any practice article.
2. Confirm the custom keyboard exposes:
   - `SHIFT`
   - `BACK`
   - `123`
   - `ABC` on the symbol layer
   - `'`
   - `,`
   - `.`
3. Confirm `SHIFT` applies one-shot uppercase input.
4. Confirm `BACK` removes the last input and restores the previous typing state.
5. Confirm punctuation keys can be entered without crashing the session.

### History and settings

1. Complete at least one article.
2. Open `History`.
3. Confirm history entries show unit and article context.
4. Open `Settings`.
5. Confirm input mode settings still work and no longer contain the old practice-set selector.

## ADB Smoke Commands

Use these commands for quick verification on a connected device.

Launch the app:

```bash
adb -s 192.168.50.60:5555 shell monkey -p com.typemini.app -c android.intent.category.LAUNCHER 1
```

Send system back:

```bash
adb -s 192.168.50.60:5555 shell input keyevent 4
```

Check that the package is installed:

```bash
adb -s 192.168.50.60:5555 shell pm list packages com.typemini.app
```

Expected package entry:

```text
package:com.typemini.app
```

## Failure Notes

### No connected devices

If install or instrumentation fails with `No connected devices!`:

- verify `adb devices -l`
- reconnect USB if needed
- rerun `adb connect 192.168.50.60:5555`
- confirm the emulator is booted before `connectedDebugAndroidTest`

### Multiple devices connected

If both phone and emulator are connected:

- use `adb -s 192.168.50.60:5555 install -r ...` for phone installs
- use the emulator for `connectedDebugAndroidTest`
- avoid ambiguous `adb` commands without `-s`

### Launcher icon visible but app opens to the wrong screen

Expected behavior is:

- entering from launcher always lands on home

If that regresses, retest:

- launcher open from cold start
- launcher open while already inside a unit
- launcher open while already inside practice

### Keyboard input regressions

If practice can no longer be completed:

- verify punctuation keys still input correctly
- verify `BACK` restores state instead of only clearing visuals
- verify the active article text and custom keyboard character set still match
