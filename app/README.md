# Android development

Open this directory in Android Studio and sync Gradle after changing build configuration.

Set the API URL in `local.properties`: copy `local.properties.example` to `local.properties` and adjust `apiBaseUrl` for your connection. Use `http://127.0.0.1:8000/` with USB plus `adb reverse tcp:8000 tcp:8000`; use `http://10.0.2.2:8000/` with the Android emulator. For a phone over Wi-Fi, use your computer's LAN IP. `local.properties` is ignored by Git, so each developer can use their own URL.

Run checks from this directory:

```powershell
.\gradlew.bat ktlintCheck lintDebug
```

Automatically format Kotlin sources and Gradle Kotlin scripts:

```powershell
.\gradlew.bat ktlintFormat
```

On macOS/Linux, use `./gradlew` instead of `.\gradlew.bat`.

Formatting follows `app/.editorconfig` with the Android Studio code style and allows Compose function names. GitHub CI checks formatting and Android lint alongside the existing backend checks. CI never applies formatting fixes; run the formatter locally and review its changes before committing.

Build the debug app and run local unit tests (no emulator required):

```powershell
.\gradlew.bat assembleDebug testDebugUnitTest
```

A passing run ends with `BUILD SUCCESSFUL`. If `ktlintCheck` fails, run `ktlintFormat`, review the changes, and run the checks again. Some violations, such as wildcard imports, require a manual fix.

Use Android Studio's terminal with a supported JDK available (`JAVA_HOME` or `PATH`). Instrumented tests require a device or emulator and are separate from these local checks.

## Live measurement and physical tests

See the [measurement guide](../docs/measurement/README.md) for supported marker sizes and controls, and the [accuracy guide](../docs/accuracy/README.md#recorded-run-conditions) for the three physical run setups and results. The manual reference distances have estimated ±3 mm uncertainty; camera parameters are still estimated.
