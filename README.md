# Cognibotics ArUco Mapper

## Overview

This Android application detects ArUco markers, calculates distances between them, and saves confirmed measurements through the Python API to SQLite. The app can load saved sites and cells, and displays saved measurements in its history. The data is intended to be used by robots in industrial automation.

## Supported ArUco Dictionaries

Physical sizes are currently fixed in code, not editable in settings. Print the black squares at the sizes below.

| Dictionary | Physical size | Description |
|---|---:|---|
| `DICT_4X4_50` | 20 mm | 4×4 marker grid with 50 available marker IDs |
| `DICT_5X5_50` | 50 mm | 5×5 marker grid with 50 available marker IDs |
| `DICT_6X6_50` | 75 mm | 6×6 marker grid with 50 available marker IDs |
| `DICT_7X7_50` | 100 mm | 7×7 marker grid with 50 available marker IDs |
| `DICT_ARUCO_ORIGINAL` | 110 mm | Original ArUco dictionary with 1024 available marker IDs; project-selected test size |

The four grid families use IDs 0–49; original ArUco uses IDs 0–1023. Marker identity includes both dictionary and ID. The 110 mm original-marker size is a project test convention, not a size specified by Mathias or the dictionary format. Existing physical test results below use the original four families and are unchanged.

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
- marker-pair distances with marker ID and dictionary

### Calibration

See the [checkerboard capture guide](docs/calibration-camera/README.md). Capturing photos alone does not calculate or save camera calibration.

## Requirements
### Camera Permission

The app requires camera permission in order to detect ArUco markers. The app will request permission when a user attempts to start a measurement. If permission is not granted the app will not be able to detect ArUco markers. The rear camera is used for detection.

## Current Restrictions and Limitations

- Only the ArUco dictionaries listed above are supported
- Distance accuracy depends on printing the correct fixed physical marker size; sizes cannot currently be changed in settings
- Reliability and accuracy depends on environmental constraints such as lighting, motion, or partially covered ArUco markers
- The camera requires a clear view of the markers being measured

## Physical measurement results

Tests compare the app with physical centre-to-centre distances. Camera parameters were estimated, not checkerboard-calibrated. For controls, marker sizes and calculation details, see the [measurement guide](docs/measurement/README.md).

- **MAE:** average absolute error, `mean(abs(app − reference))`. Error percentage is `100 × abs(app − reference) / reference`, averaged over the readings.
- Smaller average error does not guarantee a tolerance. Missing readings are not zeros; green tracking status does not prove accuracy.
- Phones and setups are kept separate. Consecutive video samples are correlated, and these are selected sections rather than averages over every video frame.

### S25: distance and torch

Same-sheet marker IDs 0 and 1; nominal 135 mm spacing; smoothing off. Camera distances were tester-reported except the two rows marked †, which were assigned from the conversation sequence. Views were intended to be front-facing but their angles were not independently recorded.

| Marker side | Camera distance | Torch on: MAE | Torch off: MAE |
|---|---:|---:|---:|
| 20 mm | 50 cm | 3.37 mm (2.49%) | 2.49 mm (1.84%) |
| 20 mm | 67 cm | 4.14 mm (3.07%) | 3.56 mm (2.64%) |
| 50 mm | 50 cm | 0.99 mm (0.73%) | 0.95 mm (0.71%) |
| 50 mm | 75 cm | 2.03 mm (1.50%) | 1.64 mm (1.21%) |
| 50 mm | 100 cm | 2.27 mm (1.68%) | 2.27 mm (1.68%) |
| 75 mm | 50 cm | 1.04 mm (0.77%) | 1.02 mm (0.75%) |
| 75 mm | 75 cm † | 1.21 mm (0.90%) | 1.54 mm (1.14%) |
| 75 mm | 100 cm | 1.08 mm (0.80%) | 1.68 mm (1.24%) |
| 100 mm | 50 cm | 0.68 mm (0.50%) | 0.49 mm (0.36%) |
| 100 mm | 75 cm | 0.74 mm (0.55%) | 0.71 mm (0.53%) |
| 100 mm | 100 cm † | 1.58 mm (1.17%) | 1.04 mm (0.77%) |

The 20 mm pair was reported undetectable at 75 and 100 cm. Torch settings and video timestamps were not supplied for those failed attempts. At 50 and 67 cm it produced intermittent readings. Torch effects varied; movement, angle and focus were not fully controlled.

### S20: distance

Same-sheet marker IDs 2 and 3; nominal 135 mm spacing. **Distance labels are inferred from sequence and relative marker scale and need confirmation.** Torch changes within some sections, so this is not a controlled on/off comparison.

| Marker side | 50 cm: MAE | 75 cm: MAE | 100 cm: MAE |
|---|---:|---:|---:|
| 100 mm | 0.54 mm (0.40%) | 1.08 mm (0.80%) | 1.61 mm (1.19%) |
| 75 mm | 0.92 mm (0.68%) | 1.67 mm (1.24%) | 2.22 mm (1.64%) |
| 50 mm | 1.49 mm (1.10%) | 2.36 mm (1.75%) | 3.23 mm (2.39%) |
| 20 mm | 3.29 mm (2.44%) | Not sampled | Not sampled |

20 mm markers at inferred 67 cm: **5.05 mm (3.74%)**. No S20 video samples at 75/100 cm are represented as observed failures.

### S20: angle at reported 50 cm

The tester confirmed the nominal order 0°, 15°, 30°, 45°, 60°. Section boundaries were inferred from position changes; the app's displayed tilt is not an independent angle measurement. Smoothing was off. Torch settings varied.

| Marker side | 0° MAE (mm) | 15° | 30° | 45° | 60° |
|---|---:|---:|---:|---:|---:|
| 100 mm | 0.38 | 0.33 | 0.44 | 0.46 | 0.41 |
| 75 mm | 0.74 | 0.50 | 0.38 | 0.45 | 0.71 |
| 50 mm | 0.93 | 0.95 | 0.57 | 0.68 | 0.78 |
| 20 mm | 3.31 | 6.93 | 7.46 | 7.77 | No reading |

The 20 mm pair produced no distance in all ten selected 60° samples. Larger markers retained readings at the tested angles, but outliers and missing values remain.

### Original three S25 runs

Each run used the same 12 cross-sheet pairs, compared with that run's own reference distances. Runs 1 and 2 also include three same-sheet readings each. Marker sizes were 50, 75 and 100 mm, IDs 0 and 1.

| Run | Reported setup | Cross-sheet readings | MAE | Mean percentage error |
|---|---|---:|---:|---:|
| 1 | Fixed view, about 0–5°; 43 mm height difference | 12 | 1.96 mm | 0.81% |
| 2 | Fixed view, about 30°; 43 mm height difference | 12 | 3.89 mm | 1.60% |
| 3 | Moving through angles; same elevation | 12 | 2.38 mm | 0.89% |
| Combined | Matching 12 pairs per run | 36 | **2.74 mm** | **1.10%** |

Including the six extra same-sheet readings gives 42 readings, MAE 2.50 mm and mean percentage error 1.05%; this weights the first two runs more heavily. The headline comparison uses the matching 36 readings. The largest absolute difference was 7.26 mm.

Manual cross-sheet references have tester-estimated **±3 mm uncertainty** for locating centres and sheet movement. This is not a verified bound or confidence interval and has not been subtracted from errors. It does not establish the print uncertainty of the newer 135 mm sheets. Smoothing appears enabled in runs 1–2 and is off in the selected run 3 section, so these are not identical-condition repeat trials.

### Earlier S25 angle observations

References are unchanged from original run 3. Smoothing was off; angles were approximate and camera distance varied. Partial rows are not directly comparable with complete rows.

| Angle | Cross-sheet pairs visible | Cross-sheet MAE | Mean percentage error | Same-sheet 100 mm pair reading / error |
|---|---:|---:|---:|---|
| 0° | 12/12 | 3.04 mm | 1.13% | 135.6 / 0.6 mm |
| 15° | 12/12 | 2.58 mm | 1.06% | 135.4 / 0.4 mm |
| 30° | 4/12 (partial) | 6.05 mm | 2.64% | 135.2 / 0.2 mm |
| 45° | 12/12 | 2.68 mm | 1.08% | 136.0 / 1.0 mm |
| 60° | 4/12 (partial) | 7.10 mm | 2.95% | 133.5 / 1.5 mm |

### Findings and remaining checks

- Larger markers generally performed better in the sampled sections. Distance errors tended to rise farther from the markers; the 20 mm pair struggled with distance and steep angles.
- These results do not establish a universal maximum working distance or an agreed tolerance. Print sizes/spacing, lens/resolution, exact app revision and independent angle-setting method remain unverified for the newer recordings.
- Do not attribute differences solely to the phone: marker IDs, setup and lighting differed. Same-sheet tests do not validate arbitrary cross-sheet distances, different heights or robot coordinate frames.
- Displaced outlines and a black preview after landscape rotation were observed separately. Their cause is not established by these videos. Tracking percentage describes repeat detection, not accuracy.
- Before firm recommendations: confirm distance labels and print dimensions, repeat selected setups independently, then repeat after full calibration. At Cognibotics, check workplace lighting, mixed sizes and different heights.

<details>
<summary>Detailed S25 distance/torch readings (310 sample records)</summary>

One reading per second across each inclusive time range below. `M` = row visible but no distance; `H` = row hidden by scrolling. Neither is used as zero. Readings are mm against 135 mm. Missing/hidden counts apply only to these sections, not whole-video detection rates.

Clips A–K follow the supplied S25 recording order: A 100 mm/50 cm; B 100 mm/75 cm; C 100 mm/100 cm; D 75 mm/100 cm; E 75 mm/75 cm; F 75 mm/50 cm; G 50 mm/50 cm; H 50 mm/75 cm; I 50 mm/100 cm; J 20 mm/67 cm; K 20 mm/50 cm. Dates and local file paths are omitted.

| Clip / marker / camera distance | Torch | Video seconds | Readings in order | Missing / hidden |
|---|---|---|---|---|
| A / 100 mm / 50 cm | on | 34–44 | 135.4, 135.6, M, 136.4, 136.1, H, 135.8, 135.4, 135.4, 135.5, 135.5 | 1 / 1 |
| A / 100 mm / 50 cm | off | 48–59 | 135.6, 135.5, 135.5, 135.5, 135.8, 135.6, 135.4, 135.5, 135.5, 135.2, 135.4, 135.4 | 0 / 0 |
| B / 100 mm / 75 cm | on | 1–17 | 135.8, 135.7, 135.0, 135.9, 136.0, 135.7, 135.9, 135.9, 135.7, 135.7, 135.9, 136.1, 135.7, 135.6, 135.7, 135.6, 135.7 | 0 / 0 |
| B / 100 mm / 75 cm | off | 21–31 | 135.9, 135.7, 135.6, 135.3, 135.6, 135.6, 135.7, 135.7, 136.0, 135.9, 135.8 | 0 / 0 |
| C / 100 mm / 100 cm | on | 9–21 | 136.2, 137.1, 135.9, 136.4, 137.4, 137.3, 136.7, 136.6, 136.4, 136.8, 136.2, 135.7, 136.8 | 0 / 0 |
| C / 100 mm / 100 cm | off | 24–39 | 135.1, 136.1, 136.3, 135.6, 135.0, 136.3, 135.9, 136.4, 136.8, 136.0, 136.6, 136.4, 136.3, 135.3, 136.0, 136.5 | 0 / 0 |
| D / 75 mm / 100 cm | on | 5–24 | 135.3, 135.5, 136.4, 136.5, 136.4, 134.8, 136.2, 135.2, 136.2, 134.9, 136.4, 136.2, 136.4, 136.8, 135.8, 136.1, 135.9, 136.6, 137.0, 136.4 | 0 / 0 |
| D / 75 mm / 100 cm | off | 26–41 | 136.6, 136.3, 136.6, 136.6, 137.4, M, 137.1, 137.4, 136.6, M, M, 135.8, 136.3, 136.1, 136.2, 137.8 | 3 / 0 |
| E / 75 mm / 75 cm | on | 4–18 | 136.5, 135.8, 136.6, 136.0, 136.0, 136.1, 136.1, 136.6, 136.3, 135.5, 136.6, 135.4, 136.8, 136.2, 136.7 | 0 / 0 |
| E / 75 mm / 75 cm | off | 21–34 | 136.3, 137.1, 137.2, 136.6, 137.3, 136.9, 137.0, 136.3, 136.0, 136.4, 136.5, 136.1, 135.6, 136.3 | 0 / 0 |
| F / 75 mm / 50 cm | on | 1–15 | 136.0, 136.1, 136.5, 136.1, 136.1, 136.1, 135.9, 135.9, 135.9, 136.0, 136.0, 135.9, 136.1, 136.0, 136.0 | 0 / 0 |
| F / 75 mm / 50 cm | off | 19–31 | 136.0, 136.1, 136.3, 135.8, 135.9, 135.9, 135.8, 135.9, 136.0, 135.9, 136.2, 135.8, 136.6 | 0 / 0 |
| G / 50 mm / 50 cm | on | 4–18 | 135.6, 136.1, 135.9, 135.9, 136.0, 136.2, 136.2, 136.1, 136.0, 135.9, 136.1, 135.9, 136.0, 136.2, 135.7 | 0 / 0 |
| G / 50 mm / 50 cm | off | 21–33 | 135.7, 135.5, 135.9, 136.3, 136.2, 136.2, 135.9, 136.1, 135.7, 136.3, 135.7, 136.1, 135.8 | 0 / 0 |
| H / 50 mm / 75 cm | on | 4–18 | 136.9, 137.2, 137.5, 136.5, 138.0, 135.7, 137.2, 137.2, 137.2, 137.0, 136.1, 137.4, 137.8, 137.2, 136.5 | 0 / 0 |
| H / 50 mm / 75 cm | off | 23–36 | 135.3, 137.4, 136.7, 136.8, 137.1, 135.7, 136.8, 136.4, 137.4, 137.6, 136.6, 136.2, 135.9, 137.0 | 0 / 0 |
| I / 50 mm / 100 cm | on | 4–17 | 136.2, 136.2, 137.8, 137.0, 136.6, 138.3, 136.4, 136.7, 139.1, 137.9, 137.5, M, 137.9, 136.9 | 1 / 0 |
| I / 50 mm / 100 cm | off | 19–31 | 136.9, 136.7, 137.8, 137.6, 137.9, 137.2, 136.3, 138.0, 138.0, M, 137.4, 136.9, 136.5 | 1 / 0 |
| J / 20 mm / 67 cm | on | 2–16 | 140.0, 139.2, 139.2, 139.9, 138.7, M, 140.7, 139.0, 138.8, 139.0, 138.4, 139.6, M, 139.0, 137.3 | 2 / 0 |
| J / 20 mm / 67 cm | off | 18–28 | 137.6, 138.5, 138.6, 139.9, M, 138.6, 139.3, 137.5, 138.1, 139.0, 138.5 | 1 / 0 |
| K / 20 mm / 50 cm | on | 2–14 | 138.9, M, M, 138.8, 139.5, M, M, 138.0, 136.6, 137.9, 139.0, 138.7, 137.9 | 4 / 0 |
| K / 20 mm / 50 cm | off | 16–29 | M, M, 138.2, M, 137.5, 136.2, 136.7, 137.4, 137.7, 138.6, M, M, 138.4, 136.7 | 5 / 0 |

Setup and torch-switching sections were excluded from the numerical summaries. Early dropouts and extra family IDs in the 100 mm/50 cm recording remain qualitative observations; they were not exhaustively counted. The selected 100 mm/50 cm torch-on section has nine readings, one missing and one hidden sample.

</details>

<details>
<summary>Detailed S20 readings and availability (310 sample records)</summary>

One sample per second in each selected 10-second section. IDs 2 and 3; smoothing off. `M` and `H` have the same meaning as above. Torch varied or was not visible; no controlled torch effect is inferred. These samples are not independent repeated trials.

Clip A: initial 75 mm distance recording. Clip B: remaining distance tests. Clip C: supplementary 20 mm recording with smoothing on, followed by an extended black preview in landscape; excluded from the numerical tables and not assigned an unverified distance. Clip D: all angle tests at reported 50 cm.

| Clip / test | Marker / camera distance / nominal angle | Video seconds | Readings in order (mm) | Missing / hidden |
|---|---|---|---|---|
| A / distance | 75 mm / 50 cm | 43–52 | 135.8, 135.9, 135.9, 135.9, 135.8, 136.0, 136.0, 135.8, 136.2, 135.9 | 0 / 0 |
| B / distance | 75 mm / 75 cm | 5–14 | 136.6, 136.4, 136.5, 136.7, 136.8, 136.8, 136.3, 136.9, 136.5, 137.2 | 0 / 0 |
| B / distance | 75 mm / 100 cm | 50–59 | 137.8, 137.2, 137.9, 137.3, 137.1, 137.5, 136.5, 136.6, 137.3, 137.0 | 0 / 0 |
| B / distance | 100 mm / 100 cm | 95–104 | 136.2, 136.4, 136.4, 136.7, 136.6, 136.6, 136.7, 136.9, 137.0, 136.6 | 0 / 0 |
| B / distance | 100 mm / 75 cm | 142–151 | 136.1, 136.2, 136.0, 135.8, 136.4, 136.1, 136.0, 136.0, 136.1, 136.1 | 0 / 0 |
| B / distance | 100 mm / 50 cm | 200–209 | 135.4, 135.6, 135.5, 135.4, 135.7, 135.5, 135.6, 135.7, 135.4, 135.6 | 0 / 0 |
| B / distance | 50 mm / 50 cm | 265–274 | 136.3, 136.6, 136.5, 136.5, 136.5, 136.4, 136.5, 136.7, 136.5, 136.4 | 0 / 0 |
| B / distance | 50 mm / 75 cm | 335–344 | 137.4, M, 137.8, M, 132.3, 137.5, 137.7, 137.2, 136.6, 137.0 | 2 / 0 |
| B / distance | 50 mm / 100 cm | 435–444 | 137.6, 136.8, 139.0, 139.0, 136.8, 139.1, 138.4, 139.3, 138.1, 138.2 | 0 / 0 |
| B / distance | 20 mm / 67 cm | 505–514 | 141.0, 140.2, 138.6, 139.2, 140.2, 140.3, 140.6, 140.1, 139.4, 140.9 | 0 / 0 |
| B / distance | 20 mm / 50 cm | 565–574 | 138.4, 138.6, 137.4, 138.6, 138.7, 138.8, 136.5, 139.1, 138.7, 138.1 | 0 / 0 |
| D / angle | 20 mm / 50 cm / 0° | 10–19 | 138.2, 140.6, 137.4, 139.5, 136.8, 138.0, 139.0, 136.6, 138.4, 138.6 | 0 / 0 |
| D / angle | 20 mm / 50 cm / 15° | 72–81 | 143.6, 141.0, M, 141.6, 146.9, 142.4, 140.2, 135.6, 144.5, 141.6 | 1 / 0 |
| D / angle | 20 mm / 50 cm / 30° | 100–109 | 133.5, H, H, H, 145.5, 148.8, 138.6, 145.8, 140.7, 141.3 | 0 / 3 |
| D / angle | 20 mm / 50 cm / 45° | 150–159 | 142.4, M, M, 140.9, 148.8, 144.4, 140.5, 136.4, M, 146.0 | 3 / 0 |
| D / angle | 20 mm / 50 cm / 60° | 175–184 | M, M, M, M, M, M, M, M, M, M | 10 / 0 |
| D / angle | 50 mm / 50 cm / 0° | 230–239 | 135.9, 135.8, 135.5, 136.0, 136.1, 135.8, 135.9, 136.3, 136.0, 136.0 | 0 / 0 |
| D / angle | 50 mm / 50 cm / 15° | 255–264 | 135.9, 136.2, 135.7, 136.1, 136.2, H, H, 135.6, 135.6, 136.3 | 0 / 2 |
| D / angle | 50 mm / 50 cm / 30° | 292–301 | 135.1, 135.0, 135.1, 135.2, 135.8, 136.0, 135.8, 136.0, 136.0, 135.7 | 0 / 0 |
| D / angle | 50 mm / 50 cm / 45° | 317–326 | 135.5, 135.3, 135.6, 135.7, 135.5, 136.1, 135.9, 135.7, 135.6, 135.9 | 0 / 0 |
| D / angle | 50 mm / 50 cm / 60° | 343–352 | 135.8, 135.5, 135.5, 135.7, 135.5, 136.1, 135.7, 136.1, 135.5, 136.4 | 0 / 0 |
| D / angle | 75 mm / 50 cm / 0° | 382–391 | 135.5, 135.9, 136.2, H, 135.8, 135.5, 135.7, 135.7, 135.7, 135.7 | 0 / 1 |
| D / angle | 75 mm / 50 cm / 15° | 402–411 | 135.5, 135.4, 135.4, 135.7, 135.5, 135.4, H, 135.4, 135.6, 135.6 | 0 / 1 |
| D / angle | 75 mm / 50 cm / 30° | 440–449 | 135.2, 135.5, 135.4, 135.6, 135.5, H, 135.3, H, 135.3, 135.2 | 0 / 2 |
| D / angle | 75 mm / 50 cm / 45° | 465–474 | 135.4, 135.4, 135.5, 135.3, 135.3, 135.6, 135.4, H, H, 135.7 | 0 / 2 |
| D / angle | 75 mm / 50 cm / 60° | 495–504 | 135.9, 135.8, 135.7, 135.6, 135.7, 135.6, 135.6, 136.0, 135.5, H | 0 / 1 |
| D / angle | 100 mm / 50 cm / 0° | 560–569 | 135.4, 135.3, 135.5, H, H, 135.4, 135.4, 135.4, 135.3, 135.3 | 0 / 2 |
| D / angle | 100 mm / 50 cm / 15° | 605–614 | 135.3, H, 135.3, 135.2, 135.5, 135.4, 135.2, 135.3, H, 135.4 | 0 / 2 |
| D / angle | 100 mm / 50 cm / 30° | 630–639 | 135.6, 135.3, 135.5, 135.3, 135.5, 135.4, 135.5, H, 135.5, 135.4 | 0 / 1 |
| D / angle | 100 mm / 50 cm / 45° | 655–664 | 135.6, 135.3, 135.5, 135.4, 135.4, 135.4, 135.5, 135.3, M, 135.7 | 1 / 0 |
| D / angle | 100 mm / 50 cm / 60° | 695–704 | M, 135.4, 135.3, 135.3, 135.5, 135.6, 135.4, 135.4, H, H | 1 / 2 |

The 20 mm pair was not observed at S20 distances beyond 67 cm in the supplied sampled sections. Missing angle samples are retained; hidden rows are not automatically classified as detection failures.

</details>

<details>
<summary>Original three-run readings (42 pair readings)</summary>

For the eight cross-height pairs in runs 1–2, reference = `sqrt(horizontal_spacing² + 43²)` mm. References for the other pairs were transcribed directly. Unrounded square roots were used in the calculations. Angles and the height difference were tester-reported, not independently verified.

#### Run 1

| Marker pair | Reference input (mm) | Reference (mm) | App (mm) | Absolute error (mm) | Error (%) |
|---|---|---:|---:|---:|---:|
| 5×5 #0 → 5×5 #1 | 135 | 135.00 | 136.2 | 1.20 | 0.89 |
| 5×5 #0 → 6×6 #0 | √(316² + 43²) | 318.91 | 318.8 | 0.11 | 0.04 |
| 5×5 #0 → 6×6 #1 | √(370² + 43²) | 372.49 | 370.5 | 1.99 | 0.53 |
| 5×5 #0 → 7×7 #0 | √(200² + 43²) | 204.57 | 199.9 | 4.67 | 2.28 |
| 5×5 #0 → 7×7 #1 | √(243² + 43²) | 246.78 | 244.3 | 2.48 | 1.00 |
| 5×5 #1 → 6×6 #0 | √(190² + 43²) | 194.81 | 196.4 | 1.59 | 0.82 |
| 5×5 #1 → 6×6 #1 | √(283² + 43²) | 286.25 | 281.4 | 4.85 | 1.69 |
| 5×5 #1 → 7×7 #0 | √(243² + 43²) | 246.78 | 247.6 | 0.82 | 0.33 |
| 5×5 #1 → 7×7 #1 | √(208² + 43²) | 212.40 | 210.4 | 2.00 | 0.94 |
| 6×6 #0 → 6×6 #1 | 135 | 135.00 | 136.0 | 1.00 | 0.74 |
| 6×6 #0 → 7×7 #0 | 321 | 321.00 | 323.8 | 2.80 | 0.87 |
| 6×6 #0 → 7×7 #1 | 205 | 205.00 | 203.6 | 1.40 | 0.68 |
| 6×6 #1 → 7×7 #0 | 295 | 295.00 | 295.1 | 0.10 | 0.03 |
| 6×6 #1 → 7×7 #1 | 161 | 161.00 | 160.3 | 0.70 | 0.43 |
| 7×7 #0 → 7×7 #1 | 135 | 135.00 | 135.3 | 0.30 | 0.22 |

#### Run 2

| Marker pair | Reference input (mm) | Reference (mm) | App (mm) | Absolute error (mm) | Error (%) |
|---|---|---:|---:|---:|---:|
| 5×5 #0 → 5×5 #1 | 135 | 135.00 | 137.1 | 2.10 | 1.56 |
| 5×5 #0 → 6×6 #0 | √(313² + 43²) | 315.94 | 315.6 | 0.34 | 0.11 |
| 5×5 #0 → 6×6 #1 | √(375² + 43²) | 377.46 | 384.1 | 6.64 | 1.76 |
| 5×5 #0 → 7×7 #0 | √(212² + 43²) | 216.32 | 215.7 | 0.62 | 0.29 |
| 5×5 #0 → 7×7 #1 | √(245² + 43²) | 248.74 | 256.0 | 7.26 | 2.92 |
| 5×5 #1 → 6×6 #0 | √(181² + 43²) | 186.04 | 193.3 | 7.26 | 3.90 |
| 5×5 #1 → 6×6 #1 | √(281² + 43²) | 284.27 | 291.2 | 6.93 | 2.44 |
| 5×5 #1 → 7×7 #0 | √(252² + 43²) | 255.64 | 253.1 | 2.54 | 0.99 |
| 5×5 #1 → 7×7 #1 | √(203² + 43²) | 207.50 | 213.5 | 6.00 | 2.89 |
| 6×6 #0 → 6×6 #1 | 135 | 135.00 | 136.3 | 1.30 | 0.96 |
| 6×6 #0 → 7×7 #0 | 304 | 304.00 | 309.7 | 5.70 | 1.87 |
| 6×6 #0 → 7×7 #1 | 186 | 186.00 | 187.5 | 1.50 | 0.81 |
| 6×6 #1 → 7×7 #0 | 294 | 294.00 | 293.9 | 0.10 | 0.03 |
| 6×6 #1 → 7×7 #1 | 158 | 158.00 | 159.8 | 1.80 | 1.14 |
| 7×7 #0 → 7×7 #1 | 135 | 135.00 | 135.5 | 0.50 | 0.37 |

#### Run 3

| Marker pair | Reference input (mm) | Reference (mm) | App (mm) | Absolute error (mm) | Error (%) |
|---|---|---:|---:|---:|---:|
| 5×5 #0 → 6×6 #0 | 341 | 341.00 | 340.2 | 0.80 | 0.23 |
| 5×5 #0 → 6×6 #1 | 219 | 219.00 | 217.4 | 1.60 | 0.73 |
| 5×5 #0 → 7×7 #0 | 214 | 214.00 | 210.9 | 3.10 | 1.45 |
| 5×5 #0 → 7×7 #1 | 334 | 334.00 | 336.0 | 2.00 | 0.60 |
| 5×5 #1 → 6×6 #0 | 322 | 322.00 | 323.4 | 1.40 | 0.43 |
| 5×5 #1 → 6×6 #1 | 189 | 189.00 | 189.3 | 0.30 | 0.16 |
| 5×5 #1 → 7×7 #0 | 302 | 302.00 | 301.0 | 1.00 | 0.33 |
| 5×5 #1 → 7×7 #1 | 392 | 392.00 | 399.2 | 7.20 | 1.84 |
| 6×6 #0 → 7×7 #0 | 255 | 255.00 | 253.0 | 2.00 | 0.78 |
| 6×6 #0 → 7×7 #1 | 215 | 215.00 | 212.3 | 2.70 | 1.26 |
| 6×6 #1 → 7×7 #0 | 216 | 216.00 | 211.3 | 4.70 | 2.18 |
| 6×6 #1 → 7×7 #1 | 252 | 252.00 | 250.2 | 1.80 | 0.71 |

Run 2's handwritten 181 mm horizontal entry produces a 186.04 mm 3D reference. Handwritten rounded answers/percentages were recalculated from the recorded inputs. Run 3 used the typed references in app order and sampled the first eight pairs at 30 s and the remaining four at 35 s. Smoothing was off in that section.

A later part of the original run 3 showed about 140 mm for the nominal 135 mm 100 mm marker pair, with smoothing enabled. It is an additional observation within the same run, not a fourth independent run or angle-specific mean. Camera distance and independently measured angles were not supplied for these original runs.

</details>

<details>
<summary>Earlier S25 angle readings (65 sample records)</summary>

Cross-sheet references use original run 3's arrangement. `M` = unavailable in the selected frame; `H` = not visible in the selected sample. Samples across scrolling are not simultaneous. Reference uncertainty remains the estimated ±3 mm; it is not subtracted.

| Angle | Video seconds | Pair | Reference (mm) | Reading (mm) |
|---|---:|---|---:|---:|
| 0° | 109.6 | 5x5 #0 -> 6x6 #0 | 341 | 344.9 |
| 0° | 109.6 | 5x5 #0 -> 6x6 #1 | 219 | 221.3 |
| 0° | 109.6 | 5x5 #0 -> 7x7 #0 | 214 | 210.4 |
| 0° | 109.6 | 5x5 #0 -> 7x7 #1 | 334 | 336.6 |
| 0° | 109.6 | 5x5 #1 -> 6x6 #0 | 322 | 325.5 |
| 0° | 109.6 | 5x5 #1 -> 6x6 #1 | 189 | 189.8 |
| 0° | 109.6 | 5x5 #1 -> 7x7 #0 | 302 | 298.7 |
| 0° | 109.6 | 5x5 #1 -> 7x7 #1 | 392 | 397.6 |
| 0° | 109.8 | 6x6 #0 -> 7x7 #0 | 255 | 253.3 |
| 0° | 109.8 | 6x6 #0 -> 7x7 #1 | 215 | 211.5 |
| 0° | 109.8 | 6x6 #1 -> 7x7 #0 | 216 | 212.1 |
| 0° | 109.8 | 6x6 #1 -> 7x7 #1 | 252 | 250.2 |
| 15° | 96 | 5x5 #0 -> 6x6 #0 | 341 | 341.2 |
| 15° | 96 | 5x5 #0 -> 6x6 #1 | 219 | 219.2 |
| 15° | 96 | 5x5 #0 -> 7x7 #0 | 214 | 209.5 |
| 15° | 96 | 5x5 #0 -> 7x7 #1 | 334 | 333.9 |
| 15° | 96 | 5x5 #1 -> 6x6 #0 | 322 | 321.4 |
| 15° | 96 | 5x5 #1 -> 6x6 #1 | 189 | 188.4 |
| 15° | 96 | 5x5 #1 -> 7x7 #0 | 302 | 297.9 |
| 15° | 96 | 5x5 #1 -> 7x7 #1 | 392 | 394.5 |
| 15° | 98 | 6x6 #0 -> 7x7 #0 | 255 | 251.3 |
| 15° | 98 | 6x6 #0 -> 7x7 #1 | 215 | 209.9 |
| 15° | 98 | 6x6 #1 -> 7x7 #0 | 216 | 210.0 |
| 15° | 98 | 6x6 #1 -> 7x7 #1 | 252 | 248.7 |
| 30° |  | 5x5 #0 -> 6x6 #0 | 341 | H |
| 30° |  | 5x5 #0 -> 6x6 #1 | 219 | H |
| 30° |  | 5x5 #0 -> 7x7 #0 | 214 | H |
| 30° |  | 5x5 #0 -> 7x7 #1 | 334 | H |
| 30° |  | 5x5 #1 -> 6x6 #0 | 322 | H |
| 30° |  | 5x5 #1 -> 6x6 #1 | 189 | H |
| 30° |  | 5x5 #1 -> 7x7 #0 | 302 | H |
| 30° |  | 5x5 #1 -> 7x7 #1 | 392 | H |
| 30° | 78 | 6x6 #0 -> 7x7 #0 | 255 | 249.6 |
| 30° | 78 | 6x6 #0 -> 7x7 #1 | 215 | 209.4 |
| 30° | 78 | 6x6 #1 -> 7x7 #0 | 216 | 206.9 |
| 30° | 78 | 6x6 #1 -> 7x7 #1 | 252 | 247.9 |
| 45° | 55 | 5x5 #0 -> 6x6 #0 | 341 | 342.1 |
| 45° | 55 | 5x5 #0 -> 6x6 #1 | 219 | 219.6 |
| 45° | 55 | 5x5 #0 -> 7x7 #0 | 214 | 210.7 |
| 45° | 55 | 5x5 #0 -> 7x7 #1 | 334 | 334.6 |
| 45° | 55 | 5x5 #1 -> 6x6 #0 | 322 | 321.4 |
| 45° | 55 | 5x5 #1 -> 6x6 #1 | 189 | 188.5 |
| 45° | 55 | 5x5 #1 -> 7x7 #0 | 302 | 299.8 |
| 45° | 55 | 5x5 #1 -> 7x7 #1 | 392 | 395.3 |
| 45° | 58 | 6x6 #0 -> 7x7 #0 | 255 | 251.6 |
| 45° | 58 | 6x6 #0 -> 7x7 #1 | 215 | 210.6 |
| 45° | 58 | 6x6 #1 -> 7x7 #0 | 216 | 208.6 |
| 45° | 58 | 6x6 #1 -> 7x7 #1 | 252 | 247.3 |
| 60° | 20 | 5x5 #0 -> 6x6 #0 | 341 | H |
| 60° | 20 | 5x5 #0 -> 6x6 #1 | 219 | H |
| 60° | 21 | 5x5 #0 -> 7x7 #0 | 214 | M |
| 60° | 21 | 5x5 #0 -> 7x7 #1 | 334 | M |
| 60° | 21 | 5x5 #1 -> 6x6 #0 | 322 | M |
| 60° | 21 | 5x5 #1 -> 6x6 #1 | 189 | M |
| 60° | 21 | 5x5 #1 -> 7x7 #0 | 302 | M |
| 60° | 21 | 5x5 #1 -> 7x7 #1 | 392 | M |
| 60° | 21 | 6x6 #0 -> 7x7 #0 | 255 | 245.8 |
| 60° | 21 | 6x6 #0 -> 7x7 #1 | 215 | 209.3 |
| 60° | 30 | 6x6 #1 -> 7x7 #0 | 216 | 213.3 |
| 60° | 30 | 6x6 #1 -> 7x7 #1 | 252 | 241.2 |

Same-sheet 100 mm marker pair, IDs 0 and 1:

| Angle | Video seconds | Reference (mm) | Reading (mm) |
|---|---:|---:|---:|
| 0° | 110 | 135 | 135.6 |
| 15° | 100 | 135 | 135.4 |
| 30° | 80 | 135 | 135.2 |
| 45° | 58 | 135 | 136.0 |
| 60° | 30 | 135 | 133.5 |

Original videos and notebook images remain with the testers and are not included in the repository.

</details>
