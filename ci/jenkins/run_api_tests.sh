#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

echo "[1/5] Start dependencies (docker compose)"
cd "$ROOT_DIR/backend"
# IMPORTANT: do not manage jenkins service from inside Jenkins itself.
docker compose -f docker-compose.yaml up -d mysql redis elasticsearch minio

echo "Waiting for MySQL..."
for i in $(seq 1 60); do
  if docker compose -f docker-compose.yaml exec -T mysql mysqladmin ping -h 127.0.0.1 -uroot -p123456 --silent; then
    echo "MySQL is ready"
    break
  fi
  sleep 2
done

echo "Waiting for Elasticsearch..."
for i in $(seq 1 60); do
  code=$(curl -s -o /dev/null -w '%{http_code}' "http://elasticsearch:9200" || true)
  echo "ES check attempt=$i http_code=$code"
  if [ "$code" = "200" ]; then
    echo "Elasticsearch is ready"
    break
  fi
  sleep 2
done

echo "[2/5] Start backend (Spring Boot test profile)"
export DB_USERNAME="${DB_USERNAME:-root}"
export DB_PASSWORD="${DB_PASSWORD:-123456}"
export DB_HOST="${DB_HOST:-mysql}"
export DB_URL="${DB_URL:-jdbc:mysql://mysql:3306/community?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8&allowPublicKeyRetrieval=true}"
export SPRING_DATA_REDIS_HOST="${SPRING_DATA_REDIS_HOST:-redis}"
export SPRING_ELASTICSEARCH_URIS="${SPRING_ELASTICSEARCH_URIS:-http://elasticsearch:9200}"
export APP_MINIO_ENDPOINT="${APP_MINIO_ENDPOINT:-http://minio:9000}"
export BASE_URL="${BASE_URL:-http://localhost:8082}"

chmod +x mvnw
: > "$ROOT_DIR/backend-test.log"
nohup ./mvnw -q -DskipTests spring-boot:run \
  -Dspring-boot.run.profiles=test \
  -Dserver.port=8082 \
  -Dspring-boot.run.arguments="--server.port=8082" \
  > "$ROOT_DIR/backend-test.log" 2>&1 &
echo $! > "$ROOT_DIR/backend.pid"

echo "[3/5] Wait for backend health"
for i in $(seq 1 90); do
  code=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:8082/user/check-username?username=health_check" || true)
  echo "health check attempt=$i http_code=$code"
  if [ "$code" = "200" ]; then
    echo "backend is healthy"
    break
  fi
  if [ -f "$ROOT_DIR/backend.pid" ] && ! kill -0 "$(cat "$ROOT_DIR/backend.pid")" 2>/dev/null; then
    echo "backend process exited early; last log:"
    tail -n 80 "$ROOT_DIR/backend-test.log" || true
    exit 1
  fi
  sleep 2
done

echo "[4/5] Setup venv + install deps"
cd "$ROOT_DIR/auto_test"
python3 -m venv .venv
# shellcheck disable=SC1091
source .venv/bin/activate
python -m pip install -U pip
pip install -r tests/requirements.txt

echo "[5/5] Run integration with xdist + allure"
export TEST_ENV="${TEST_ENV:-test}"
# dash/sh: must use ./.env (with slash), not .env
if [ -f ./.env ]; then
  set -a
  # shellcheck disable=SC1091
  . ./.env
  set +a
fi
python run.py --integration --allure -- ${PYTEST_XDIST:--n auto}

echo "Done. Allure results in auto_test/reports/allure-results"
