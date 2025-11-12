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
                    pip install pylint flake8 bandit mypy
                '''
            }
        }

        stage('Static Analysis') {
            steps {
                bat '''
                    mkdir reports 2>nul
                    call .venv\\Scripts\\activate
                    
                    pylint app.py > reports\\pylint.txt || true
                    flake8 app.py > reports\\flake8.txt || true
                    bandit -r . -f json -o reports\\bandit.json || true
                    mypy app.py > reports\\mypy.txt || true
                '''
            }
        }

        // stage('Publish Analysis') {
        //     steps {
        //       recordIssues tools: [
        //         flake8(pattern: 'reports/flake8.txt')
        //     ]
        //   }
        // }
    }

    post {
        always {
            archiveArtifacts artifacts: 'reports/**', fingerprint: true
        }
    }
}