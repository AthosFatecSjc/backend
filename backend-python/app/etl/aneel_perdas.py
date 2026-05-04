import csv
import io
import json
import logging
import os
import unicodedata
from dataclasses import dataclass
from datetime import date, datetime
from pathlib import Path
from typing import Any

import psycopg2.extras
from playwright.sync_api import TimeoutError as PlaywrightTimeoutError
from playwright.sync_api import sync_playwright

from app.db.connections import get_postgres_connection

REPORT_URL = os.getenv(
    "ANEEL_PERDAS_REPORT_URL",
    "https://portalrelatorios.aneel.gov.br/luznatarifa/perdasenergias#!",
)
PAGE_NAME = os.getenv("ANEEL_PERDAS_PAGE_NAME", "")
PAGE_INDEX = int(os.getenv("ANEEL_PERDAS_PAGE_INDEX", "2"))
VISUAL_TITLE = os.getenv("ANEEL_PERDAS_VISUAL_TITLE", "")
EXPORT_MODE = os.getenv("ANEEL_PERDAS_EXPORT_MODE", "underlying").lower()
MAX_ROWS = int(os.getenv("ANEEL_PERDAS_MAX_ROWS", "30000"))
TIMEOUT_MS = int(os.getenv("ANEEL_PERDAS_TIMEOUT_MS", "120000"))
RETENTION_YEARS = int(os.getenv("ANEEL_PERDAS_RETENTION_YEARS", "5"))
HEADLESS = os.getenv("ANEEL_PERDAS_HEADLESS", "true").lower() not in {
    "0",
    "false",
    "no",
}
ARCHIVE_DIR = os.getenv("ANEEL_PERDAS_ARCHIVE_DIR", "")

TABULAR_VISUAL_TYPES = {
    "table",
    "tableEx",
    "pivotTable",
    "matrix",
    "tablix",
}

EXPORT_TYPE_JS = {
    "summarized": "Summarized",
    "underlying": "Underlying",
}

COLUMN_ALIASES = {
    "sigla": "sig_agente",
    "sig agente": "sig_agente",
    "agente": "sig_agente",
    "distribuidora": "distribuidora",
    "concessionaria": "distribuidora",
    "razao social": "distribuidora",
    "empresa": "distribuidora",
    "cnpj": "num_cnpj",
    "num cnpj": "num_cnpj",
    "numero cnpj": "num_cnpj",
    "data do processo": "data_processo",
    "data processo": "data_processo",
    "dataprocesso": "data_processo",
    "ano": "ano",
    "ano processo": "ano",
    "perdas nao tecnicas": "perdas_nao_tec",
    "perdas nao tec": "perdas_nao_tec",
    "perda nao tecnica": "perdas_nao_tec",
    "perdas nao tecnicas mwh": "perdas_nao_tec",
    "custo perdas nao tecnicas": "custo_perdas_nao_tec",
    "custo perdas nao tec": "custo_perdas_nao_tec",
    "custo perda nao tecnica": "custo_perdas_nao_tec",
    "custo perdas nao tecnicas r": "custo_perdas_nao_tec",
}

logger = logging.getLogger(__name__)

MISSING_TOKENS = {
    "",
    "-",
    "--",
    "n/a",
    "na",
    "nd",
    "n.d",
    "null",
    "none",
    "sem informacao",
    "sem informacoes",
    "sem informação",
    "sem informações",
}


@dataclass
class VisualExportResult:
    page_name: str
    visual_name: str
    visual_title: str
    visual_type: str
    csv_data: str


def _build_missing_data_labels(
    perdas_nao_tec: float | None,
    custo_perdas_nao_tec: float | None,
) -> list[str]:
    missing = []
    if perdas_nao_tec is None:
        missing.append("perdas_nao_tec: Dado ausente")
    if custo_perdas_nao_tec is None:
        missing.append("custo_perdas_nao_tec: Dado ausente")
    return missing


def _normalize_text(value: str) -> str:
    normalized = unicodedata.normalize("NFKD", value or "")
    ascii_only = normalized.encode("ascii", "ignore").decode("ascii")
    cleaned = "".join(char if char.isalnum() else " " for char in ascii_only.lower())
    return " ".join(cleaned.split())


def _parse_number(raw_value: str | None) -> float | None:
    if raw_value is None:
        return None
    value = raw_value.strip()
    if _normalize_text(value) in MISSING_TOKENS:
        return None
    value = value.replace("%", "").replace(".", "").replace(",", ".")
    try:
        return float(value)
    except ValueError:
        logger.warning("Valor numerico invalido recebido no ETL de perdas: %s", raw_value)
        return None


def _parse_date(raw_value: str | None) -> date | None:
    if raw_value is None:
        return None
    value = raw_value.strip()
    if _normalize_text(value) in MISSING_TOKENS:
        return None
    for fmt in ("%d/%m/%Y", "%Y-%m-%d", "%d-%m-%Y"):
        try:
            return datetime.strptime(value, fmt).date()
        except ValueError:
            continue
    return None


def _parse_year(raw_value: str | None) -> int | None:
    if raw_value is None:
        return None
    value = raw_value.strip()
    if _normalize_text(value) in MISSING_TOKENS:
        return None
    try:
        year = int(value)
    except ValueError:
        logger.warning("Ano invalido recebido no ETL de perdas: %s", raw_value)
        return None

    if year <= 0 or year > 9999:
        logger.warning("Ano fora de faixa recebido no ETL de perdas: %s", raw_value)
        return None

    return year


def _mask_cnpj(raw_value: str | None) -> str:
    if raw_value is None:
        return "n/a"
    digits = "".join(char for char in raw_value if char.isdigit())
    if len(digits) < 4:
        return "n/a"
    return f"***{digits[-4:]}"


def _load_distribuidoras(conn: Any) -> dict[str, int]:
    query = """
        SELECT id, sig_agente, num_cnpj, razao_social
        FROM aneel.distribuidora
    """
    lookup: dict[str, int] = {}
    with conn.cursor() as cursor:
        cursor.execute(query)
        for dist_id, sig_agente, num_cnpj, razao_social in cursor.fetchall():
            for raw_value in (sig_agente, num_cnpj, razao_social):
                if raw_value:
                    lookup[_normalize_text(str(raw_value))] = dist_id
    return lookup


def _canonicalize_headers(headers: list[str]) -> dict[str, str]:
    canonical = {}
    for header in headers:
        normalized_header = _normalize_text(header)
        key = COLUMN_ALIASES.get(normalized_header)
        if not key:
            if "data processo" in normalized_header:
                key = "data_processo"
            elif "perdas nao tecnicas" in normalized_header and "custo" not in normalized_header:
                key = "perdas_nao_tec"
            elif "custo perdas nao tecnicas" in normalized_header:
                key = "custo_perdas_nao_tec"
        if key:
            canonical[header] = key
    return canonical


def _archive_export(csv_data: str) -> str | None:
    if not ARCHIVE_DIR:
        return None
    archive_path = Path(ARCHIVE_DIR)
    archive_path.mkdir(parents=True, exist_ok=True)
    file_path = archive_path / f"aneel_perdas_{datetime.now():%Y%m%d_%H%M%S}.csv"
    file_path.write_text(csv_data, encoding="utf-8", newline="")
    return str(file_path)


def _parse_export_rows(csv_data: str) -> list[dict[str, str]]:
    lines = csv_data.splitlines()
    filtered_lines = [line for line in lines if line.strip()]
    if not filtered_lines:
        logger.error("Erro na extracao ANEEL perdas: CSV exportado veio vazio")
        return []

    reader = csv.DictReader(io.StringIO("\n".join(filtered_lines)))
    return [dict(row) for row in reader if row]


def _transform_rows(
    raw_rows: list[dict[str, str]],
    distribuidoras: dict[str, int],
) -> list[tuple[Any, ...]]:
    if not raw_rows:
        return []

    headers = list(raw_rows[0].keys())
    header_map = _canonicalize_headers(headers)

    if not any(candidate in header_map.values() for candidate in {"data_processo", "ano"}):
        raise ValueError(
            "Nao foi possivel identificar a coluna de data/ano no CSV exportado. "
            f"Colunas recebidas: {headers}"
        )

    if not any(
        candidate in header_map.values()
        for candidate in {"sig_agente", "distribuidora", "num_cnpj"}
    ):
        raise ValueError(
            "Nao foi possivel identificar a distribuidora no CSV exportado. "
            f"Colunas recebidas: {headers}"
        )

    min_year = datetime.now().year - RETENTION_YEARS
    transformed: list[tuple[Any, ...]] = []
    rows_with_content = 0
    for row in raw_rows:
        canonical_row = {
            mapped_key: (row.get(original_key) or "").strip()
            for original_key, mapped_key in header_map.items()
        }

        if any(value for value in canonical_row.values()):
            rows_with_content += 1

        distribuidora_id = None
        for value in (
            canonical_row.get("num_cnpj", ""),
            canonical_row.get("sig_agente", ""),
            canonical_row.get("distribuidora", ""),
        ):
            if not value:
                continue
            distribuidora_id = distribuidoras.get(_normalize_text(value))
            if distribuidora_id is not None:
                break

        if distribuidora_id is None:
            logger.warning(
                "Linha de perdas ignorada: distribuidora nao reconhecida (sig_agente=%s, distribuidora=%s, num_cnpj=%s, ano=%s)",
                canonical_row.get("sig_agente") or "n/a",
                canonical_row.get("distribuidora") or "n/a",
                _mask_cnpj(canonical_row.get("num_cnpj")),
                canonical_row.get("ano") or "n/a",
            )
            continue

        data_processo = _parse_date(canonical_row.get("data_processo"))
        ano_value = _parse_year(canonical_row.get("ano"))
        if ano_value is None and data_processo is not None:
            ano_value = data_processo.year

        if data_processo is None and ano_value is not None:
            try:
                data_processo = date(ano_value, 1, 1)
            except ValueError:
                logger.warning(
                    "Ano invalido para compor data_processo no ETL de perdas: %s",
                    ano_value,
                )
                continue

        if data_processo is None or ano_value is None:
            continue
        if ano_value < min_year:
            continue

        perdas_nao_tec = _parse_number(canonical_row.get("perdas_nao_tec"))
        custo_perdas_nao_tec = _parse_number(canonical_row.get("custo_perdas_nao_tec"))

        transformed.append(
            (
                distribuidora_id,
                data_processo,
                ano_value,
                perdas_nao_tec,
                custo_perdas_nao_tec,
                _build_missing_data_labels(perdas_nao_tec, custo_perdas_nao_tec),
            )
        )

    if rows_with_content == 0 or not transformed:
        logger.error("Erro na extracao ANEEL perdas: nenhum registro valido apos transformacao")

    return transformed


def _upsert_perdas(conn: Any, rows: list[tuple[Any, ...]]) -> int:
    if not rows:
        return 0

    query = """
        INSERT INTO aneel.perdas (
            id_distribuidora,
            data_processo,
            ano,
            perdas_nao_tec,
            custo_perdas_nao_tec,
            dados_ausentes
        )
        VALUES %s
        ON CONFLICT (id_distribuidora, ano) DO UPDATE SET
            data_processo = COALESCE(EXCLUDED.data_processo, aneel.perdas.data_processo),
            perdas_nao_tec = COALESCE(EXCLUDED.perdas_nao_tec, aneel.perdas.perdas_nao_tec),
            custo_perdas_nao_tec = COALESCE(
                EXCLUDED.custo_perdas_nao_tec,
                aneel.perdas.custo_perdas_nao_tec
            ),
            dados_ausentes = ARRAY_REMOVE(
                ARRAY[
                    CASE
                        WHEN COALESCE(EXCLUDED.perdas_nao_tec, aneel.perdas.perdas_nao_tec) IS NULL
                        THEN 'perdas_nao_tec: Dado ausente'
                    END,
                    CASE
                        WHEN COALESCE(
                            EXCLUDED.custo_perdas_nao_tec,
                            aneel.perdas.custo_perdas_nao_tec
                        ) IS NULL
                        THEN 'custo_perdas_nao_tec: Dado ausente'
                    END
                ],
                NULL
            )
    """
    with conn.cursor() as cursor:
        psycopg2.extras.execute_values(cursor, query, rows)
    conn.commit()
    return len(rows)


def _open_report(page: Any) -> None:
    page.goto(REPORT_URL, wait_until="domcontentloaded", timeout=TIMEOUT_MS)
    page.wait_for_function(
        "() => !!window.report && typeof window.report.getPages === 'function'",
        timeout=TIMEOUT_MS,
    )
    page.wait_for_timeout(5000)


def discover_report_structure() -> list[dict[str, Any]]:
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=HEADLESS)
        page = browser.new_page()
        try:
            _open_report(page)
            return page.evaluate(
                """
                async () => {
                    const pages = await window.report.getPages();
                    const payload = [];
                    for (const reportPage of pages) {
                        const visuals = await reportPage.getVisuals();
                        payload.push({
                            name: reportPage.name,
                            displayName: reportPage.displayName,
                            visuals: visuals.map((visual) => ({
                                name: visual.name,
                                title: visual.title,
                                type: visual.type,
                                x: visual.layout?.x ?? null,
                                y: visual.layout?.y ?? null,
                                width: visual.layout?.width ?? null,
                                height: visual.layout?.height ?? null
                            }))
                        });
                    }
                    return payload;
                }
                """
            )
        finally:
            browser.close()


def export_perdas_csv() -> VisualExportResult:
    export_type = EXPORT_TYPE_JS.get(EXPORT_MODE)
    if export_type is None:
        raise ValueError("ANEEL_PERDAS_EXPORT_MODE deve ser 'summarized' ou 'underlying'.")

    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=HEADLESS)
        page = browser.new_page()
        try:
            _open_report(page)
            payload = page.evaluate(
                """
                async ({ pageName, pageIndex, visualTitle, exportType, maxRows, preferredTypes }) => {
                    const pages = await window.report.getPages();
                    const indexedPage = pageIndex >= 0 && pageIndex < pages.length ? pages[pageIndex] : null;
                    const namedPage = pageName
                        ? pages.find((item) => item.displayName === pageName)
                        : null;
                    const targetPage = namedPage || indexedPage || pages[0];

                    await targetPage.setActive();
                    await new Promise((resolve) => setTimeout(resolve, 4000));

                    const visuals = await targetPage.getVisuals();
                    const normalizedVisualTitle = (visualTitle || "").trim().toLowerCase();
                    let targetVisual = visuals.find(
                        (item) => item.title && item.title.trim().toLowerCase() === normalizedVisualTitle
                    );

                    if (!targetVisual) {
                        const tabularVisuals = visuals.filter((item) => preferredTypes.includes(item.type));
                        tabularVisuals.sort((left, right) => {
                            const leftArea = (left.layout?.width || 0) * (left.layout?.height || 0);
                            const rightArea = (right.layout?.width || 0) * (right.layout?.height || 0);
                            return rightArea - leftArea;
                        });
                        targetVisual = tabularVisuals[0] || visuals[0];
                    }

                    if (!targetVisual) {
                        throw new Error("Nenhum visual disponivel para exportacao.");
                    }

                    const models = window["powerbi-client"].models;
                    const exported = await targetVisual.exportData(
                        models.ExportDataType[exportType],
                        maxRows
                    );

                    return {
                        pageName: targetPage.displayName,
                        visualName: targetVisual.name,
                        visualTitle: targetVisual.title || "",
                        visualType: targetVisual.type || "",
                        csvData: exported.data || ""
                    };
                }
                """,
                {
                    "pageName": PAGE_NAME,
                    "pageIndex": PAGE_INDEX,
                    "visualTitle": VISUAL_TITLE,
                    "exportType": export_type,
                    "maxRows": MAX_ROWS,
                    "preferredTypes": list(TABULAR_VISUAL_TYPES),
                },
            )

            if not payload["csvData"]:
                raise ValueError("O Power BI retornou um CSV vazio.")

            return VisualExportResult(
                page_name=payload["pageName"],
                visual_name=payload["visualName"],
                visual_title=payload["visualTitle"],
                visual_type=payload["visualType"],
                csv_data=payload["csvData"],
            )
        except PlaywrightTimeoutError as exc:
            raise TimeoutError("Tempo esgotado ao carregar o relatorio da ANEEL.") from exc
        finally:
            browser.close()


def run_perdas_import() -> dict[str, Any]:
    export_result = export_perdas_csv()
    archived_file = _archive_export(export_result.csv_data)
    raw_rows = _parse_export_rows(export_result.csv_data)
    if not raw_rows:
        raise ValueError("Erro na extracao ANEEL perdas: CSV vazio")

    conn = get_postgres_connection()
    try:
        distribuidoras = _load_distribuidoras(conn)
        transformed_rows = _transform_rows(raw_rows, distribuidoras)
        if not transformed_rows:
            raise ValueError("Erro na extracao ANEEL perdas: nenhum registro valido para carga")
        upserted = _upsert_perdas(conn, transformed_rows)
    finally:
        conn.close()

    return {
        "report_url": REPORT_URL,
        "page_name": export_result.page_name,
        "page_index": PAGE_INDEX,
        "retention_years": RETENTION_YEARS,
        "min_year_included": datetime.now().year - RETENTION_YEARS,
        "visual_name": export_result.visual_name,
        "visual_title": export_result.visual_title,
        "visual_type": export_result.visual_type,
        "raw_rows": len(raw_rows),
        "loaded_rows": upserted,
        "archived_file": archived_file,
    }


def discover_report_structure_as_json() -> str:
    return json.dumps(discover_report_structure(), ensure_ascii=True, indent=2)
