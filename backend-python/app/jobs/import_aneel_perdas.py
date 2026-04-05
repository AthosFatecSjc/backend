import argparse
import json

from app.etl.aneel_perdas import discover_report_structure, run_perdas_import


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

    result = run_perdas_import()
    print(json.dumps(result, ensure_ascii=True, indent=2, default=str))


if __name__ == "__main__":
    main()
