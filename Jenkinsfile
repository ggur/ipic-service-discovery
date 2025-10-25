pipeline {
    agent any  // This defines the agent where the pipeline will run. 'any' means it can run on any available agent.

    environment {
        DOCKER_IMAGE_NAME = 'cgurugc/service-discovery'
        DOCKER_REGISTRY = 'docker.io'  // Define the Docker registry (Docker Hub by default)
        DOCKER_USER = 'cgurugc@gmail.com'  // Docker Hub username (or another registry)
        HELM_RELEASE_NAME = 'ipic-service-discovery' // The name for our Helm deployment
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
                    def imageTag = "build-${env.BUILD_NUMBER}"
                    // Build Docker image from the Dockerfile
                    sh "docker build -t ${DOCKER_IMAGE_NAME}:${imageTag} ."
                }
            }
        }

        stage('Login and Push Docker Image') {
            steps {
                // Use withCredentials for secure handling of the Docker password
                // 'docker-creds' should be a 'Secret text' credential in Jenkins
                withCredentials([string(credentialsId: 'docker-creds', variable: 'DOCKER_PASS')]) {
                    script {
                        def imageTag = "build-${env.BUILD_NUMBER}"
                        echo 'Logging in to Docker Hub...'
                        sh "echo \$DOCKER_PASS | docker login -u '${DOCKER_USER}' --password-stdin ${DOCKER_REGISTRY}"
                        echo 'Pushing Docker image...'
                        sh "docker push ${DOCKER_IMAGE_NAME}:${imageTag}"
                    }
                } // Credentials are automatically revoked here
            }
        }

        stage('Deploy with Helm') {
            steps {
                // Use withKubeconfig to securely provide cluster credentials
                withKubeconfig([credentialsId: 'kube-config-docker-desktop']) {
                    script {
                        def imageTag = "build-${env.BUILD_NUMBER}"
                        echo "Deploying Helm chart for image ${DOCKER_IMAGE_NAME}:${imageTag}"
                        // Use 'helm upgrade --install' to either install or update the release
                        // We set the image tag dynamically from our build
                        sh """helm upgrade --install ${HELM_RELEASE_NAME} ./helm \
                              --set image.tag=${imageTag}"""
                    }
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
