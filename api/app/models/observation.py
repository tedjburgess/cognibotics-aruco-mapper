import uuid

from sqlalchemy import Float, ForeignKey, Index, String, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base import Base


class Observation(Base):
    __tablename__ = "observations"
    __table_args__ = (
        UniqueConstraint("marker_id", "scan_id", name="uq_observations_marker_scan"),
        Index("ix_observations_scan_id", "scan_id"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    scan_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("scans.id"), nullable=False)
    marker_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("markers.id"), nullable=False)
    name: Mapped[str | None] = mapped_column(
        String(255)
    )  # marker object name (or possibly Juliet object name)
    position_x: Mapped[float] = mapped_column(Float, nullable=False)
    position_y: Mapped[float] = mapped_column(Float, nullable=False)
    position_z: Mapped[float] = mapped_column(Float, nullable=False)
    orientation_qx: Mapped[float] = mapped_column(Float, nullable=False)
    orientation_qy: Mapped[float] = mapped_column(Float, nullable=False)
    orientation_qz: Mapped[float] = mapped_column(Float, nullable=False)
    orientation_qw: Mapped[float] = mapped_column(Float, nullable=False)
    size_mm: Mapped[float | None] = mapped_column(Float)

    scan = relationship("Scan", back_populates="observations")
    marker = relationship("Marker", back_populates="observations")
