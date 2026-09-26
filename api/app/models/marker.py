import uuid

from sqlalchemy import Enum, Float, ForeignKey, Integer, String
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.enums import ArucoDictionary
from app.models.base import Base


class Marker(Base):
    __tablename__ = "markers"

    id: Mapped[uuid.UUID] = mapped_column(primary_key=True, default=uuid.uuid4)
    scan_id: Mapped[uuid.UUID] = mapped_column(ForeignKey("scans.id"), nullable=False)
    aruco_id: Mapped[int] = mapped_column(Integer, nullable=False)
    dictionary: Mapped[ArucoDictionary] = mapped_column(
        Enum(ArucoDictionary, native_enum=False),
        default=ArucoDictionary.DICT_6X6_50,
        nullable=False,
    )
    name: Mapped[str | None] = mapped_column(String(255))  # Juliet object name
    position_x: Mapped[float] = mapped_column(Float, nullable=False)
    position_y: Mapped[float] = mapped_column(Float, nullable=False)
    position_z: Mapped[float] = mapped_column(Float, nullable=False)
    orientation_qx: Mapped[float] = mapped_column(Float, nullable=False)
    orientation_qy: Mapped[float] = mapped_column(Float, nullable=False)
    orientation_qz: Mapped[float] = mapped_column(Float, nullable=False)
    orientation_qw: Mapped[float] = mapped_column(Float, nullable=False)
    size_mm: Mapped[float | None] = mapped_column(Float)

    scan = relationship("Scan", back_populates="markers")
