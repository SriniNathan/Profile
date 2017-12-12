pipeline {
  agent any
  stages {
    stage('Test Stage') {
      steps {
        svn(url: 'https://subversion.assembla.com/svn/calc-service/clients/InsPro/branches/', poll: true)
      }
    }
  }
}