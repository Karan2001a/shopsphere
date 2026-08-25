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

         stage('Check Docker') {
           steps {
               bat '"C:\\Users\\bhoja\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" --version'
               bat '"C:\\Users\\bhoja\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" compose version'
           }
       }

       stage('Docker Build') {
           steps {
               bat '"C:\\Users\\bhoja\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" compose build'
           }
       }

       stage('Docker Deploy') {
           steps {
               bat '"C:\\Users\\bhoja\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" compose down'
               bat '"C:\\Users\\bhoja\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" compose up -d'
           }
       }

       stage('Verify Containers') {
           steps {
               bat '"C:\\Users\\bhoja\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe" compose ps'
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
