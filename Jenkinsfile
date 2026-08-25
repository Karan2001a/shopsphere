pipeline {
    agent any

    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9'
    }

    environment {
        DOCKER_EXE = 'C:\\Users\\bhoja\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe'
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

        stage('Check Docker') {
            steps {
                bat '"%DOCKER_EXE%" --version'
                bat '"%DOCKER_EXE%" compose version'
            }
        }

        stage('Docker Build') {
            steps {
                withCredentials([
                    string(credentialsId: 'MYSQL_ROOT_PASSWORD', variable: 'MYSQL_ROOT_PASSWORD'),
                    string(credentialsId: 'JWT_SECRET', variable: 'JWT_SECRET'),
                    string(credentialsId: 'MAIL_USERNAME', variable: 'MAIL_USERNAME'),
                    string(credentialsId: 'MAIL_PASSWORD', variable: 'MAIL_PASSWORD')
                ]) {
                    bat '"%DOCKER_EXE%" compose build'
                }
            }
        }

        stage('Docker Deploy') {
            steps {
                withCredentials([
                    string(credentialsId: 'MYSQL_ROOT_PASSWORD', variable: 'MYSQL_ROOT_PASSWORD'),
                    string(credentialsId: 'JWT_SECRET', variable: 'JWT_SECRET'),
                    string(credentialsId: 'MAIL_USERNAME', variable: 'MAIL_USERNAME'),
                    string(credentialsId: 'MAIL_PASSWORD', variable: 'MAIL_PASSWORD')
                ]) {
                    bat '"%DOCKER_EXE%" compose down'
                    bat '"%DOCKER_EXE%" compose up -d'
                }
            }
        }

        stage('Verify Containers') {
            steps {
                bat '"%DOCKER_EXE%" compose ps'
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
