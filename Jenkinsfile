pipeline {
  agent any

  environment {
    CLUSTER_NAME = "demo-eks"
    REGION = "ap-south-1"
    DOCKER_IMAGE = "ockerlitud/myapp"
  }

  stages {

    stage('Clone Code') {
      steps {
        git branch: 'main', url: 'https://github.com/ganeshhhhhh/poc-3.git'
      }
    }

    stage('Build') {
      steps {
        sh 'mvn clean package'
      }
    }

    stage('SonarQube') {
      steps {
        withSonarQubeEnv('sonar') {
          sh 'mvn clean verify sonar:sonar'
        }
      }
    }

    stage('Docker Build') {
      steps {
        sh '/usr/bin/docker build -t $DOCKER_IMAGE:$BUILD_NUMBER .'
      }
    }

    stage('Docker Push') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'docker-creds',
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        )]) {
          sh '''
          echo $DOCKER_PASS | /usr/bin/docker login -u $DOCKER_USER --password-stdin
          /usr/bin/docker push $DOCKER_IMAGE:$BUILD_NUMBER
          '''
        }
      }
    }

    stage('Create EKS') {
      steps {
        sh '''
        /usr/local/bin/eksctl get cluster --name $CLUSTER_NAME || \
        /usr/local/bin/eksctl create cluster \
        --name $CLUSTER_NAME \
        --region $REGION \
        --nodes 2
        '''
      }
    }

    stage('Configure Kube') {
      steps {
        sh '''
        /usr/local/bin/aws eks update-kubeconfig \
        --region $REGION \
        --name $CLUSTER_NAME
        '''
      }
    }

    stage('Deploy') {
      steps {
        sh '''
        /usr/local/bin/kubectl apply -f deployment.yaml
        /usr/local/bin/kubectl apply -f service.yaml
        '''
      }
    }

    stage('Verify') {
      steps {
        sh '''
        /usr/local/bin/kubectl get pods
        /usr/local/bin/kubectl get svc
        '''
      }
    }
  }
}
