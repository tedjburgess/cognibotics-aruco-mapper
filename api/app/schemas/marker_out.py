from typing import Any
from uuid import UUID

from pydantic import BaseModel, ConfigDict, model_validator

from app.enums import ArucoDictionary
from app.schemas.orientation import Orientation
from app.schemas.position import Position


class MarkerOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    aruco_id: int
    dictionary: ArucoDictionary
    name: str | None = None
    position: Position
    orientation: Orientation
    size_mm: float | None = None

    @model_validator(mode="before")
    @classmethod
    def from_marker(cls, data: Any) -> Any:
        if isinstance(data, dict):
            return data

        return {
            "id": data.id,
            "aruco_id": data.aruco_id,
            "dictionary": data.dictionary,
            "name": data.name,
            "position": {
                "x": data.position_x,
                "y": data.position_y,
                "z": data.position_z,
            },
            "orientation": {
                "x": data.orientation_qx,
                "y": data.orientation_qy,
                "z": data.orientation_qz,
                "w": data.orientation_qw,
            },
            "size_mm": data.size_mm,
        }
