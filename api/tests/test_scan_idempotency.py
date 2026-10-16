import tempfile
import unittest
from collections.abc import Generator
from pathlib import Path
from uuid import uuid4

from fastapi.testclient import TestClient
from sqlalchemy import create_engine, func, select
from sqlalchemy.orm import Session, sessionmaker

from app import models
from app.database import get_db
from app.main import app


class ScanIdempotencyTest(unittest.TestCase):
    def setUp(self) -> None:
        self.directory = tempfile.TemporaryDirectory()
        path = Path(self.directory.name) / "test.db"
        self.engine = create_engine(f"sqlite:///{path}")
        models.Base.metadata.create_all(bind=self.engine)
        self.sessions = sessionmaker(bind=self.engine)

        def override_db() -> Generator[Session]:
            with self.sessions() as db:
                yield db

        app.dependency_overrides[get_db] = override_db
        self.client = TestClient(app)

    def tearDown(self) -> None:
        self.client.close()
        app.dependency_overrides.clear()
        self.engine.dispose()
        self.directory.cleanup()

    @staticmethod
    def payload() -> dict[str, object]:
        return {
            "site_name": "Factory",
            "cell_name": "Assembly",
            "device_id": "Android",
            "measured_at": "2026-10-16T08:00:00Z",
            "observations": [
                {
                    "aruco_id": 3,
                    "dictionary": "DICT_6X6_50",
                    "position": {"x": 0, "y": 0, "z": 500},
                    "orientation": {"x": 0, "y": 0, "z": 0, "w": 1},
                    "size_mm": 75,
                }
            ],
            "relations": [],
        }

    def test_replay_returns_same_scan_without_creating_second_row(self) -> None:
        key = str(uuid4())
        headers = {"Idempotency-Key": key}
        first = self.client.post("/api/v1/scans", json=self.payload(), headers=headers)
        second = self.client.post("/api/v1/scans", json=self.payload(), headers=headers)

        self.assertEqual(201, first.status_code, first.text)
        self.assertEqual(201, second.status_code, second.text)
        self.assertEqual(first.json()["id"], second.json()["id"])
        with self.sessions() as db:
            self.assertEqual(1, db.scalar(select(func.count()).select_from(models.Scan)))
            self.assertEqual(1, db.scalar(select(func.count()).select_from(models.ScanSubmission)))

    def test_reusing_request_id_with_changed_payload_is_rejected(self) -> None:
        headers = {"Idempotency-Key": str(uuid4())}
        first = self.client.post("/api/v1/scans", json=self.payload(), headers=headers)
        changed = self.payload()
        changed["cell_name"] = "Different"
        second = self.client.post("/api/v1/scans", json=changed, headers=headers)
        self.assertEqual(201, first.status_code, first.text)
        self.assertEqual(409, second.status_code, second.text)

    def test_legacy_clients_without_key_can_still_post(self) -> None:
        first = self.client.post("/api/v1/scans", json=self.payload())
        second = self.client.post("/api/v1/scans", json=self.payload())
        self.assertEqual(201, first.status_code, first.text)
        self.assertEqual(201, second.status_code, second.text)
        self.assertNotEqual(first.json()["id"], second.json()["id"])


if __name__ == "__main__":
    unittest.main()
