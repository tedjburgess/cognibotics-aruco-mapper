from pydantic import BaseModel, Field

from app.enums import ArucoDictionary
from app.schemas.position import Position


class RelationIn(BaseModel):
    from_aruco_id: int = Field(ge=0)
    from_dictionary: ArucoDictionary = ArucoDictionary.DICT_6X6_50
    to_aruco_id: int = Field(ge=0)
    to_dictionary: ArucoDictionary = ArucoDictionary.DICT_6X6_50
    distance_mm: float = Field(ge=0)
    relative_position: Position | None = None
