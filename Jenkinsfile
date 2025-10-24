pipeline {
  agent any                     // run on any available agent
  environment {
    MAVEN_OPTS = "-Xmx1024m"
    DOCKER_REGISTRY = "myregistry.example.com" // optional
  }
  options {
    timestamps()
    ansiColor('xterm')
    buildDiscarder(logRotator(daysToKeepStr: '14', numToKeepStr: '10'))
  }
  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      when { expression { fileExists('pom.xml') } } // Maven example
      steps {
        sh 'mvn -B clean package'   // use bat 'mvn -B ...' on Windows agents
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
      }
    }

    stage('Unit Tests') {
      steps {
        sh 'mvn test'
        junit 'target/surefire-reports/*.xml'
      }
    }

    stage('Docker Build & Push') {
      when { expression { fileExists('Dockerfile') } }
      environment {
        REGISTRY_CREDENTIALS = credentials('docker-registry-id') // credential id
      }
      steps {
        sh '''
          docker build -t $DOCKER_REGISTRY/myapp:${BUILD_NUMBER} .
          echo "$REGISTRY_CREDENTIALS_PSW" | docker login -u "$REGISTRY_CREDENTIALS_USR" --password-stdin $DOCKER_REGISTRY
          docker push $DOCKER_REGISTRY/myapp:${BUILD_NUMBER}
        '''
      }
    }

    stage('Deploy (dev)') {
      when { branch 'main' }
      steps {
        echo "Deploying to dev environment..."
        // add your deploy steps, e.g. kubectl, ssh, etc.
      }
    }
  }

  post {
    success { echo "Build succeeded: ${env.BUILD_URL}" }
    unstable { echo "Build unstable" }
    failure { mail to: 'team@example.com', subject: "Build failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}", body: "See ${env.BUILD_URL}" }
    always { cleanWs() }
  }
}
