from datetime import datetime
from typing import Any
from uuid import UUID

from pydantic import BaseModel, ConfigDict, model_validator

from app.schemas.marker_out import MarkerOut
from app.schemas.relation_out import RelationOut


class ScanOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    cell_name: str
    device_id: str
    measured_at: datetime
    created_at: datetime
    markers: list[MarkerOut]
    relations: list[RelationOut]

    @model_validator(mode="before")
    @classmethod
    def from_scan(cls, data: Any) -> Any:
        if isinstance(data, dict):
            return data

        return {
            "id": data.id,
            "cell_name": data.cell.name,
            "device_id": data.device_id,
            "measured_at": data.measured_at,
            "created_at": data.created_at,
            "markers": [MarkerOut.model_validate(m) for m in data.markers],
            "relations": [RelationOut.model_validate(r) for r in data.relations],
        }
