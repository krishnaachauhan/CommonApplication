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

        stage('SonarQube Analysis') {
            steps {
                script {
                    // Updated token tool name
                    def scannerHome = tool name: 'sonar-scanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
                    
                    // Generate JaCoCo test coverage reports
                    sh 'mvn test jacoco:report'

                    withSonarQubeEnv('sonar-scanner') {
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

        stage('Export SonarQube Issues') {
            steps {
                sh """
                    curl -u sqp_9a9bc0bf72a1cdb12400e0a3d5e0b79d295b0de2: \
                    "http://10.14.1.49:9000/api/issues/search?componentKeys=java-profile-projets" \
                    -o issue.json
                """
                archiveArtifacts artifacts: 'issue.json', fingerprint: true
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
