pipeline {
    agent any

    tools {
        maven 'Maven 3.8.7'
        jdk 'JDK 17'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/tathienbao/movie-booking.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Test') {
            environment {
                JWT_SECRET_KEY = 'test-secret-key-for-automated-testing-min-48-chars-long-secure'
            }
            steps {
                sh 'mvn test'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t movie-booking-api:${BUILD_NUMBER} .'
                sh 'docker tag movie-booking-api:${BUILD_NUMBER} movie-booking-api:latest'
            }
        }

        stage('Run Integration Test') {
            steps {
                sh 'docker run -d -p 8081:8080 -e JWT_SECRET_KEY="test-secret-key-for-automated-testing-min-48-chars-long-secure" --name test-container movie-booking-api:latest'
                sh 'sleep 10'
                sh 'curl -f http://localhost:8081/api/movies || exit 1'
                sh 'docker stop test-container && docker rm test-container'
            }
        }
    }

    post {
        always {
            cleanWs()
        }
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
