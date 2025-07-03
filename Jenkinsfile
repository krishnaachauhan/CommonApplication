pipeline {
    agent {
        label 'jenkins-agent1'
    }
    tools {
        jdk 'Jdk17'
        maven 'maven3'
    }
    environment {
        GIT_REPO_URL     = 'https://github.com/krishnaachauhan/CommonApplication.git'
        GIT_CREDENTIALS  = 'krishnaachauhan'
        BRANCH           = 'main'
        EMAIL_RECIPIENT  = 'krishna.chauhan@bankaiinformatics.co.in,pooja.bharambe@bankaiinformatics.co.in'
    }
 
    stages {
        stage('Cleanup Workspace') {
            steps {
                cleanWs()
            }
        }
 
        stage('Checkout') {
            steps {
                git branch: "${env.BRANCH}",
                    credentialsId: "${env.GIT_CREDENTIALS}",
                    url: "${env.GIT_REPO_URL}"
            }
        }
 
        stage('Build Application') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }
 
        stage('SonarQube Analysis') {
            steps {
                script {
                    def scannerHome = tool name: 'Sonarqube-token', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
 
                    withSonarQubeEnv('Sonarqube-token') {
                        sh """
                            ${scannerHome}/bin/sonar-scanner \
                            -Dsonar.projectKey=Java-Project-1 \
                            -Dsonar.qualityProfile=Acute-Java-Quality-Profiles \
                            -Dsonar.host.url=http://10.14.1.49:9000 \
                            -Dsonar.token=sqp_b526783c031d5a8e3f258a81a6749136f6420418 \
                            -Dsonar.sourceEncoding=UTF-8 \
                            -Dsonar.language=java \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.java.libraries=target/*.jar
                        """
                    }
                }
            }
        }
 
        stage('Run Maven Install-File') {
            steps {
                sh '''
                mvn install:install-file \
                  -Dfile=target/Enfinity-CommonApplication-1.0.jar \
                  -DgroupId=EnfinityCommonApplicationJar \
                  -DartifactId=EnfinityCommonApplicationJar \
                  -Dversion=1.0 \
                  -Dpackaging=jar
                '''
            }
        }
    }
 
    post {
        success {
            mail to: "${env.EMAIL_RECIPIENT}",
                subject: "Jenkins Build SUCCESS - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Good news! The build was successful.\n\nJob: ${env.JOB_NAME}\nBuild: ${env.BUILD_NUMBER}\nURL: ${env.BUILD_URL}"
        }
        failure {
            mail to: "${env.EMAIL_RECIPIENT}",
                subject: "Jenkins Build FAILURE - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                body: "Unfortunately, the build failed.\n\nJob: ${env.JOB_NAME}\nBuild: ${env.BUILD_NUMBER}\nURL: ${env.BUILD_URL}"
        }
    }
}
