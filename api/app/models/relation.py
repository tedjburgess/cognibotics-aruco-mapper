import uuid
from typing import Any

from sqlalchemy import JSON, CheckConstraint, Float, ForeignKey, UniqueConstraint
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base import Base


class Relation(Base):
    __tablename__ = "relations"
    __table_args__ = (
        UniqueConstraint(
            "scan_id", "from_marker_id", "to_marker_id", name="uq_relations_endpoints"
        ),
        CheckConstraint("from_marker_id <> to_marker_id", name="ck_relations_distinct_endpoints"),
    )

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    scan_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("scans.id"), nullable=False)
    from_marker_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("markers.id"), nullable=False)
    to_marker_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("markers.id"), nullable=False)
    distance_mm: Mapped[float] = mapped_column(Float, nullable=False)
    relative_position: Mapped[dict[str, Any] | None] = mapped_column(JSON)

    scan = relationship("Scan", back_populates="relations")
