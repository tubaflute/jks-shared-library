import hudson.FilePath

def call() {
    pipeline {
        agent any
        options {
            buildDiscarder(logRotator(numToKeepStr: '3'))
            disableConcurrentBuilds()
            timestamps()
        }
        
        parameters {
            string(name: 'GIT_URL', defaultValue: '', description: 'Git repository URL')
            string(name: 'APP_NAME', defaultValue: '', description: 'Application name')
            string(name: 'MAVEN_ARGS', defaultValue: '-U clean package -DskipTests', description: 'Maven build option and args')
        }

        environment {
            GIT_URL = "${params.GIT_URL}"
            APP_NAME = "${params.APP_NAME}"
            MAVEN_ARGS = "${params.MAVEN_ARGS}"
        }

        stages {
            stage('Generate Jenkinsfile') {
                steps {
                    script {
                        // 使用字符串插值创建 Jenkinsfile 的内容
                        def jenkinsfileContent = """
                        pipeline {
                            agent any
                            options {
                                buildDiscarder(logRotator(numToKeepStr: '5'))
                                disableConcurrentBuilds()
                                timestamps()
                            }
                            parameters {
                                gitParameter(branch: '', branchFilter: '.*', defaultValue: '', name: 'branchStatus', quickFilterEnabled: false, selectedValue: 'NONE', sortMode: 'NONE', tagFilter: '*', type: 'GitParameterDefinition')
                            }
                            stages {
                                stage('Checkout') {
                                    steps {
                                        checkout([\$class: 'GitSCM', branches: [[name: "\$branchStatus"]], extensions: [], userRemoteConfigs: [[credentialsId: 'xxx_code', url: '${GIT_URL}']]])
                                    }
                                }
                                stage('Maven Build') {
                                    steps {
                                        withMaven(globalMavenSettingsConfig: 'null', jdk: 'jdk-180', maven: 'maven-363', mavenSettingsConfig: 'c8dd8127-032d-46e4-a83d-f1e5877e5e50') {
                                            sh "mvn ${MAVEN_ARGS}"
                                        }
                                    }
                                }
                            }
                            post {
                                success {
                                    echo "Pipeline completed successfully."
                                }
                                failure {
                                    echo "Pipeline failed."
                                }
                            }
                        }
                        """

                        // 输出目录
                        def jenkinsHome = System.getenv("JENKINS_HOME")
                        if (!jenkinsHome) {
                            error "JENKINS_HOME environment variable is not set."
                        }

                        // 创建目录路径
                        def jenkinsfileDir = "${jenkinsHome}/new_jksfiles"
                        def dir = new File(jenkinsfileDir)
                        if (!dir.exists()) {
                            dir.mkdirs() // 如果目录不存在，创建它
                        }

                        // Jenkinsfile 路径
                        def jenkinsfilePath = "${jenkinsfileDir}/${APP_NAME}_Jenkinsfile"

                        // 保存 Jenkinsfile 到指定路径
                        def file = new File(jenkinsfilePath)
                        file.write(jenkinsfileContent)

                        echo "Jenkinsfile for ${APP_NAME} saved to: ${jenkinsfilePath}"
                    }
                }
            }
        }

        post {
            success {
                echo "Jenkinsfile has been generated and saved successfully."
            }
            failure {
                echo "Failed to generate and save the Jenkinsfile."
            }
        }
    }
}
