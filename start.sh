#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker nao encontrado no PATH."
  exit 1
fi

if ! docker compose version >/dev/null 2>&1; then
  echo "Docker Compose nao esta disponivel."
  exit 1
fi

PROFILE_ARGS=()
if [[ "${1:-}" == "--tools" ]]; then
  PROFILE_ARGS=(--profile tools)
fi

echo "Iniciando ambiente Docker..."
docker compose "${PROFILE_ARGS[@]}" up -d --build

echo
echo "Ambiente inicializado."
echo "Backend Java: http://localhost:8181/hello"
echo "Backend Python: http://localhost:8000/health"
echo "Documentacao Python: http://localhost:8000/docs"

if [[ "${#PROFILE_ARGS[@]}" -gt 0 ]]; then
  echo "pgAdmin: http://localhost:8080"
  echo "Mongo Express: http://localhost:8081"
else
  echo "Ferramentas opcionais desativadas. Use ./start.sh --tools para subir pgAdmin e Mongo Express."
fi
