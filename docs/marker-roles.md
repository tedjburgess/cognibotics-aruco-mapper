# Marker roles

The application uses fixed ArUco marker IDs to distinguish between directional and positional markers.

## Marker ID ranges

The same ID convention is used for all supported ArUco dictionaries.

| Marker IDs | Role |
| --- | --- |
| `0-9` | Directional reference |
| `10+` | Positional |

For example:

- `DICT_4X4_50`, ID `0` is directional.
- `DICT_4X4_50`, ID `10` is positional.
- `DICT_5X5_50`, ID `0` is directional.
- `DICT_5X5_50`, ID `10` is positional.

The dictionary is still part of the marker identity, so the same numeric ID in two different dictionaries represents two different markers.

## Marker origins

Positional and directional markers use different reference points when their 3D position is calculated.

### Positional markers

Positional markers use the centre of the marker as their origin.

```text
+-----------+
|           |
|     X     |  <- origin
|           |
+-----------+
```

### Directional markers

Directional markers use the marker-local top-left corner as their origin.

```text
origin ->   X-----------+
            |           |
            |           |
            |           |
            +-----------+
```

The corner is relative to the marker itself, not the camera or screen. Rotating the physical marker also rotates the reference corner with it.

## Distance measurements

Distances are calculated using the reference position of each marker.

This means the application can measure:

- centre-to-centre
- directional-to-centre
- centre-to-directional
- directional-to-directional

A directional-to-positional measurement therefore represents the 3D straight-line distance between the directional marker's top-left origin and the positional marker's centre.

The camera UI displays the distance type together with the measured value.

## Camera display

Detected markers show their role in the camera view.

Examples:

```text
ID 0 - Directional
ID 10 - Position
```

The existing certainty and scan-state information is still displayed together with the marker information.

## Supported dictionaries

The marker-role convention applies to the currently supported dictionaries:

- `DICT_4X4_50`
- `DICT_5X5_50`
- `DICT_6X6_50`
- `DICT_7X7_50`
- `DICT_ARUCO_ORIGINAL` (110 mm project test size, IDs 0–1023)

## Implementation

Marker roles are derived from the marker's `MarkerKey`.

```kotlin
fun MarkerKey.role(): MarkerOrigin {
    return if (id in 0..9) {
        MarkerOrigin.CORNER
    } else {
        MarkerOrigin.CENTER
    }
}
```

The pose solver continues to estimate the marker pose using the marker centre. For directional markers, the solved pose is transformed to the marker-local top-left corner before the position is returned.

This allows OpenCV's square pose estimation to keep its expected centred coordinate system while exposing the required directional reference position to the rest of the application.