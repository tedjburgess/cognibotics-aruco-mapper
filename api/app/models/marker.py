import uuid
from datetime import datetime

from sqlalchemy import DateTime, Enum, Integer, UniqueConstraint, func
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.enums import ArucoDictionary
from app.models.base import Base


class Marker(Base):
    __tablename__ = "markers"
    __table_args__ = (UniqueConstraint("aruco_id", "dictionary", name="uq_markers_variant"),)

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    aruco_id: Mapped[int] = mapped_column(Integer, nullable=False)
    dictionary: Mapped[ArucoDictionary] = mapped_column(
        Enum(ArucoDictionary, native_enum=False),
        default=ArucoDictionary.DICT_6X6_50,
        nullable=False,
    )
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    observations = relationship("Observation", back_populates="marker")
