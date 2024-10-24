import hudson.FilePath

def jenkinsfilePath

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
            string(name: 'GIT_CREDENTIALS_ID', defaultValue: '', description: 'Git credentials ID')
            string(name: 'TARGET_GIT_BRANCH', defaultValue: 'main', description: 'Target Git branch to push Jenkinsfile')
        }

        environment {
            GIT_URL = "${params.GIT_URL}"
            APP_NAME = "${params.APP_NAME}"
            MAVEN_ARGS = "${params.MAVEN_ARGS}"
            GIT_CREDENTIALS_ID = "${params.GIT_CREDENTIALS_ID}"
            TARGET_GIT_BRANCH = "${params.TARGET_GIT_BRANCH}"
        }

        stages {
            stage('Generate Jenkinsfile') {
                steps {
                    cleanWs()
                    script {
                        // 构建 Jenkinsfile 的内容
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
                                        cleanWs()
                                        checkout([\$class: 'GitSCM', branches: [[name: "\$branchStatus"]], extensions: [], userRemoteConfigs: [[credentialsId: 'xxx_code', url: '${GIT_URL}']]])
                                    }
                                }
                                stage('Maven Build') {
                                    steps {
                                        withMaven(jdk: 'jdk-180', maven: 'maven-363') {
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
                        jenkinsfilePath = "${jenkinsfileDir}/${APP_NAME}_Jenkinsfile"

                        // 保存 Jenkinsfile 到指定路径
                        def file = new File(jenkinsfilePath)
                        file.write(jenkinsfileContent)

                        echo "Jenkinsfile for ${APP_NAME} saved to: ${jenkinsfilePath}"
                    }
                }
            }
            stage('Commit and Push to Git') {
                steps {
                    withCredentials([sshUserPrivateKey(credentialsId: 'xxx_code', keyFileVariable: 'SSH_KEY')]) {
                        // some block
                        withEnv(["GIT_SSH_COMMAND=ssh -i $SSH_KEY -o StrictHostKeyChecking=no"]) {
                            // dir("jksfiles") {
                                sh """
                                git clone git@codeup.aliyun.com:newxxx/share-devops/save-jenkinsfile.git 
                                cd save-jenkinsfile
                                git pull 
                                cp "${jenkinsfilePath}" . 
                                git add ${APP_NAME}_Jenkinsfile 
                                git commit -m 'Add Jenkinsfile'  
                                git push origin master 
                                """
                            // }
                        }
                    }
                    
                    // script {
                    //     // 配置 Git 仓库，提交并推送 Jenkinsfile
                    //     sh """
                    //     git config --global user.email "jenkins@example.com"
                    //     git config --global user.name "Jenkins CI"
                    //     git clone ${GIT_URL} repo
                    //     cd repo
                    //     git checkout ${TARGET_GIT_BRANCH}
                    //     cp ${jenkinsfilePath} .
                    //     git add ${APP_NAME}_Jenkinsfile
                    //     git commit -m "Add Jenkinsfile for ${APP_NAME}"
                    //     git push origin ${TARGET_GIT_BRANCH}
                    //     """
                    // }
                }
            }
        }

        post {
            success {
                echo "Jenkinsfile has been generated, committed, and pushed to Git successfully."
            }
            failure {
                echo "Failed to generate, commit, or push the Jenkinsfile to Git."
            }
        }
    }
}
