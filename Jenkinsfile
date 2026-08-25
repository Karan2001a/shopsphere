pipeline {
    agent any

    tools {
        jdk 'JDK-21'
        maven 'Maven-3.9'
    }

    environment {
        DOCKER_EXE = 'C:\\Users\\bhoja\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin\\docker.exe'
        COMPOSE_EXE = 'C:\\Users\\bhoja\\.docker\\cli-plugins\\docker-compose.exe'
    }

    stages {

        // ==========================================
        // 1. CHECKOUT SOURCE CODE
        // ==========================================
        stage('Checkout') {
            steps {
                echo 'Checking out ShopSphere source code...'
                checkout scm
            }
        }

        // ==========================================
        // 2. BUILD USER SERVICE
        // ==========================================
        stage('Build User Service') {
            steps {
                echo 'Building User Service...'

                dir('user-service') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        // ==========================================
        // 3. BUILD PRODUCT SERVICE
        // ==========================================
        stage('Build Product Service') {
            steps {
                echo 'Building Product Service...'

                dir('productservice') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        // ==========================================
        // 4. BUILD ORDER SERVICE
        // ==========================================
        stage('Build Order Service') {
            steps {
                echo 'Building Order Service...'

                dir('order-service') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        // ==========================================
        // 5. BUILD NOTIFICATION SERVICE
        // ==========================================
        stage('Build Notification Service') {
            steps {
                echo 'Building Notification Service...'

                dir('notificationservice') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        // ==========================================
        // 6. BUILD API GATEWAY
        // ==========================================
        stage('Build API Gateway') {
            steps {
                echo 'Building API Gateway...'

                dir('api-gateway') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        // ==========================================
        // 7. VERIFY DOCKER
        // ==========================================
        stage('Check Docker') {
            steps {
                echo 'Checking Docker installation...'

                bat '"%DOCKER_EXE%" --version'

                echo 'Checking Docker Compose...'

                bat '"%COMPOSE_EXE%" version'
            }
        }

        // ==========================================
        // 8. BUILD DOCKER IMAGES
        // ==========================================
        stage('Docker Build') {
            steps {

                echo 'Building ShopSphere Docker images...'

                withCredentials([

                    string(
                        credentialsId: 'MYSQL_ROOT_PASSWORD',
                        variable: 'MYSQL_ROOT_PASSWORD'
                    ),

                    string(
                        credentialsId: 'JWT_SECRET',
                        variable: 'JWT_SECRET'
                    ),

                    string(
                        credentialsId: 'MAIL_USERNAME',
                        variable: 'MAIL_USERNAME'
                    ),

                    string(
                        credentialsId: 'MAIL_PASSWORD',
                        variable: 'MAIL_PASSWORD'
                    )

                ]) {

                    bat '"%COMPOSE_EXE%" build'
                }
            }
        }

        // ==========================================
        // 9. DEPLOY WITH DOCKER COMPOSE
        // ==========================================
        stage('Docker Deploy') {
            steps {

                echo 'Deploying ShopSphere containers...'

                withCredentials([

                    string(
                        credentialsId: 'MYSQL_ROOT_PASSWORD',
                        variable: 'MYSQL_ROOT_PASSWORD'
                    ),

                    string(
                        credentialsId: 'JWT_SECRET',
                        variable: 'JWT_SECRET'
                    ),

                    string(
                        credentialsId: 'MAIL_USERNAME',
                        variable: 'MAIL_USERNAME'
                    ),

                    string(
                        credentialsId: 'MAIL_PASSWORD',
                        variable: 'MAIL_PASSWORD'
                    )

                ]) {

                    echo 'Stopping previous ShopSphere containers...'

                    bat '"%COMPOSE_EXE%" down'

                    echo 'Starting new ShopSphere containers...'

                    bat '"%COMPOSE_EXE%" up -d'
                }
            }
        }

        // ==========================================
        // 10. VERIFY CONTAINERS
        // ==========================================
        stage('Verify Containers') {
            steps {

                echo 'Checking ShopSphere containers...'

                bat '"%COMPOSE_EXE%" ps'
            }
        }
    }

    // ==========================================
    // PIPELINE RESULT
    // ==========================================
    post {

        success {
            echo '=========================================='
            echo 'SHOPSPHERE CI/CD PIPELINE SUCCESSFUL'
            echo '=========================================='
        }

        failure {
            echo '=========================================='
            echo 'SHOPSPHERE CI/CD PIPELINE FAILED'
            echo 'Check the failed stage in Console Output.'
            echo '=========================================='
        }

        always {
            echo 'ShopSphere Jenkins pipeline finished.'
        }
    }
}
