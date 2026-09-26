from collections.abc import AsyncGenerator
from contextlib import asynccontextmanager

from fastapi import FastAPI

from app.database import engine
from app.models import Base
from app.routers import health, scans


@asynccontextmanager
async def lifespan(app: FastAPI) -> AsyncGenerator[None]:
    Base.metadata.create_all(bind=engine)
    yield


app = FastAPI(title="ArUco Mapper API", version="0.1.0", lifespan=lifespan)

API_PREFIX = "/api/v1"

app.include_router(scans.router, prefix=API_PREFIX)
app.include_router(health.router, prefix=API_PREFIX)
