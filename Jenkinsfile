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

    stage('Recreate EKS Cluster') {
      steps {
        sh '''
        echo "Deleting existing cluster (if any)..."

        /usr/local/bin/eksctl delete cluster \
        --name $CLUSTER_NAME \
        --region $REGION || true

        echo "Waiting for CloudFormation stack deletion..."

        aws cloudformation wait stack-delete-complete \
          --stack-name eksctl-$CLUSTER_NAME-cluster \
          --region $REGION || true

        echo "Creating new EKS cluster..."

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
        echo "Deploying latest image: $DOCKER_IMAGE:$BUILD_NUMBER"

        /usr/local/bin/kubectl set image deployment/myapp myapp=$DOCKER_IMAGE:$BUILD_NUMBER || true

        /usr/local/bin/kubectl apply -f deployment.yaml
        /usr/local/bin/kubectl apply -f service.yaml

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


