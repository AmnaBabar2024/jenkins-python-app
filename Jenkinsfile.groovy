pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Clones the repository automatically
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Running Python App...'
                bat 'python app.py'  // use 'sh' if on Linux agent
            }
        }

    }

    post {
        success {
            echo '✅ Build completed successfully!'
        }
        failure {
            echo '❌ Build failed!'
        }
    }
}