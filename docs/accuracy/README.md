# Issue #39: marker distance accuracy

Recorded results: [all three physical measurement runs and estimated ±3 mm reference uncertainty](results-2026-10-14.md). The report includes 42 readings, a like-for-like 36-reading comparison (12 cross-sheet pairs per run), and one video angle-test run. The video observations still need mapping to the four requested angles.

This is a physical experiment, not camera calibration. The ticket needs a known marker centre-to-centre distance, app results at **0°, 15°, 35° and 60°**, and a comparison shared with the group. The local #57 implementation now shows fresh distances in **Measurement → Start measurement**. The history display separately shows `relations[].distance_mm` returned by the backend. Follow [the live measurement guide](../measurement/README.md) for the known-size mapping and calibration limitations.

## Recorded run conditions

The tester described the setups in [PR #63](https://github.com/tedjburgess/cognibotics-aruco-mapper/pull/63):

| Run | Reported camera conditions | Marker arrangement | Mean absolute percentage error |
|---|---|---|---:|
| 1 | Fixed view, approximately 0–5° tilt | Fixed marker spacing, 43 mm height difference | 0.81% |
| 2 | Fixed view, approximately 30° tilt | Fixed marker spacing, 43 mm height difference | 1.60% |
| 3 | Camera moving through different angles | Fixed marker spacing, all markers at the same elevation | 0.89% |

Each result uses the same 12 cross-sheet pairs against that run's own reference distances. Combined mean absolute percentage error: **1.10%**, with estimated **±3 mm manual reference uncertainty**. The report retains all 42 supplied readings. Run 3's percentage uses the documented 30–35 second samples, not every angle or frame in the video.

These approximate angles are tester-reported conditions, not independently verified angle measurements. Manual reference error, viewing angle, height differences and estimated camera parameters may contribute to run 2's higher error; these runs do not isolate the cause. This setup summary does not establish a separate result at each of the four angles requested by #39.

## Before recording results

1. Use a physical phone and measured printed markers. Measure the black square including its black border, excluding the white quiet zone. Mathias's notes specify 4×4: 20 mm, 5×5: 50 mm, 6×6: 75 mm and 7×7: 100 mm. Record the actual dictionary including capacity, e.g. `DICT_4X4_50`; 4×4 describes the payload grid, not the physical size.
2. Use the live measurement view to record a fresh marker-pair distance. The prototype does not yet save new scans; leave `scan_id` blank and identify each capture with its run ID and a timestamp/screenshot in the notes. Old history rows or manually seeded API values only test the display, not camera accuracy.
3. Record the app/backend revisions, phone, lens/resolution, calibration profile and the measuring instrument's uncertainty. Use the same lens, zoom and resolution throughout a setup. If calibration is unavailable, label the run as an uncalibrated prototype; do not present it as calibrated accuracy.
4. Agree the acceptable absolute error with Mathias. His notes ask for documented tolerance and instructions for the working distance; they do not specify a numerical tolerance.

## Baseline: both markers on the same plane

1. Fix two distinct markers to a rigid board. Use a centre spacing that leaves room for both printed markers; the ticket's example of 30 mm cannot accommodate two 100 mm markers without overlap. A larger spacing such as 300 mm is a possible setup, not a recorded result. Measure and enter the actual spacing.
2. Mark the physical centres and measure centre-to-centre, not edge-to-edge. Secure the board so this distance stays fixed.
3. Define **0°** as the camera optical axis perpendicular to the board. Tilt in one recorded direction to 15°, 35° and 60°, keeping the lens-to-board-centre distance fixed where possible. Record the actual camera distance and angle; avoid changing both unknowingly.
4. For repeatability, three separate captures/scans per angle are recommended; this is not an extra requirement in #39. Do not count three adjacent video frames as three independent trials. Record the live marker-pair distance and identify the capture with a timestamp/screenshot. If no distance is produced, record a failed detection or rejected run; do not write zero or omit the attempt.
5. If following the recommended three captures per angle, record the 12 trials in a copy of `runs-template.csv`. Add rows if more repetitions are needed. The template contains planned trials only, with no invented measurements.

## Extension: markers at different heights

Keep this separate from the baseline. Fix one marker to a measured support, record the **marker-centre height difference**, and measure the true straight-line 3D distance. If horizontal centre spacing is `d` and the height difference is `h`, the reference is `sqrt(d² + h²)`, not `d`. Record units in mm and do not compare a planar app distance with a 3D reference as if they were the same quantity.

The closed prototype in PR #37 used an anchor-plane assumption. The replacement in #57 instead solves each known-size marker independently and uses all three coordinates. A raised-marker experiment checks that implementation and its camera parameters; it does not automatically establish acceptable accuracy. Keep the original measurements even if they fail.

## Compare and present

For each trial: `signed error = app distance − reference distance`; `absolute error = |signed error|`; `relative error % = 100 × absolute error / reference distance`.

For each angle report the number of valid and failed attempts, mean signed error (bias), mean absolute error and maximum absolute error. Keep flat and elevated configurations, marker sizes/dictionaries, camera distances and calibration profiles separate. Present the CSV plus a short table or chart to the group and flag large errors or missing detections for the calculation owner.

For a working-distance recommendation, repeat the full angle test at several measured camera distances for each relevant marker size and height setup. Report the furthest **tested** passing distance for those stated conditions. One setup at one distance cannot establish a maximum, and a small experiment is not a guarantee for every environment.

## How to explain the approach

“I tested the measurement against an independently measured centre-to-centre distance, keeping the setup fixed while varying the camera angle. I repeated captures at each of the four angles and included failed attempts. I tested different heights separately using the true 3D reference distance, so I could identify whether the calculation was assuming one plane. Then I compared the errors against the agreed tolerance.”

Use past tense only after the physical experiment has actually been run. The frontend display and template alone do not complete #39.

Sources: [issue #39](https://github.com/tedjburgess/cognibotics-aruco-mapper/issues/39), [Weekly Questions for Mathias](https://docs.google.com/document/d/1i_YYHlNhNgMwPSgvP9i4gOGUjM7iwR7m1az3AuGLE7Y/edit), [OpenCV calibration and pose estimation](https://docs.opencv.org/4.4.0/d9/d0c/group__calib3d.html).
