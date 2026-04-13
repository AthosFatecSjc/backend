import argparse
import json
import sys
from uuid import uuid4

from app.etl.aneel_perdas import discover_report_structure, run_perdas_import
from app.services.internal_log_client import InternalLogClient


def _sanitize_error_message(error: Exception) -> str:
    raw = (
        str(error)
        .replace("\n", " ")
        .replace("\r", " ")
        .replace("\t", " ")
        .replace(";", ",")
        .strip()
    )
    if not raw:
        raw = error.__class__.__name__
    return raw[:300]


def _emit_log_best_effort(log_client: InternalLogClient, **payload: str) -> None:
    try:
        log_client.emit(**payload)
    except Exception as exc:
        print(
            f"[warn] falha ao emitir log interno ANEEL: {_sanitize_error_message(exc)}",
            file=sys.stderr,
        )


def main() -> None:
    parser = argparse.ArgumentParser(
        description="Importa dados trimestrais de perdas de energia da ANEEL via Power BI."
    )
    parser.add_argument(
        "--discover",
        action="store_true",
        help="Lista paginas e visuais do relatorio para facilitar a configuracao.",
    )
    args = parser.parse_args()

    if args.discover:
        print(json.dumps(discover_report_structure(), ensure_ascii=True, indent=2))
        return

    execution_id = str(uuid4())
    log_client = InternalLogClient()

    _emit_log_best_effort(
        log_client,
        event="ANEEL_EXTRACTION_START",
        result="SUCCESS",
        description="Inicio da rotina automatica de extracao ANEEL perdas.",
        metadata="routine=import_aneel_perdas;stage=start",
        target_ref=execution_id,
    )

    try:
        result = run_perdas_import()
        _emit_log_best_effort(
            log_client,
            event="ANEEL_EXTRACTION_SUCCESS",
            result="SUCCESS",
            description="Rotina automatica de extracao ANEEL perdas concluida com sucesso.",
            metadata=(
                "routine=import_aneel_perdas;stage=completed;"
                f"raw_rows={result.get('raw_rows', 0)};"
                f"loaded_rows={result.get('loaded_rows', 0)}"
            ),
            target_ref=execution_id,
        )
        result["execution_id"] = execution_id
    except Exception as exc:
        _emit_log_best_effort(
            log_client,
            event="ANEEL_EXTRACTION_FAIL",
            result="FAIL",
            description="Falha na rotina automatica de extracao ANEEL perdas.",
            metadata=(
                "routine=import_aneel_perdas;stage=failed;"
                f"error={_sanitize_error_message(exc)}"
            ),
            target_ref=execution_id,
        )
        raise

    print(json.dumps(result, ensure_ascii=True, indent=2, default=str))


if __name__ == "__main__":
    main()
