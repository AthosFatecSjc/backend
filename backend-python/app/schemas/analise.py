from datetime import datetime
from typing import List

from pydantic import BaseModel


class AnaliseRequest(BaseModel):
    concessionaria_id: int
    ano_inicio: int
    ano_fim: int
    metricas: List[str]


class AnaliseResponse(BaseModel):
    id: str
    concessionaria_id: int
    data_analise: datetime
    resultados: dict
    recomendacoes: List[str]
