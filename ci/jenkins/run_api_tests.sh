#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

echo "[1/5] Start dependencies (docker compose)"
cd "$ROOT_DIR/backend"
# IMPORTANT: do not manage jenkins service from inside Jenkins itself.
docker compose -f docker-compose.yaml up -d mysql redis elasticsearch minio

echo "[2/5] Start backend (Spring Boot test profile)"
nohup mvn -q -DskipTests spring-boot:run -Dspring-boot.run.profiles=test > "$ROOT_DIR/backend-test.log" 2>&1 &

echo "[3/5] Wait for backend health"
for i in $(seq 1 60); do
  if curl -fsS "http://localhost:8080/actuator/health" >/dev/null; then
    echo "backend is healthy"
    break
  fi
  sleep 2
done

echo "[4/5] Setup venv + install deps"
cd "$ROOT_DIR/auto_test"
python3 -m venv .venv
source .venv/bin/activate
python -m pip install -U pip
pip install -r tests/requirements.txt

echo "[5/5] Run integration with xdist + allure"
export TEST_ENV="${TEST_ENV:-test}"
python run.py --integration --allure -- ${PYTEST_XDIST:--n auto}

echo "Done. Allure results in auto_test/reports/allure-results"

