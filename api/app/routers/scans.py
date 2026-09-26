from collections.abc import Sequence
from typing import Annotated

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.orm import Session

from app import models, schemas
from app.database import get_db

router = APIRouter(prefix="/scans")


@router.post("", response_model=schemas.ScanOut, status_code=status.HTTP_201_CREATED)
def create_scan(payload: schemas.ScanIn, db: Annotated[Session, Depends(get_db)]) -> models.Scan:
    cell = db.scalars(select(models.Cell).where(models.Cell.name == payload.cell_name)).first()

    if cell is None:
        cell = models.Cell(name=payload.cell_name)
        db.add(cell)

    scan = models.Scan(cell=cell, device_id=payload.device_id, measured_at=payload.measured_at)
    db.add(scan)
    db.flush()

    aruco_ids = {m.aruco_id for m in payload.markers}
    for m in payload.markers:
        scan.markers.append(
            models.Marker(
                aruco_id=m.aruco_id,
                dictionary=m.dictionary,
                name=m.name,
                position_x=m.position.x,
                position_y=m.position.y,
                position_z=m.position.z,
                orientation_qx=m.orientation.x,
                orientation_qy=m.orientation.y,
                orientation_qz=m.orientation.z,
                orientation_qw=m.orientation.w,
                size_mm=m.size_mm,
            )
        )

    for r in payload.relations:
        if r.from_aruco_id not in aruco_ids or r.to_aruco_id not in aruco_ids:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Relation references a marker that is not in this scan",
            )
        scan.relations.append(
            models.Relation(
                from_aruco_id=r.from_aruco_id,
                to_aruco_id=r.to_aruco_id,
                distance_mm=r.distance_mm,
                relative_position=(
                    r.relative_position.model_dump() if r.relative_position else None
                ),
            )
        )

    db.commit()

    return scan


@router.get("", response_model=list[schemas.ScanOut])
def list_scans(db: Annotated[Session, Depends(get_db)]) -> Sequence[models.Scan]:
    return db.scalars(select(models.Scan).order_by(models.Scan.measured_at.desc())).all()
