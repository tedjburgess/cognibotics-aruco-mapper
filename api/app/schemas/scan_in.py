from datetime import UTC, datetime

from pydantic import BaseModel, Field, field_validator

from app.schemas.observation_in import ObservationIn
from app.schemas.relation_in import RelationIn


class ScanIn(BaseModel):
    site_name: str = Field(min_length=1)
    cell_name: str = Field(min_length=1)
    device_id: str = Field(min_length=1)
    measured_at: datetime

    @field_validator("measured_at")
    @classmethod
    def normalize_measured_at(cls, value: datetime) -> datetime:
        if value.tzinfo is None or value.utcoffset() is None:
            raise ValueError("measured_at must include timezone information")
        return value.astimezone(UTC)

    observations: list[ObservationIn] = Field(min_length=1)
    relations: list[RelationIn] = Field(default_factory=list)
