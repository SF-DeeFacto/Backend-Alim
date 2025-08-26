def APP_VERSION
def DOCKER_IMAGE_NAME
def PROD_BUILD = false
def APP_NAME = "alim-service" // 기본값, 필요시 변경 가능

pipeline {
    agent any

    parameters {
        booleanParam defaultValue: false, description: '릴리스 빌드 여부 (Docker 이미지에 -RELEASE 태그 추가)', name: 'RELEASE'
    }

    environment {
        GIT_URL = "https://github.com/SF-DeeFacto/Backend-Alim.git"
        AWS_CREDENTIALS = 'jenkins-ecr'
    }

    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: "30", artifactNumToKeepStr: "30"))
        timeout(time: 60, unit: 'MINUTES')
        retry(2)
    }

    stages{

         stage('Checkout Source Code') {
            steps {
                sshagent(credentials: [env.SSH_KEY_ID]) { // [변경] SSH 키로 인증
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: "*/dev"]], // [변경] dev 브랜치만 감지
                        doGenerateSubmoduleConfigurations: false,
                        extensions: [],
                        userRemoteConfigs: [[url: "${GIT_URL}"]]
                    ])
                }
        }

        stage('Set Version & Docker Image Name') {
            steps {
                script {
                    // [변경] 최신 태그 자동 계산
                    def lastTag = sh(script: "git describe --tags --abbrev=0 || echo v1.0.0", returnStdout: true).trim()
                    def (major, minor, patch) = lastTag.replace('v','').tokenize('.')
                    patch = (patch as int) + 1
                    APP_VERSION = "v${major}.${minor}.${patch}"

                    if (params.RELEASE) {
                        APP_VERSION += '-RELEASE'
                        PROD_BUILD = true
                    }

                    // [변경] 새로운 태그 생성 및 푸시
                    sshagent(credentials: [env.SSH_KEY_ID]) {
                        sh """
                            git config user.name "jenkins"
                            git config user.email "jenkins@sf-deefacto.com"
                            git tag ${APP_VERSION}
                            git push origin ${APP_VERSION}
                        """
                    }

                    withCredentials([file(credentialsId: 'deefacto-Alim-service-env', variable: 'ENV_FILE')]) {
                        def props = readProperties file: ENV_FILE
                        env.ECR_REPOSITORY = props.ECR_REPOSITORY
                        env.AWS_ACCOUNT_ID = props.AWS_ACCOUNT_ID
                        env.AWS_REGION = props.AWS_REGION
                        env.ECR_REGISTRY_URL = "${env.AWS_ACCOUNT_ID}.dkr.ecr.${env.AWS_REGION}.amazonaws.com"
                    }

                    DOCKER_IMAGE_NAME = "${env.ECR_REGISTRY_URL}/${env.ECR_REPOSITORY}:${APP_NAME}-${APP_VERSION}"

                    sh "echo 'App name is: ${env.APP_NAME}'"
                    sh "echo 'ECR Repository is: ${env.ECR_REPOSITORY}'"
                    sh "echo 'DOCKER_IMAGE_NAME is ${DOCKER_IMAGE_NAME}'"
                }
            }
        }


        stage('Login to ECR') {
            steps {
                script {
                    withAWS(credentials: AWS_CREDENTIALS, region: env.AWS_REGION) {
                        sh "aws ecr get-login-password --region ${env.AWS_REGION} | docker login --username AWS --password-stdin ${env.ECR_REGISTRY_URL}"
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE_NAME}", "--build-arg APP_NAME=${APP_NAME} .")
                }
            }
        }

        stage('Push Docker Image') {
            steps {
                script {
                    sh "docker push ${DOCKER_IMAGE_NAME}"
                    sh "docker rmi ${DOCKER_IMAGE_NAME}"
                }
            }
        }
    }
}
