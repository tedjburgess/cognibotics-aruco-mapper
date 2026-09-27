from datetime import datetime

from pydantic import BaseModel, Field

from app.schemas.observation_in import ObservationIn
from app.schemas.relation_in import RelationIn


class ScanIn(BaseModel):
    site_name: str = Field(min_length=1)
    cell_name: str = Field(min_length=1)
    device_id: str = Field(min_length=1)
    measured_at: datetime
    observations: list[ObservationIn] = Field(min_length=1)
    relations: list[RelationIn] = Field(default_factory=list)
