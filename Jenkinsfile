pipeline {
    agent any

    options {
        timeout(time: 15, unit: 'MINUTES')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build') {
            steps {
                sh 'mvn -B clean compile'
            }
        }
        stage('Test') {
            steps {
                sh 'mvn -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Package') {
            steps {
                sh 'mvn -B -DskipTests package'
            }
        }
        stage('Run') {
            steps {
                sh 'java -jar target/inventory-service-1.0.0.jar'
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
    }
}
