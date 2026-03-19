from datetime import datetime

import pandas as pd
from fastapi import HTTPException

from app.core.config import MONGO_DATABASE
from app.db.connections import get_mongo_client, get_postgres_connection


def health_check():
    try:
        pg_conn = get_postgres_connection()
        pg_conn.close()

        mongo_client = get_mongo_client()
        mongo_client.admin.command("ping")
        mongo_client.close()

        return {
            "status": "healthy",
            "postgres": "connected",
            "mongodb": "connected",
        }
    except Exception as exc:
        raise HTTPException(status_code=500, detail=str(exc))


def criar_analise_tecnica(request):
    pg_conn = get_postgres_connection()

    query = """
        SELECT i.ano, i.mes, i.dec_anual, i.fec_anual, c.nome as concessionaria
        FROM energia.indicadores i
        JOIN energia.concessionarias c ON i.concessionaria_id = c.id
        WHERE i.concessionaria_id = %s
        AND i.ano BETWEEN %s AND %s
        ORDER BY i.ano, i.mes
    """

    df = pd.read_sql(
        query,
        pg_conn,
        params=(request.concessionaria_id, request.ano_inicio, request.ano_fim),
    )
    pg_conn.close()

    analise = {}
    recomendacoes = []

    if not df.empty:
        media_dec = df["dec_anual"].mean()
        media_fec = df["fec_anual"].mean()

        analise = {
            "media_dec": float(media_dec),
            "media_fec": float(media_fec),
            "tendencia_dec": (
                "crescimento"
                if df["dec_anual"].iloc[-1] > df["dec_anual"].iloc[0]
                else "queda"
            ),
            "total_meses": len(df),
            "concessionaria": df["concessionaria"].iloc[0],
        }

        if media_dec > 10:
            recomendacoes.append(
                "Alta duracao de interrupcoes - Necessario investimento em infraestrutura"
            )
        if media_fec > 5:
            recomendacoes.append(
                "Alta frequencia de interrupcoes - Revisar manutencao preventiva"
            )
    else:
        analise = {"error": "Sem dados para o periodo"}
        recomendacoes.append("Coletar mais dados para analise robusta")

    mongo_client = get_mongo_client()
    db = mongo_client[MONGO_DATABASE]

    resultado = {
        "concessionaria_id": request.concessionaria_id,
        "data_analise": datetime.now(),
        "periodo": {"inicio": request.ano_inicio, "fim": request.ano_fim},
        "resultados": analise,
        "recomendacoes": recomendacoes,
    }

    result = db.analises_tecnicas.insert_one(resultado)
    mongo_client.close()

    return {"id": str(result.inserted_id), **resultado}


def get_analise(analise_id: str):
    from bson.objectid import ObjectId

    mongo_client = get_mongo_client()
    db = mongo_client[MONGO_DATABASE]

    analise = db.analises_tecnicas.find_one({"_id": ObjectId(analise_id)})
    mongo_client.close()

    if not analise:
        raise HTTPException(status_code=404, detail="Analise nao encontrada")

    analise["id"] = str(analise.pop("_id"))
    return analise
