pipeline {
  agent any

  options {
    buildDiscarder(logRotator(numToKeepStr: '30'))
  }

  parameters {
    booleanParam(name: 'RUN_UNIT', defaultValue: true, description: 'Run unit tests (no backend required)')
    booleanParam(name: 'RUN_INTEGRATION', defaultValue: true, description: 'Run integration tests (start backend + deps)')
    string(name: 'PYTEST_XDIST', defaultValue: '-n auto', description: 'pytest-xdist args, e.g. "-n auto" or ""')
  }

  environment {
    // Backend 跑在 Jenkins 容器内：依赖用 docker-compose 服务名，不能用 localhost
    DB_USERNAME = 'root'
    DB_PASSWORD = '123456'
    DB_HOST = 'mysql'
    DB_URL = 'jdbc:mysql://mysql:3306/community?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8&allowPublicKeyRetrieval=true'
    SPRING_DATA_REDIS_HOST = 'redis'
    SPRING_ELASTICSEARCH_URIS = 'http://elasticsearch:9200'
    APP_MINIO_ENDPOINT = 'http://minio:9000'

    TEST_ENV = 'test'
    PIP_DISABLE_PIP_VERSION_CHECK = '1'

    // Jenkins 自身占用 8080，后端改用 8082
    BASE_URL = 'http://localhost:8082'
  }

  stages {
    stage('Jenkinsfile Marker') {
      steps {
        sh '''
          set -euxo pipefail
          echo "JENKINSFILE_HEALTH_COMMUNITY_AUTO_TEST_V1"
          echo "Workspace: $(pwd)"
          ls -la
        '''
      }
    }

    stage('Checkout') {
      steps {
        deleteDir()
        checkout([
          $class: 'GitSCM',
          branches: [[name: '*/main']],
          userRemoteConfigs: [[url: 'https://github.com/susang233/health-community.git']]
        ])
      }
    }

    stage('Start dependencies (MySQL/Redis/MinIO/ES)') {
      when { expression { return params.RUN_INTEGRATION } }
      steps {
        sh '''
          set -euxo pipefail
          cd backend
          # IMPORTANT: do not start/stop jenkins service from inside Jenkins itself.
          docker compose -f docker-compose.yaml up -d mysql redis elasticsearch minio

          echo "Waiting for MySQL..."
          for i in $(seq 1 60); do
            if docker compose -f docker-compose.yaml exec -T mysql mysqladmin ping -h 127.0.0.1 -uroot -p123456 --silent; then
              echo "MySQL is ready"
              break
            fi
            if [ "$i" -eq 60 ]; then
              echo "MySQL not ready in time"
              exit 1
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
            if [ "$i" -eq 60 ]; then
              echo "Elasticsearch not ready in time"
              exit 1
            fi
            sleep 2
          done
        '''
      }
    }

    stage('Start backend (Spring Boot test profile)') {
      when { expression { return params.RUN_INTEGRATION } }
      steps {
        sh '''
          set -euxo pipefail
          cd backend

          chmod +x mvnw
          : > ../backend-test.log
          nohup ./mvnw -q -DskipTests spring-boot:run \
            -Dspring-boot.run.profiles=test \
            -Dserver.port=8082 \
            -Dspring-boot.run.arguments="--server.port=8082" \
            > ../backend-test.log 2>&1 &
          echo $! > ../backend.pid

          # 公开接口作为就绪探针（不要用 /actuator/health）
          for i in $(seq 1 90); do
            code=$(curl -s -o /dev/null -w '%{http_code}' "http://localhost:8082/user/check-username?username=health_check" || true)
            echo "health check attempt=$i http_code=$code"
            if [ "$code" = "200" ]; then
              echo "backend is healthy"
              exit 0
            fi
            # 若 mvn 进程已退出，尽早失败并打印日志
            if [ -f ../backend.pid ] && ! kill -0 "$(cat ../backend.pid)" 2>/dev/null; then
              echo "backend process exited early; last log:"
              tail -n 80 ../backend-test.log || true
              exit 1
            fi
            sleep 2
          done

          echo "backend did not become healthy in time; last log:"
          tail -n 80 ../backend-test.log || true
          exit 1
        '''
      }
    }

    stage('Install python deps') {
      steps {
        sh '''
          set -euxo pipefail
          if ! command -v python3 >/dev/null 2>&1; then
            apt-get update
            apt-get install -y --no-install-recommends python3 python3-venv python3-pip
          fi
          cd auto_test
          python3 -m venv .venv
          . .venv/bin/activate
          python -m pip install -U pip
          pip install -r tests/requirements.txt
        '''
      }
    }

    stage('Unit tests') {
      when { expression { return params.RUN_UNIT } }
      steps {
        sh '''
          set -euxo pipefail
          cd auto_test
          . .venv/bin/activate
          python run.py --unit
        '''
      }
    }

    stage('Integration tests (xdist + Allure results)') {
      when { expression { return params.RUN_INTEGRATION } }
      steps {
        sh '''
          set -euxo pipefail
          cd auto_test
          . .venv/bin/activate

          if [ -f .env ]; then
            set -a
            # shellcheck disable=SC1091
            . .env
            set +a
          fi

          : "${USER_USERNAME:=}"
          : "${USER_PASSWORD:=}"
          if [ -z "${USER_USERNAME}" ] || [ -z "${USER_PASSWORD}" ]; then
            echo "Missing USER_USERNAME/USER_PASSWORD in Jenkins env."
            echo "Please set them in this Job (or provide auto_test/.env)."
            exit 1
          fi

          python run.py --integration --allure -- ${PYTEST_XDIST}
        '''
      }
      post {
        always {
          archiveArtifacts artifacts: 'auto_test/reports/**,backend-test.log', allowEmptyArchive: true
        }
      }
    }

    stage('Generate Allure HTML (optional)') {
      when { expression { return params.RUN_INTEGRATION } }
      steps {
        sh '''
          set +e
          if command -v allure >/dev/null 2>&1; then
            cd auto_test
            allure generate -c reports/allure-results -o reports/allure-report
          else
            echo "allure CLI not found; skipping HTML generation (results are archived)."
          fi
          true
        '''
      }
      post {
        always {
          archiveArtifacts artifacts: 'auto_test/reports/allure-report/**', allowEmptyArchive: true
        }
      }
    }
  }

  post {
    always {
      sh '''
        set +e
        if [ -f backend.pid ]; then
          kill "$(cat backend.pid)" 2>/dev/null || true
        fi
        cd backend
        # IMPORTANT: do not stop jenkins service from inside Jenkins itself.
        docker compose -f docker-compose.yaml stop mysql redis elasticsearch minio || true
      '''
    }
  }
}
