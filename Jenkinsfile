pipeline {
    agent {
        label 'jenkins-agent1'
    }
    tools {
        jdk 'jdk17'
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
 
        // stage('SonarQube Analysis') {
        //     steps {
        //         script {
        //             def scannerHome = tool name: 'sonarqube-token1', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
 
        //             withSonarQubeEnv('sonarqube-token1') {
        //                 sh """
        //                     ${scannerHome}/bin/sonar-scanner \
        //                     -Dsonar.projectKey=java-profile-projets \
        //                     -Dsonar.qualityProfile=java-quality-1 \
        //                     -Dsonar.host.url=http://10.14.1.49:9000 \
        //                     -Dsonar.token=sqa_c24e0ba176993e6ea6e507abd62e9e95c10084ec \
        //                     -Dsonar.sourceEncoding=UTF-8 \
        //                     -Dsonar.language=java \
        //                     -Dsonar.java.binaries=target/classes \
        //                     -Dsonar.java.libraries=target/*.jar
        //                 """
        //             }
        //         }
        //     } sonarqube-token
        // }
        stage('SonarQube Analysis') {
            steps {
                script {
                    def scannerHome = tool name: 'sonarqube-token', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
            
                    // First generate test coverage reports
                    sh 'mvn test jacoco:report'  // Generates JaCoCo report at target/site/jacoco/jacoco.xml
            
                    withSonarQubeEnv('sonarqube-token') {
                        sh """
                            ${scannerHome}/bin/sonar-scanner \
                            -Dsonar.projectKey=java-profile-projets \
                            -Dsonar.qualityProfile=java-quality-1 \
                            -Dsonar.host.url=http://10.14.1.49:9000 \
                            -Dsonar.token=sqp_9a9bc0bf72a1cdb12400e0a3d5e0b79d295b0de2 \
                            -Dsonar.sourceEncoding=UTF-8 \
                            -Dsonar.language=java \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.java.libraries=target/*.jar \
                            -Dsonar.java.coveragePlugin=jacoco \
                            -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                            -Dsonar.tests=src/test/java \
                            -Dsonar.junit.reportPaths=target/surefire-reports \
                            -Dsonar.sources=src/main/java
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


sqa_c24e0ba176993e6ea6e507abd62e9e95c10084ec  6-09-2025 testing purpose 
