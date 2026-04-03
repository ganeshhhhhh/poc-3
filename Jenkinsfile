pipeline {
  agent any

  parameters {
    booleanParam(name: 'CREATE_EKS', defaultValue: false, description: 'Create EKS Cluster')
  }

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
      when {
        expression { params.CREATE_EKS == true }
      }
      steps {
        sh '''
        echo "Skipping EKS creation (already exists)"
        /usr/local/bin/eksctl get cluster --name $CLUSTER_NAME
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
        echo "Deploying latest image: $DOCKER_IMAGE:$BUILD_NUMBER"

        /usr/local/bin/kubectl set image deployment/myapp myapp=$DOCKER_IMAGE:$BUILD_NUMBER

        /usr/local/bin/kubectl rollout status deployment myapp
        '''
      }
    }

    stage('Verify') {
      steps {
        sh '''
        echo "Pods:"
        /usr/local/bin/kubectl get pods -o wide

        echo "Services:"
        /usr/local/bin/kubectl get svc

        echo "Running Image:"
        /usr/local/bin/kubectl get deployment myapp -o=jsonpath="{.spec.template.spec.containers[0].image}"
        echo ""
        '''
      }
    }
  }

  post {
    success {
      echo "Deployment Successful with image $DOCKER_IMAGE:$BUILD_NUMBER"
    }
    failure {
      echo "Deployment Failed"
    }
  }
}
