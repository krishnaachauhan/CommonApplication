@Library('jenkins-shared-library@main') _

pipeline {
    agent {
        label 'Jenkins-Agent'
    }
    tools {
        jdk 'jdk17'
        maven 'maven3'
    }
    environment {
        GIT_REPO_URL = 'https://github.com/adminacute/Enfinity-CommonApplication.git'
        GIT_CREDENTIALS = 'jennkins-to-github'
        BRANCH = 'devops_main'
        EMAIL_RECIPIENT = 'jaid.shaikh@acuteinformatics.in,krishna.chauhan@acuteinformatics.in,jayendra.sathwara@acuteinformatics.in,sajid.sachawala@acuteinformatics.in,pratiksha.bansod@acuteinformatics.in'
    }

    stages {
        stage('Cleanup Workspace') {
            steps {
                script {
                    cleanWs()
                }
            }
        }

        stage('Checkout') {
            steps {
                script {
                    checkoutRepo(env.GIT_REPO_URL, env.GIT_CREDENTIALS, env.BRANCH)
                }
            }
        }

        stage('Build Application') {
            steps {
                mvnBuild()
            }
        }

        stage('Run Maven Command') {
            steps {
                script {
                    sh 'mvn install:install-file -Dfile=${WORKSPACE}/target/Enfinity-CommonApplication-1.0.jar -DgroupId=EnfinityCommonApplicationJar -DartifactId=EnfinityCommonApplicationJar -Dversion=1.0 -Dpackaging=jar'
                }
            }
        }
    }
    post {
        success {
            notification('SUCCESS', env.EMAIL_RECIPIENT)
        }
        failure {
            notification('FAILURE', env.EMAIL_RECIPIENT)
        }
    }
}
