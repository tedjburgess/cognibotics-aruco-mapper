from pydantic import BaseModel, Field

from app.schemas.position import Position


class RelationIn(BaseModel):
    from_aruco_id: int = Field(ge=0)
    to_aruco_id: int = Field(ge=0)
    distance_mm: float = Field(ge=0)
    relative_position: Position | None = None
