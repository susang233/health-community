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
    // Backend test profile uses these envs (see backend application-test.yaml)
    DB_USERNAME = 'root'
    DB_PASSWORD = '123456'
    // Jenkins 容器里跑后端时，DB_URL 不能用 localhost，要使用 docker-compose 的服务名
    DB_HOST = 'mysql'
    DB_URL = 'jdbc:mysql://mysql:3306/community?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf-8&allowPublicKeyRetrieval=true'

    // auto_test config
    TEST_ENV = 'test'
    PIP_DISABLE_PIP_VERSION_CHECK = '1'

    // Jenkins 容器内部：8080 已被 Jenkins 占用
    // 后端改用 8082，并让 auto_test 的 base_url 指向同一端口
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
          # IMPORTANT: do not start jenkins service from inside Jenkins itself.
          docker compose -f docker-compose.yaml up -d mysql redis elasticsearch minio
        '''
      }
    }

    stage('Start backend (Spring Boot test profile)') {
      when { expression { return params.RUN_INTEGRATION } }
      steps {
        sh '''
          set -euxo pipefail
          cd backend

          # Start backend in background. Logs will be archived if build fails.
          chmod +x mvnw
          nohup ./mvnw -q -DskipTests spring-boot:run -Dspring-boot.run.profiles=test -Dserver.port=8082 -Dspring-boot.run.arguments="--server.port=8082" > ../backend-test.log 2>&1 &

          # Wait until backend is up.
          # NOTE: /actuator/health 在当前环境下可能返回 403，所以使用公开接口 /user/check-username 作为就绪探针。
          for i in $(seq 1 60); do
            code="$(curl -sS -o /dev/null -w \"%{http_code}\" \"http://localhost:8082/user/check-username?username=health_check\" || echo 000)"
            echo "health check attempt=$i http_code=$code"
            if [ "$code" = "200" ]; then
              echo "backend is healthy"
              exit 0
            fi
            sleep 2
          done

          echo "backend did not become healthy in time"
          exit 1
        '''
      }
    }

    stage('Install python deps') {
      steps {
        sh '''
          set -euxo pipefail
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

          # 为避免 Jenkins 端未配置环境变量：优先从仓库里的 auto_test/.env 注入变量
          if [ -f .env ]; then
            set -a
            # shellcheck disable=SC1091
            . .env
            set +a
          fi

          # Credentials for test accounts should be configured in Jenkins as secret env vars.
          # If not configured, tests depending on them may fail.
          : "${USER_USERNAME:=}"
          : "${USER_PASSWORD:=}"
          if [ -z "${USER_USERNAME}" ] || [ -z "${USER_PASSWORD}" ]; then
            echo "Missing USER_USERNAME/USER_PASSWORD in Jenkins env."
            echo "Please set them in this Job (or provide auto_test/.env in the repo, not recommended for secrets)."
            exit 1
          fi

          python run.py --integration --allure -- ${PYTEST_XDIST}
        '''
      }
      post {
        always {
          archiveArtifacts artifacts: 'auto_test/reports/**', allowEmptyArchive: true
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
        cd backend
        # best-effort: do not stop jenkins service from inside Jenkins itself.
        docker compose -f docker-compose.yaml stop mysql redis elasticsearch minio || true
      '''
    }
  }
}

