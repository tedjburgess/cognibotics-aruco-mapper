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

## Development

Linting, formatting and type checking are handled by [ruff](https://docs.astral.sh/ruff/)
and [mypy](https://mypy-lang.org/), both configured in `pyproject.toml`. The
`Makefile` wraps the common commands:

```bash
make help          # list all targets
make install       # sync dependencies
make run           # start the dev server
make format        # auto-fix lint and apply formatting
make check         # lint + format check + types (what CI runs)
```

Run the tools directly if you prefer:

```bash
uv run ruff check .        # lint
uv run ruff check . --fix  # lint and auto-fix
uv run ruff format .       # format
uv run mypy                # type check (strict)
```

Ruff also sorts imports and `__all__`; the config selects the `E`, `F`, `I`, `B`,
`UP`, `SIM`, `C4` and `RUF` rule sets at a 100 character line length.

`make check` also runs automatically on every push and pull request via
`.github/workflows/ci.yml`.

## Scan endpoints

### `POST /api/v1/scans`

Creates the cell if missing, then the scan, markers and relations. Returns the
saved scan.

```json
{
  "cell_name": "cell-a",
  "device_id": "phone-1",
  "measured_at": "2026-09-26T10:00:00Z",
  "markers": [
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
      "to_aruco_id": 1,
      "distance_mm": 1550.0,
      "relative_position": {"x": 1.0, "y": 0.0, "z": 0.0}
    }
  ]
}
```

### `GET /api/v1/scans`

Returns all scans

```json
[
  {
    "id": "3f2b1c0a-9e5d-4b2f-9a1c-5e8f7d6c4b3a",
    "cell_name": "cell-a",
    "device_id": "phone-1",
    "measured_at": "2026-09-26T10:00:00Z",
    "created_at": "2026-09-26T10:00:01Z",
    "markers": [
      {
        "id": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
        "aruco_id": 0,
        "dictionary": "DICT_6X6_50",
        "name": "object-origin",
        "position": {"x": 0.1, "y": 0.2, "z": 0.3},
        "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
        "size_mm": 50
      },
      {
        "id": "6a7b8c9d-0e1f-4a2b-3c4d-5e6f7a8b9c0d",
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
        "from_aruco_id": 0,
        "to_aruco_id": 1,
        "distance_mm": 1550.0,
        "relative_position": {"x": 1.0, "y": 0.0, "z": 0.0}
      }
    ]
  }
]
```
