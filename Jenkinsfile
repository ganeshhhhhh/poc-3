pipeline {
  agent any

  environment {
    CLUSTER_NAME = "demo-eks"
    REGION = "ap-south-1"
    DOCKER_IMAGE = "ganeshhhhhh/myapp"
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
          echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin
          /usr/bin/docker push $DOCKER_IMAGE:$BUILD_NUMBER
          '''
        }
      }
    }

    stage('Create EKS') {
      steps {
        sh '''
        eksctl get cluster --name $CLUSTER_NAME || \
        eksctl create cluster \
        --name $CLUSTER_NAME \
        --region $REGION \
        --nodes 2
        '''
      }
    }

    stage('Configure Kube') {
      steps {
        sh '''
        aws eks update-kubeconfig \
        --region $REGION \
        --name $CLUSTER_NAME
        '''
      }
    }

    stage('Update Image') {
      steps {
        sh '''
        sed -i "s|image:.*|image: $DOCKER_IMAGE:$BUILD_NUMBER|" k8s/deployment.yaml
        '''
      }
    }

    stage('Deploy') {
      steps {
        sh 'kubectl apply -f k8s/'
      }
    }

    stage('Verify') {
      steps {
        sh '''
        kubectl get pods
        kubectl get svc
        '''
      }
    }
  }
}
