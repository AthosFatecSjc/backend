#!/usr/bin/env bash
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="$PROJECT_DIR/logs"
LOG_FILE="$LOG_DIR/etl.log"

mkdir -p "$LOG_DIR"
cd "$PROJECT_DIR"

DOCKER_BIN="$(command -v docker)"

echo "==== ETL START $(date '+%Y-%m-%d %H:%M:%S') ====" | tee -a "$LOG_FILE"

if "$DOCKER_BIN" compose run --rm backend-java java -jar app.jar etl >> "$LOG_FILE" 2>&1; then
  echo "==== ETL SUCCESS $(date '+%Y-%m-%d %H:%M:%S') ====" | tee -a "$LOG_FILE"
else
  echo "==== ETL FAILED $(date '+%Y-%m-%d %H:%M:%S') ====" | tee -a "$LOG_FILE"
  exit 1
fi