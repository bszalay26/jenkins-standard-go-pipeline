def call(String... args) {
  pipeline {
    agent any
  
    tools {
      go "${args[0]}"
    }
  
    environment {
      GO111MODULES = 'on'
      GOLANG_CI_VERSION = "${args[1]}"
    }
    
    stages {
      stage('Build') {
        steps {
          //sh 'go mod init example/hello'
          sh 'go build'
        }
      }
  
      stage('Test') {
        steps {
          sh 'go test ./... -coverprofile=coverage.txt'
          sh "curl -s https://codecov.io/bash | bash -s -"        
        }
      }
  
      stage('Code Analysis') {
        steps {
          sh "curl -sfL https://install.goreleaser.com/github.com/golangci/golangci-lint.sh | bash -s -- -b $GOPATH/bin ${GOLANG_CI_VERSION}"
          //sh 'golangci-lint run'
        }
      }
  
      stage('Release') {
        when {
            buildingTag()
        }
        environment {
            GITHUB_TOKEN = credentials('GitHub')
        }
        steps {
            sh 'curl -sL https://git.io/goreleaser | bash'
        }
      }
    }
  }
}
