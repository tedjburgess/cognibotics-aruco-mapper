from collections.abc import Sequence
from typing import Annotated

from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.orm import Session, selectinload

from app import models, schemas
from app.database import get_db

router = APIRouter(prefix="/sites", tags=["sites"])


@router.get("", response_model=list[schemas.SiteOut])
def get_sites(db: Annotated[Session, Depends(get_db)]) -> Sequence[models.Site]:
    return db.scalars(
        select(models.Site)
        .options(selectinload(models.Site.cells))
        .order_by(models.Site.name)
    ).all()
