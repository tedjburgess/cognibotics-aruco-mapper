# ArUco Mapper API

## Requirements

- [uv](https://docs.astral.sh/uv/)

## Run

Copy `.env.example` to `.env` and set variables.
Default values use local SQLite `aruco_mapper.db`.
Tables are automatically created on startup.

```bash
uv sync
uv run fastapi dev app/main.py
```

Open http://localhost:8000 (or `/docs` for swagger).

## Scan endpoints

### `POST /api/v1/scans`

Creates the site and cell if missing, then the scan, its observations and
relations. Returns the saved scan.

```json
{
  "site_name": "lab-a",
  "cell_name": "cell-a",
  "device_id": "phone-1",
  "measured_at": "2026-09-26T10:00:00Z",
  "observations": [
    {
      "aruco_id": 0,
      "dictionary": "DICT_6X6_50",
      "name": "object-origin",
      "position": {"x": 0.1, "y": 0.2, "z": 0.3},
      "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
      "size_mm": 50
    },
    {
      "aruco_id": 1,
      "dictionary": "DICT_6X6_50",
      "name": "object-x",
      "position": {"x": 1.1, "y": 0.2, "z": 0.3},
      "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
      "size_mm": 50
    }
  ],
  "relations": [
    {
      "from_aruco_id": 0,
      "from_dictionary": "DICT_6X6_50",
      "to_aruco_id": 1,
      "to_dictionary": "DICT_6X6_50",
      "distance_mm": 1550.0,
      "relative_position": {"x": 1.0, "y": 0.0, "z": 0.0}
    }
  ]
}
```

### `GET /api/v1/scans`

Returns all scans.

```json
[
  {
    "id": "3f2b1c0a-9e5d-4b2f-9a1c-5e8f7d6c4b3a",
    "site_name": "lab-a",
    "cell_name": "cell-a",
    "device_id": "phone-1",
    "measured_at": "2026-09-26T10:00:00Z",
    "created_at": "2026-09-26T10:00:01Z",
    "observations": [
      {
        "id": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
        "marker_id": "7c8d9e0f-1a2b-4c3d-9e8f-0a1b2c3d4e5f",
        "aruco_id": 0,
        "dictionary": "DICT_6X6_50",
        "name": "object-origin",
        "position": {"x": 0.1, "y": 0.2, "z": 0.3},
        "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
        "size_mm": 50
      },
      {
        "id": "6a7b8c9d-0e1f-4a2b-3c4d-5e6f7a8b9c0d",
        "marker_id": "8d9e0f1a-2b3c-4d5e-8f9a-0b1c2d3e4f50",
        "aruco_id": 1,
        "dictionary": "DICT_6X6_50",
        "name": "object-x",
        "position": {"x": 1.1, "y": 0.2, "z": 0.3},
        "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
        "size_mm": 50
      }
    ],
    "relations": [
      {
        "id": "9c8d7e6f-5a4b-4c3d-2e1f-0a9b8c7d6e5f",
        "from_marker_id": "7c8d9e0f-1a2b-4c3d-9e8f-0a1b2c3d4e5f",
        "to_marker_id": "8d9e0f1a-2b3c-4d5e-8f9a-0b1c2d3e4f50",
        "distance_mm": 1550.0,
        "relative_position": {"x": 1.0, "y": 0.0, "z": 0.0}
      }
    ]
  }
]
```

Each observation carries a `marker_id` pointing at the shared variant, so
clients can group observations of the same marker across scans and cells.
Relations reference markers by `from_marker_id` and `to_marker_id`.
