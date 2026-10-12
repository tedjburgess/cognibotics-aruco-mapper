# Android development

Open this directory in Android Studio and sync Gradle after changing build configuration.

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
