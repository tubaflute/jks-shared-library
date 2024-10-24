import hudson.FilePath

def call() {
    pipeline {
        agent any
        
        parameters {
            string(name: 'GIT_URL', defaultValue: '', description: 'Git repository URL')
            string(name: 'APP_NAME', defaultValue: '', description: 'Application name')
            string(name: 'PACKAGE_NAME', defaultValue: '', description: 'Java package name to build')
            string(name: 'BRANCH_NAME', defaultValue: 'main', description: 'Git branch name')
        }

        environment {
            GIT_URL = "${params.GIT_URL}"
            APP_NAME = "${params.APP_NAME}"
            PACKAGE_NAME = "${params.PACKAGE_NAME}"
            BRANCH_NAME = "${params.BRANCH_NAME}"
        }

        stages {
            stage('Generate Jenkinsfile') {
                steps {
                    script {
                        // 构建 Jenkinsfile 的内容
                        def jenkinsfileContent = """
                        pipeline {
                            agent any
                            stages {
                                stage('Checkout') {
                                    steps {
                                        git branch: '${BRANCH_NAME}', url: '${GIT_URL}'
                                    }
                                }
                                stage('Build') {
                                    steps {
                                        sh 'mvn clean package -DskipTests'
                                    }
                                }
                                stage('Archive') {
                                    steps {
                                        archiveArtifacts artifacts: "**/${PACKAGE_NAME}.jar", allowEmptyArchive: false
                                    }
                                }
                                stage('Deploy') {
                                    steps {
                                        echo "Deploying ${APP_NAME}..."
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
