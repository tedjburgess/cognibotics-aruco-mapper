import uuid

from sqlalchemy import ForeignKey, String
from sqlalchemy.orm import Mapped, mapped_column

from app.models.base import Base


class ScanSubmission(Base):
    """An idempotency key is unique across all scans, including across client retries."""

    __tablename__ = "scan_submissions"

    request_id: Mapped[str] = mapped_column(String(36), primary_key=True)
    payload_hash: Mapped[str] = mapped_column(String(64), nullable=False)
    scan_id: Mapped[uuid.UUID | None] = mapped_column(ForeignKey("scans.id"), nullable=True)
