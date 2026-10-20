from pydantic import BaseModel, ConfigDict

from app.schemas.cell_out import CellOut


class SiteOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    name: str
    cells: list[CellOut]
