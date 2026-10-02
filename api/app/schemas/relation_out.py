from typing import Any
from uuid import UUID

from pydantic import BaseModel, ConfigDict, model_validator

from app.schemas.position import Position


class RelationOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    from_marker_id: UUID
    to_marker_id: UUID
    distance_mm: float
    relative_position: Position | None = None

    @model_validator(mode="before")
    @classmethod
    def from_relation(cls, data: Any) -> Any:
        if isinstance(data, dict):
            return data

        return {
            "id": data.id,
            "from_marker_id": data.from_marker_id,
            "to_marker_id": data.to_marker_id,
            "distance_mm": data.distance_mm,
            "relative_position": data.relative_position,
        }
