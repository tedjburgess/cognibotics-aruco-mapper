import uuid
from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, String, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base import Base


class Scan(Base):
    __tablename__ = "scans"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    cell_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("cells.id"), nullable=False)
    device_id: Mapped[str] = mapped_column(String(255), nullable=False)
    measured_at: Mapped[datetime] = mapped_column(DateTime, nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    cell = relationship("Cell", back_populates="scans")
    markers = relationship("Marker", back_populates="scan", cascade="all, delete-orphan")
    relations = relationship("Relation", back_populates="scan", cascade="all, delete-orphan")
