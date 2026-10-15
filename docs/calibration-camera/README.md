# Checkerboard camera screen (#50)

From the home screen, open **Calibration → Checkerboard camera**. Allow camera permission, then enter the board's inner-corner columns, rows and measured side length of one square in mm. A board with 10 × 7 squares has 9 × 6 inner corners. These inputs describe the board, not the ArUco marker sizes.

Keep the whole board visible and still for each photo. Change its position and tilt between photos. Capture up to 20 photos, optionally using the torch. The counter means photos captured, not successful checkerboard detections. Clear removes the current session's photos and unlocks the board inputs. Leaving a session with photos asks before discarding them. Session state survives activity recreation/rotation.

The photos are original CameraX JPEGs in the app's private cache, not the public gallery or backend. Camera access is released when leaving the screen. No new storage permission is needed. Unfinished cache files may remain after the process is killed and can be removed by Android's cache management.

## Scope and integration

This ticket adds the capture screen and board setup only. It does **not** detect/validate the board, compute intrinsics or distortion, save a calibration profile, or change live measurement calculations. The UI explicitly says that calibration has not been calculated or saved.

The board configuration and temporary photo paths currently belong to the screen session. Adam's #13 detection/calibration flow must consume them before the session is cleared; it should handle JPEG orientation, the selected camera and image resolution, reject unsuitable images, and save a validated profile through #51/#52. There is no success/accuracy claim based on the number of photos alone.

Most work is in `CheckerboardCameraScreen.kt`; the calibration menu button and app route are the only entry-point changes. App navigation remembers the selected screen during activity recreation so the photo session is not lost on rotation. #59 also changes that menu, so its navigation additions may need combining when both branches are merged. This branch starts from main and does not include unmerged measurement PR #63.

## Check it

From `app`:

```powershell
.\gradlew.bat ktlintCheck lintDebug assembleDebug testDebugUnitTest
.\gradlew.bat connectedDebugAndroidTest
```

The camera tests use a real camera and temporary JPEGs, so run them on a phone or suitable emulator. They cover navigation, configuration across recreation, photo creation, cancel/discard, and retaining unrelated existing cache photos. They grant camera permission for that test run.

For manual testing, also try denying permission, returning from Android settings after allowing it, backgrounding/resuming the app, rotating the phone, and switching back to Measurement. Confirm that no calibration values or saved scans are created by capturing a photo.
