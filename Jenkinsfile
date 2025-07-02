@Library('jenkins-shared-library@main') _

pipeline {
    agent {
        label 'Jenkins-Agent'
    }
    tools {
        jdk 'Jdk17'
        maven 'maven3'
    }
    environment {
        GIT_REPO_URL = 'https://github.com/adminacute/Enfinity-CommonApplication.git'
        GIT_CREDENTIALS = 'krishnaachauhan-PAT'
            // 'jennkins-to-github'
        BRANCH = 'main'
            // 'devops_main'
        EMAIL_RECIPIENT = 'krishna.chauhan@bankaiinformatics.co.in,pooja.bharambe@bankaiinformatics.co.in'
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
        stage('SonarQube Analysis') {
            steps {
                    script {
                        // Define the SonarQube scanner tool
                        def scannerHome = tool name: 'Sonarqube-token', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
                        
                        // Perform SonarQube analysis
                        withSonarQubeEnv('Sonarqube-token') {    
                            sh """
                                ${scannerHome}/Sonarqube-token \
                                -Dsonar.qualityProfile=Acute-Java-Quality-Profiles \
                                -Dsonar.qualityGate=Acute-Java-Quality-Gate \
                                -Dsonar.projectKey=Java-Project-1  \
                                -Dsonar.sourceEncoding=UTF-8 \
                                -Dsonar.language=java \
                                -Dsonar.host.url=http://10.14.1.49:9000/ \
                                -Dsonar.token=sqp_b526783c031d5a8e3f258a81a6749136f6420418 \
                                -Dsonar.java.libraries=target/*.jar \
                                -Dsonar.java.binaries=target/classes    
                            """
                        }
                    }
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
