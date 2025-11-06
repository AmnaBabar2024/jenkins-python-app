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
                bat '''
                    python -m venv .venv
                    call .venv\\Scripts\\activate
                '''
            }
        }

        stage('Static Analysis') {
            steps {
                bat '''
                    call .venv\\Scripts\\activate
                    pylint app.py || true
                    flake8 app.py || true
                    bandit -r . || true
                    mypy app.py || true
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