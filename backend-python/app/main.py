from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import psycopg2
from pymongo import MongoClient
import pandas as pd
from datetime import datetime
import os
from dotenv import load_dotenv

load_dotenv()

app = FastAPI(title="Energia Analytics Python API")

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configurações dos bancos
POSTGRES_CONFIG = {
    'host': 'localhost',
    'database': 'energia_db',
    'user': 'energia_app',
    'password': 'app_password'
}

MONGO_CONFIG = {
    'host': 'localhost',
    'port': 27017,
    'username': 'energia_app',
    'password': 'app_password'
}

# Modelos Pydantic
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

# Rotas
@app.get("/")
def read_root():
    return {"message": "API Python para Análise de Energia"}

@app.get("/health")
def health_check():
    try:
        # Testar conexão PostgreSQL
        pg_conn = psycopg2.connect(**POSTGRES_CONFIG)
        pg_conn.close()
        
        # Testar conexão MongoDB
        mongo_client = MongoClient(**MONGO_CONFIG)
        mongo_client.admin.command('ping')
        mongo_client.close()
        
        return {
            "status": "healthy",
            "postgres": "connected",
            "mongodb": "connected"
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/analises/tecnicas", response_model=AnaliseResponse)
async def criar_analise_tecnica(request: AnaliseRequest):
    """Criar uma análise técnica baseada nos dados do PostgreSQL"""
    
    # Buscar dados do PostgreSQL
    pg_conn = psycopg2.connect(**POSTGRES_CONFIG)
    
    query = """
        SELECT i.ano, i.mes, i.dec_anual, i.fec_anual, c.nome as concessionaria
        FROM energia.indicadores i
        JOIN energia.concessionarias c ON i.concessionaria_id = c.id
        WHERE i.concessionaria_id = %s 
        AND i.ano BETWEEN %s AND %s
        ORDER BY i.ano, i.mes
    """
    
    df = pd.read_sql(query, pg_conn, params=(request.concessionaria_id, request.ano_inicio, request.ano_fim))
    pg_conn.close()
    
    # Análise dos dados
    analise = {}
    recomendacoes = []
    
    if not df.empty:
        # Calcular médias
        media_dec = df['dec_anual'].mean()
        media_fec = df['fec_anual'].mean()
        
        analise = {
            'media_dec': float(media_dec),
            'media_fec': float(media_fec),
            'tendencia_dec': 'crescimento' if df['dec_anual'].iloc[-1] > df['dec_anual'].iloc[0] else 'queda',
            'total_meses': len(df),
            'concessionaria': df['concessionaria'].iloc[0]
        }
        
        # Gerar recomendações
        if media_dec > 10:
            recomendacoes.append("Alta duração de interrupções - Necessário investimento em infraestrutura")
        if media_fec > 5:
            recomendacoes.append("Alta frequência de interrupções - Revisar manutenção preventiva")
    else:
        analise = {'error': 'Sem dados para o período'}
        recomendacoes.append("Coletar mais dados para análise robusta")
    
    # Salvar no MongoDB
    mongo_client = MongoClient(**MONGO_CONFIG)
    db = mongo_client.energia_analytics
    
    resultado = {
        'concessionaria_id': request.concessionaria_id,
        'data_analise': datetime.now(),
        'periodo': {'inicio': request.ano_inicio, 'fim': request.ano_fim},
        'resultados': analise,
        'recomendacoes': recomendacoes
    }
    
    result = db.analises_tecnicas.insert_one(resultado)
    mongo_client.close()
    
    return {
        'id': str(result.inserted_id),
        **resultado
    }

@app.get("/api/analises/{analise_id}")
async def get_analise(analise_id: str):
    """Recuperar uma análise do MongoDB"""
    from bson.objectid import ObjectId
    
    mongo_client = MongoClient(**MONGO_CONFIG)
    db = mongo_client.energia_analytics
    
    analise = db.analises_tecnicas.find_one({'_id': ObjectId(analise_id)})
    mongo_client.close()
    
    if not analise:
        raise HTTPException(status_code=404, detail="Análise não encontrada")
    
    analise['id'] = str(analise.pop('_id'))
    return analise

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)