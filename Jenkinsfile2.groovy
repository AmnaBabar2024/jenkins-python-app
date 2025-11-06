pipeline {
  agent any

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Setup Python') {
      steps {
        sh '''
          python3 -m venv .venv
          . .venv/bin/activate
          pip install -U pip
          pip install -r requirements.txt || true
          pip install pylint flake8 bandit mypy
          mkdir -p reports
        '''
      }
    }

    stage('Static Analysis') {
      steps {
        // Run tools and write reports into 'reports' folder
        sh '''
          . .venv/bin/activate
          # Pylint (parseable)
          pylint app.py --output-format=parseable > reports/pylint.txt || true

          # Flake8
          flake8 app.py --exit-zero > reports/flake8.txt || true

          # Bandit (JSON)
          bandit -r . -f json -o reports/bandit.json || true

          # Mypy
          mypy app.py --no-color-output > reports/mypy.txt || true
        '''
      }
    }

    stage('Publish Analysis') {
      steps {
        // recordIssues is from Warnings Next Generation plugin
        recordIssues tools: [
          pylint(pattern: 'reports/pylint.txt'),
          flake8(pattern: 'reports/flake8.txt'),
          bandit(pattern: 'reports/bandit.json'),
          mypy(pattern: 'reports/mypy.txt')
        ], aggregatingResults: true
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'reports/**', fingerprint: true
    }
  }
}