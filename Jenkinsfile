pipeline {
    agent any  // This defines the agent where the pipeline will run. 'any' means it can run on any available agent.

    environment {
        DOCKER_IMAGE = 'cgurugc/service-discovery:latest'  // Define the Docker image name with username
        DOCKER_REGISTRY = 'docker.io'  // Define the Docker registry (Docker Hub by default)
        DOCKER_USER = 'cgurugc@gmail.com'  // Docker Hub username (or another registry)
        // DOCKER_PASS is now handled by withCredentials
    }

    stages {
        stage('Checkout Code') {
            steps {
                // Checkout the code from Git repository
                git credentialsId: 'github-token', url: 'https://github.com/ggur/ipic-service-discovery.git', branch: 'main'
            }
        }
		
		


        stage('Build with Maven') {
            steps {
                script {
                    // Run Maven to clean and build the project
                    sh 'mvn clean package'  // This will clean and build your Maven project
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    // Build Docker image from the Dockerfile
                    sh "docker build -t $DOCKER_IMAGE ."
                }
            }
        }

        stage('Login and Push Docker Image') {
            steps {
                // Use withCredentials for secure handling of the Docker password
                // 'docker-creds' should be a 'Secret text' credential in Jenkins
                withCredentials([string(credentialsId: 'docker-creds', variable: 'DOCKER_PASS')]) {
                    script {
                        echo 'Logging in to Docker Hub...'
                        sh "echo \$DOCKER_PASS | docker login -u '${DOCKER_USER}' --password-stdin ${DOCKER_REGISTRY}"
                        echo 'Pushing Docker image...'
                        sh "docker push ${DOCKER_IMAGE}"
                    }
                } // Credentials are automatically revoked here
            }
        }

        stage('Deploy to Docker') {
            steps {
                script {
                    // Run the Docker container from the image
                    sh "docker run -d -p 8761:8761 --name myapp $DOCKER_IMAGE"
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed.'
        }
    }
}