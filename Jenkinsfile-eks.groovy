pipeline {
    agent any  // This defines the agent where the pipeline will run. 'any' means it can run on any available agent.

    parameters {
        // Add a parameter to choose the deployment environment
        choice(name: 'ENVIRONMENT', choices: ['dev', 'prod'], description: 'Select the environment to deploy to')
    }

    environment {
        // AWS & EKS Configuration - Replace placeholders with your actual values
        AWS_ACCOUNT_ID    = '440744252423' // <-- REPLACE with your AWS Account ID
        AWS_REGION        = 'us-east-2'    // <-- REPLACE with your AWS region
        EKS_CLUSTER_NAME  = 'my-eks-cluster' // <-- REPLACE with your EKS cluster name
        ECR_REPOSITORY    = 'ipic-service-discovery'
        ECR_REGISTRY      = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        DOCKER_IMAGE_NAME = "${ECR_REGISTRY}/${ECR_REPOSITORY}"
        HELM_RELEASE_NAME = 'ipic-service-discovery' // The name for our Helm deployment
        IMAGE_TAG = "build-${env.BUILD_NUMBER}" // Define the image tag once for all stages
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
                    sh "docker build -t ${DOCKER_IMAGE_NAME}:${IMAGE_TAG} ."
                }
            }
        }

        stage('Login and Push Docker Image') {
            // This stage uses AWS credentials to log in to Amazon ECR and push the image.
            // 'aws-credentials' should be a Jenkins credential of type 'AWS Credentials'.
            steps {
                withAWS(credentials: 'aws-credentials', region: AWS_REGION) {
                    script {
                        echo "Logging in to Amazon ECR..."
                        sh "aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}"
                        echo 'Pushing Docker image...'
                        sh "docker push ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                    }
                }
            }
        }

        stage('Lint Helm Chart') {
            steps {
                script {
                    echo "Linting Helm chart..."
                    // This validates the syntax of your Helm chart before deployment
                    sh "helm lint helm/ipic-service-discovery"
                }
            }
        }

        stage('Deploy with Helm') {
            steps {
                // Use AWS credentials to configure kubectl for the EKS cluster
                withAWS(credentials: 'aws-credentials', region: AWS_REGION) {
                    script {
                        echo "Configuring kubectl for EKS cluster '${EKS_CLUSTER_NAME}'..."
                        sh "aws eks update-kubeconfig --name ${EKS_CLUSTER_NAME} --region ${AWS_REGION}"

                        echo "Deploying Helm chart for image ${DOCKER_IMAGE_NAME}:${IMAGE_TAG}"
                        // Use 'helm upgrade --install' to either install or update the release
                        // We set the image tag dynamically from our build
                        // Use the -f flag to specify the environment-specific values file
                        sh """helm upgrade --install ${HELM_RELEASE_NAME} ./helm/ipic-service-discovery \
                              --set image.repository=${DOCKER_IMAGE_NAME},image.tag=${IMAGE_TAG} \
                              -f ./helm/ipic-service-discovery/values-${params.ENVIRONMENT}.yaml"""
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
