import uuid
from datetime import datetime

from sqlalchemy import DateTime, ForeignKey, Index, String, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base import Base


class Scan(Base):
    __tablename__ = "scans"
    __table_args__ = (
        Index("ix_scans_cell_id", "cell_id"),
        Index("ix_scans_measured_at", "measured_at"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    cell_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("cells.id"), nullable=False)
    device_id: Mapped[str] = mapped_column(String(255), nullable=False)
    measured_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    cell = relationship("Cell", back_populates="scans")
    observations = relationship("Observation", back_populates="scan", cascade="all, delete-orphan")
    relations = relationship("Relation", back_populates="scan", cascade="all, delete-orphan")
