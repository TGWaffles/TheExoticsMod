pipeline {
    agent any
    triggers {
        pollSCM('') // Enabling being build on Push
    }
    stages {
        stage ('Clean') {
            steps {
                withGradle {
                    sh './gradlew clean'
                }
            }
        }
        stage ('Test') {
            steps {
                withGradle {
                    sh './gradlew test'
                }
            }
        }
        stage ('Deploy') {
            steps {
                withGradle {
                    sh './gradlew reobfShadowJar'
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            junit '**/build/test-results/test/*.xml'
            jacoco(
                execPattern: 'build/jacoco/*.exec',
                classPattern: 'build/classes',
                sourcePattern: 'src/main/java',
                exclusionPattern: 'src/test*'
            )
        }
    }
}