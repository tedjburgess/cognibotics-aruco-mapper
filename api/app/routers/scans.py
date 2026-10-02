from collections.abc import Sequence
from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
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


@router.post("", response_model=schemas.ScanOut, status_code=status.HTTP_201_CREATED)
def create_scan(payload: schemas.ScanIn, db: Annotated[Session, Depends(get_db)]) -> models.Scan:
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
