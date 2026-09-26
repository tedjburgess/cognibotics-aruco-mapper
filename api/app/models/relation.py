import uuid
from typing import Any

from sqlalchemy import JSON, Float, ForeignKey, Integer
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base import Base


class Relation(Base):
    __tablename__ = "relations"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    scan_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("scans.id"), nullable=False)
    from_aruco_id: Mapped[int] = mapped_column(Integer, nullable=False)
    to_aruco_id: Mapped[int] = mapped_column(Integer, nullable=False)
    distance_mm: Mapped[float] = mapped_column(Float, nullable=False)
    relative_position: Mapped[dict[str, Any] | None] = mapped_column(JSON)

    scan = relationship("Scan", back_populates="relations")
