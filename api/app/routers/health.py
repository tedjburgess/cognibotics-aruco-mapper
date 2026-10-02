from fastapi import APIRouter

router = APIRouter(prefix="/health")


@router.get("")
def health() -> dict[str, str]:
    return {"status": "ok"}
