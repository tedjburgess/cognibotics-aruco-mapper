from datetime import UTC, datetime
from typing import Any
from uuid import UUID

from pydantic import BaseModel, ConfigDict, field_validator, model_validator

from app.schemas.observation_out import ObservationOut
from app.schemas.relation_out import RelationOut


class ScanOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    site_name: str
    cell_name: str
    device_id: str
    measured_at: datetime
    created_at: datetime
    observations: list[ObservationOut]
    relations: list[RelationOut]

    @field_validator("measured_at", "created_at")
    @classmethod
    def normalize_timestamps(cls, value: datetime) -> datetime:
        if value.tzinfo is None or value.utcoffset() is None:
            return value.replace(tzinfo=UTC)

        return value.astimezone(UTC)

    @model_validator(mode="before")
    @classmethod
    def from_scan(cls, data: Any) -> Any:
        if isinstance(data, dict):
            return data

        return {
            "id": data.id,
            "site_name": data.cell.site.name,
            "cell_name": data.cell.name,
            "device_id": data.device_id,
            "measured_at": data.measured_at,
            "created_at": data.created_at,
            "observations": [ObservationOut.model_validate(o) for o in data.observations],
            "relations": [RelationOut.model_validate(r) for r in data.relations],
        }
