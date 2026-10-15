# Cognibotics ArUco Mapper

## Overview

This project is an Android application prototype built for Cognibotics, located in Lund, Sweden. The core app functionality is the use of a phone camera to detect ArUco markers and calculate the distance between them. Measurement data is stored in a database through the application's API, allowing it to be retrieved and used by other parts of the application or external systems. The data is intended to be used by robots in industrial automation.

## Supported ArUco Dictionaries

Physical sizes can be adjusted within settings.

| Dictionary | Physical size | Description |
|---|---:|---|
| `DICT_4X4_50` | 20 mm | 4×4 marker grid with 50 available marker IDs |
| `DICT_5X5_50` | 50 mm | 5×5 marker grid with 50 available marker IDs |
| `DICT_6X6_50` | 75 mm | 6×6 marker grid with 50 available marker IDs |
| `DICT_7X7_50` | 100 mm | 7×7 marker grid with 50 available marker IDs |

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
- Distance accuracy depends on using the correct physical marker size (can be adjusted within settings)
- Reliability and accuracy depends on environmental constraints such as lighting, motion, or partially covered ArUco markers
- The camera requires a clear view of the markers being measured

## API