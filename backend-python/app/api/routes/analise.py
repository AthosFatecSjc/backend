from fastapi import APIRouter

from app.schemas.analise import AnaliseRequest, AnaliseResponse
from app.services.analise_service import (
    criar_analise_tecnica,
    get_analise,
    health_check,
)

router = APIRouter()


@router.get("/")
def read_root():
    return {"message": "API Python para Analise de Energia"}


@router.get("/health")
def check_health():
    return health_check()


@router.post("/api/analises/tecnicas", response_model=AnaliseResponse)
async def create_analise_tecnica(request: AnaliseRequest):
    return criar_analise_tecnica(request)


@router.get("/api/analises/{analise_id}")
async def read_analise(analise_id: str):
    return get_analise(analise_id)
