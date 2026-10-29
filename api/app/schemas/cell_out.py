from uuid import UUID

from pydantic import BaseModel, ConfigDict


class CellOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: UUID
    name: str
