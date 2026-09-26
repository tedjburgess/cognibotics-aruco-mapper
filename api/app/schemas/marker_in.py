from pydantic import BaseModel, Field

from app.enums import ArucoDictionary
from app.schemas.orientation import Orientation
from app.schemas.position import Position


class MarkerIn(BaseModel):
    aruco_id: int = Field(ge=0)
    dictionary: ArucoDictionary = ArucoDictionary.DICT_6X6_50
    name: str | None = None
    position: Position
    orientation: Orientation
    size_mm: float | None = Field(default=None, gt=0)
