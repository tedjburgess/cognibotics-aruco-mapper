# Cognibotics ArUco Mapper

## Overview

This project is an Android application prototype built for Cognibotics, located in Lund, Sweden. The core app functionality is the use of a phone camera to detect ArUco markers and calculate the distance between them. The API can store and return scan data, and the app can display returned scans in measurement history. Saving new live measurements from the app is not connected yet. The data is intended to be used by robots in industrial automation.

## Supported ArUco Dictionaries

Physical sizes are currently fixed in code, not editable in settings. Print the black squares at the sizes below.

| Dictionary | Physical size | Description |
|---|---:|---|
| `DICT_4X4_1000` | 20 mm | 4×4 marker grid with 1000 available marker IDs |
| `DICT_5X5_1000` | 50 mm | 5×5 marker grid with 1000 available marker IDs |
| `DICT_6X6_1000` | 75 mm | 6×6 marker grid with 1000 available marker IDs |
| `DICT_7X7_1000` | 100 mm | 7×7 marker grid with 1000 available marker IDs |

The detector uses the 1000-entry variants for mixed-family live measurement; compatible codes from the smaller predefined variants can also be detected. The dictionary enum retains the 50-entry variants for explicit selection in code. **Test sheets** mode starts enabled and accepts IDs 0–3 only; switch it off for other IDs.

## Application Usage

### Measurement

To conduct a measurement the user should do the following:

- Select **Measurement** from the home screen
- Select **Start Measurement** from the measurement screen
- Grant camera permission if needed
- Point camera at the supported ArUco markers
- View the detected markers and marker information on the camera screen

### Measurement History

**Measurement History** is located within the **Measurement** screen. The history displays a list of previously saved measurements. Each measurement entry shows the following:

- **site name**
- **cell name**
- **date/time** when the measurement was taken
- **number** of detected markers

### Calibration
### Export

## Requirements
### Camera Permission

The app requires camera permission in order to detect ArUco markers. The app will request permission when a user attempts to start a measurement. If permission is not granted the app will not be able to detect ArUco markers. The rear camera is used for detection.

## Current Restrictions and Limitations

- Only the ArUco dictionaries listed above are supported
- Distance accuracy depends on printing the correct fixed physical marker size; sizes cannot currently be changed in settings
- Reliability and accuracy depends on environmental constraints such as lighting, motion, or partially covered ArUco markers
- The camera requires a clear view of the markers being measured

For setup, controls and limitations, see the [measurement guide](docs/measurement/README.md). The [accuracy report](docs/accuracy/results-2026-10-14.md) documents all three physical runs and the estimated ±3 mm reference uncertainty. Camera parameters remain estimated, not checkerboard-calibrated.

## API
