# Known-size live measurement (#57)

## Try it

Open **Measurement → Start measurement** on a physical phone and grant camera permission. Show two different markers together. The control panel displays their centre-to-centre distance in mm; scroll the panel if the phone screen is small. Mixed families are supported:

| Family | Required printed black-square side |
|---|---:|
| 4×4 | 20 mm |
| 5×5 | 50 mm |
| 6×6 | 75 mm |
| 7×7 | 100 mm |
| Original ArUco | 110 mm (project-selected test size) |

Measure the printed black square, including its black border, excluding the surrounding white quiet zone. Printing at another scale gives incorrect distances. Dictionary grid dimensions and physical dimensions are different concepts. There is no reference marker to configure.

The detector supports the four 50-entry grid dictionaries (IDs 0–49) plus `DICT_ARUCO_ORIGINAL` (IDs 0–1023). Physical sizes are defined in `ArucoDictionary`; 110 mm for original ArUco is our project test convention and is not a specification from Mathias. Labels and API requests keep dictionary + ID separate, so the same numeric ID in two dictionaries is not the same marker. AprilTag remains unsupported. Never print duplicate identities in the same view.

The camera overlay, tracking indicators, analysis switch and torch remain available. Distances use current-frame positions and need at least three consecutive detections of both markers. Removing a marker clears its number immediately and leaves a dash in the same row. Turn Analysis off and back on when switching sheets to clear the remembered rows. Tracking percentage means repeat detection over consecutive frames; it does not mean measurement accuracy.

Small 4×4 codes require an exact decoded match. All families require at least two image pixels per cell across the marker's compressed dimension. These guards reduce accidental detections but cannot prove that every accepted marker is real. A distant 20 mm marker may disappear; bring the camera closer rather than lower the guard during accuracy testing.

**Steady display** shows the median of up to five consecutive readings, no older than 500 ms. Switch it off to record raw readings. Missing readings never reuse an old value.

## How the calculation works

1. OpenCV detects the four corners and dictionary family.
2. The physical family size supplies that marker's own square coordinates in mm.
3. `solvePnPGeneric` with `SOLVEPNP_IPPE_SQUARE` estimates its pose in the camera coordinate frame, using the camera matrix and lens-distortion coefficients. The implementation compares the original IPPE candidates, their LM refinements and an independent iterative solution for near-front-on cases, selects the finite solution with all corners in front of the camera and the lowest reprojection error, and hides poses with a poor image fit or similarly fitting candidates with widely different centres. This adapts Simon's useful pose-solving approach from closed PR #37.
4. The displayed tilt is the acute angle between the estimated marker normal and camera optical axis. This rotation is already part of the pose calculation; no separate cosine correction is applied. Tilt also depends on camera calibration.
5. The translation is the marker centre `(x, y, z)` in mm. For two markers, distance is `sqrt((x₂−x₁)² + (y₂−y₁)² + (z₂−z₁)²)`.

Each marker uses its own known size. No marker supplies a shared reference plane, and no 2D screen-pixel distance is treated as a physical distance. Different heights are represented by their 3D positions; they still need physical accuracy testing.

## Camera parameters and present limitations

The current app has no completed checkerboard calibration/persistence workflow. Until #13/#50/#51 supply it, the live view uses a provisional pinhole estimate from the camera CameraX actually selected. It reads a single reported focal length and physical sensor dimensions, assumes a centred aspect-ratio crop, and uses no measured lens distortion. Devices with missing or ambiguous metadata display “Camera parameters unavailable” instead of inventing a scale. Pinch zoom is disabled because zoom would invalidate this estimate.

Every provisional result is labelled **Estimated camera parameters — accuracy not validated**. These results can expose implementation problems, but they are not a calibrated accuracy guarantee. Camera processing, cropping, distortion, autofocus, rolling shutter and planar-pose ambiguity can affect results. Real checkerboard calibration is still needed before establishing a defensible tolerance/working-distance recommendation.

`ArucoAnalyzer.calibratedCameraModel` is the integration point for a future profile. The calibration owner must provide the selected lens's analysis-resolution profile with `calibrated = true`. The analyzer refuses resolution mismatches instead of silently reusing them. Capture, persistence and lens/profile selection are not implemented by this ticket.

Live measurement does not need the backend. Uploading a new scan/spatial graph remains separate work (#49 and the scan-saving flow). The earlier history change displays relation distances already returned by `/api/v1/scans`; saved results are not a substitute for testing a fresh camera measurement.

## Checks

From the `app` directory:

```powershell
.\gradlew.bat ktlintCheck lintDebug assembleDebug testDebugUnitTest
```

Unit tests cover missing/stable rows, raw versus median readings, short smoothing windows, tiny/compressed marker guards, agreed sizes, 3D distances including depth, mixed-family IDs, ambiguous duplicates, invalid positions, calibration resolution and tracking resets. Native OpenCV tests under `androidTest` cover dictionary detection and known-size pairs at 0°, 15°, 35° and 60° for all four families. With one phone or emulator connected:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

The synthetic test is not evidence of physical accuracy. See the [physical measurement results](../../README.md#physical-measurement-results) for the recorded experiments, readings and remaining checks.

Sources: [#57](https://github.com/tedjburgess/cognibotics-aruco-mapper/issues/57), [Simon’s prototype #37](https://github.com/tedjburgess/cognibotics-aruco-mapper/pull/37), [OpenCV PnP reference](https://docs.opencv.org/4.13.0/d5/d1f/calib3d_solvePnP.html), and Mathias's recorded size mapping.

Original ArUco has five data cells plus its one-cell black border on each side (seven cells across). Exact decoding is used for this dictionary. IDs 0–9 follow the current corner-origin convention, while IDs 10 and above use the centre. See [marker roles](../marker-roles.md). A 135 mm same-sheet check is valid for equal-size markers printed in the same orientation with the same origin type; it is not a general reference between separate sheets.

Some codes can be decoded under a second dictionary over the same square: the fixed sample
set revealed that 7x7 ID 44 also produces a coarse 4x4 ID 17 interpretation. When different
dictionaries claim the same four corners, the detector compares the whole rectified black/white
pattern at 144 x 144 pixels against the candidate code. It keeps a candidate only if its mismatch
is at most 15% and at least five percentage points better than the other interpretations;
otherwise that ambiguous square is withheld. Unique, non-conflicting detections are unchanged.
This prevents choosing a physical size just because one dictionary happened to decode first.
Tests cover the alias, the legitimate 4x4 ID 17, and separate markers. Native camera tests and
performance checks still need a connected device; synthetic/PDF checks are not physical accuracy results.
