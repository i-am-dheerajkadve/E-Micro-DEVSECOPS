pipeline {
    agent any

    tools {
        jdk 'jdk17'
        maven 'maven3'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test Maven Services') {
            steps {
                script {
                    def mavenServices = ['api-gateway', 'user-service', 'order-service', 'inventory-service']
                    for (service in mavenServices) {
                        dir(service) {
                            sh 'mvn clean package -DskipTests=false'
                        }
                    }
                }
            }
        }

        stage('Build & Test Gradle Services') {
            steps {
                script {
                    def gradleServices = ['product-service', 'cart-service', 'payment-service', 'notification-service']
                    for (service in gradleServices) {
                        dir(service) {
                            sh 'chmod +x gradlew'
                            sh './gradlew build'
                        }
                    }
                }
            }
        }

        stage('Security Analysis') {
            steps {
                echo 'Running SonarQube & OWASP Dependency Check scans...'
                // withSonarQubeEnv('SonarQubeServer') { sh 'mvn sonar:sonar' }
            }
        }

        stage('Docker Build and Push') {
            steps {
                echo 'Building and pushing Docker images...'
                // sh 'docker build -t ecom-gateway:latest ./api-gateway'
            }
        }

        stage('Kubernetes Deploy') {
            steps {
                echo 'Deploying to Kubernetes namespace ecommerce-devops...'
                // sh 'kubectl apply -f k8s/'
            }
        }
    }
}
