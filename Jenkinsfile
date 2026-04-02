pipeline {
  agent any

  environment {
    CLUSTER_NAME = "demo-eks"
    REGION = "ap-south-1"
    DOCKER_IMAGE = "yourdockerhub/myapp"
  }

  stages {

    stage('Clone Code') {
      steps {
        git 'https://github.com/<your-username>/my-app.git'
      }
    }

    stage('Build') {
      steps {
        sh 'mvn clean package'
      }
    }

    stage('SonarQube') {
      steps {
        sh 'mvn sonar:sonar'
      }
    }

    stage('Docker Build') {
      steps {
        sh 'docker build -t $DOCKER_IMAGE:$BUILD_NUMBER .'
      }
    }

    stage('Docker Push') {
      steps {
        sh '''
        docker login -u yourdockerhub -p yourpassword
        docker push $DOCKER_IMAGE:$BUILD_NUMBER
        '''
      }
    }

    stage('Create EKS') {
      steps {
        sh '''
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
        sh 'kubectl get svc'
      }
    }
  }
}
