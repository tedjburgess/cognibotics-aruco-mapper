from pydantic import BaseModel


class Orientation(BaseModel):
    x: float
    y: float
    z: float
    w: float
