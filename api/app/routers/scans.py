from collections.abc import Sequence
from hashlib import sha256
from typing import Annotated
from uuid import UUID

from fastapi import APIRouter, Depends, Header, HTTPException, status
from sqlalchemy import select
from sqlalchemy.exc import IntegrityError
from sqlalchemy.orm import Session, selectinload

from app import models, schemas
from app.database import get_db
from app.enums import ArucoDictionary

router = APIRouter(prefix="/scans")

MarkerKey = tuple[int, ArucoDictionary]


def _resolve_markers(
    db: Session, observations: Sequence[schemas.ObservationIn]
) -> dict[MarkerKey, models.Marker]:
    keys: set[MarkerKey] = {(o.aruco_id, o.dictionary) for o in observations}

    resolved: dict[MarkerKey, models.Marker] = {}
    for aruco_id, dictionary in sorted(keys):
        marker = db.scalars(
            select(models.Marker).where(
                models.Marker.aruco_id == aruco_id,
                models.Marker.dictionary == dictionary,
            )
        ).first()

        if marker is None:
            marker = models.Marker(aruco_id=aruco_id, dictionary=dictionary)
            db.add(marker)

        resolved[(aruco_id, dictionary)] = marker

    db.flush()

    return resolved


def _find_previous_submission(
    db: Session, request_id: str, payload_hash: str
) -> models.Scan | None:
    submission = db.get(models.ScanSubmission, request_id)
    if submission is None:
        return None
    if submission.payload_hash != payload_hash:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Request ID was previously used with a different scan payload",
        )
    if submission.scan_id is None:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="The request is still being processed; retry with the same request ID",
        )
    return db.scalars(
        select(models.Scan)
        .where(models.Scan.id == submission.scan_id)
        .options(
            selectinload(models.Scan.cell).selectinload(models.Cell.site),
            selectinload(models.Scan.observations).selectinload(models.Observation.marker),
            selectinload(models.Scan.relations),
        )
    ).one()


@router.post("", response_model=schemas.ScanOut, status_code=status.HTTP_201_CREATED)
def create_scan(
    payload: schemas.ScanIn,
    db: Annotated[Session, Depends(get_db)],
    idempotency_key: Annotated[UUID | None, Header(alias="Idempotency-Key")] = None,
) -> models.Scan:
    submission: models.ScanSubmission | None = None
    if idempotency_key is not None:
        request_id = str(idempotency_key)
        payload_hash = sha256(payload.model_dump_json().encode("utf-8")).hexdigest()
        previous = _find_previous_submission(db, request_id, payload_hash)
        if previous is not None:
            return previous

        # This unique-key reservation is in the SAME transaction as the scan.
        # If another request already committed the key, return its scan instead.
        submission = models.ScanSubmission(request_id=request_id, payload_hash=payload_hash)
        db.add(submission)
        try:
            db.flush()
        except IntegrityError:
            db.rollback()
            previous = _find_previous_submission(db, request_id, payload_hash)
            if previous is not None:
                return previous
            raise HTTPException(
                status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
                detail="Could not reserve request ID; retry with the same ID",
            ) from None

    site = db.scalars(select(models.Site).where(models.Site.name == payload.site_name)).first()
    if site is None:
        site = models.Site(name=payload.site_name)
        db.add(site)
        db.flush()

    cell = db.scalars(
        select(models.Cell).where(
            models.Cell.site_id == site.id,
            models.Cell.name == payload.cell_name,
        )
    ).first()
    if cell is None:
        cell = models.Cell(site_id=site.id, name=payload.cell_name)
        db.add(cell)
        db.flush()

    scan = models.Scan(cell=cell, device_id=payload.device_id, measured_at=payload.measured_at)
    db.add(scan)
    db.flush()

    markers = _resolve_markers(db, payload.observations)

    seen: set[MarkerKey] = set()
    for o in payload.observations:
        key = (o.aruco_id, o.dictionary)
        if key in seen:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Scan contains the same marker variant more than once",
            )
        seen.add(key)
        scan.observations.append(
            models.Observation(
                marker_id=markers[key].id,
                name=o.name,
                position_x=o.position.x,
                position_y=o.position.y,
                position_z=o.position.z,
                orientation_qx=o.orientation.x,
                orientation_qy=o.orientation.y,
                orientation_qz=o.orientation.z,
                orientation_qw=o.orientation.w,
                size_mm=o.size_mm,
            )
        )

    for r in payload.relations:
        from_key: MarkerKey = (r.from_aruco_id, r.from_dictionary)
        to_key: MarkerKey = (r.to_aruco_id, r.to_dictionary)
        if from_key not in seen or to_key not in seen:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Relation references a marker that is not in this scan",
            )
        if from_key == to_key:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Relation endpoints must be different markers",
            )
        scan.relations.append(
            models.Relation(
                from_marker_id=markers[from_key].id,
                to_marker_id=markers[to_key].id,
                distance_mm=r.distance_mm,
                relative_position=(
                    r.relative_position.model_dump() if r.relative_position else None
                ),
            )
        )

    if submission is not None:
        submission.scan_id = scan.id
    db.commit()

    return scan


@router.get("", response_model=list[schemas.ScanOut])
def get_scans(db: Annotated[Session, Depends(get_db)]) -> Sequence[models.Scan]:
    return db.scalars(
        select(models.Scan)
        .options(
            selectinload(models.Scan.cell).selectinload(models.Cell.site),
            selectinload(models.Scan.observations).selectinload(models.Observation.marker),
            selectinload(models.Scan.relations),
        )
        .order_by(models.Scan.measured_at.desc())
    ).all()
