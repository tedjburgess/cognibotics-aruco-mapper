from datetime import datetime

from pydantic import BaseModel, Field

from app.schemas.marker_in import MarkerIn
from app.schemas.relation_in import RelationIn


class ScanIn(BaseModel):
    cell_name: str = Field(min_length=1)
    device_id: str = Field(min_length=1)
    measured_at: datetime
    markers: list[MarkerIn] = Field(min_length=1)
    relations: list[RelationIn] = Field(default_factory=list)
