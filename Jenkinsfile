pipeline {
    agent any

    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build User Service') {
            steps {
                dir('user-service') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Product Service') {
            steps {
                dir('productservice') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Order Service') {
            steps {
                dir('order-service') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Notification Service') {
            steps {
                dir('notificationservice') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build API Gateway') {
            steps {
                dir('api-gateway') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Docker Build') {
            steps {
                bat 'docker compose build'
            }
        }

        stage('Docker Deploy') {
            steps {
                bat 'docker compose down'
                bat 'docker compose up -d'
            }
        }

        stage('Verify Containers') {
            steps {
                bat 'docker compose ps'
            }
        }
    }

    post {
        success {
            echo 'ShopSphere pipeline completed successfully.'
        }

        failure {
            echo 'ShopSphere pipeline failed. Check the stage logs.'
        }
    }
}