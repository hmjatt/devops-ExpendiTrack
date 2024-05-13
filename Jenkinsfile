pipeline {
    agent {
        // Name of build agent
        label 'devops-dev'
    }
    environment {
        // Using the credentials() method to retrieve the Artifactory credentials from the Jenkins credentials store
        containerRegistryCredentials = credentials('ARTIFACTORY_PUBLISH')
        containerRegistryURL = 'jack.hc-sc.gc.ca'
        imageName = 'devops-training-w24'
        dockerComposeDir = 'docker-compose-dir' 
        database = 'db'
        backend = 'backend'
        frontend = 'frontend'
        seleniumStandalone = 'selenium-standalone'
        seleniumTests = 'selenium-tests'
        // Set the version number using the Jenkins BUILD_ID environment variable.
        version = "1.0.${env.BUILD_ID}"
        PROJECT_NAME = "budget_tracker"


    }
    stages {
        stage('Environment Setup') {
            steps {
                // clean the workspace on the agent before the build.
                cleanWs()                
                checkout scm
                script{
                    // Setup Artifactory connection
                    artifactoryServer = Artifactory.server 'default'
                    artifactoryDocker = Artifactory.docker server: artifactoryServer
                    buildInfo = Artifactory.newBuildInfo()
                }
            }
        }

        stage('Tests') {
            steps {
                script {
                     sh """
                        cd frontend
                        npm install
                        npm test
                    """
                }
            }
        }

        stage('Build, Tag and Push Docker Compose') {
            steps {

                sh """ docker compose down -v """

                sh """                    
                    docker compose version

                    docker login -u ${containerRegistryCredentials_USR} -p ${containerRegistryCredentials_PSW} ${containerRegistryURL}

                    cd ${database}
                    docker build --no-cache -t ${database}:${version} .
                    docker tag ${database}:${version} ${containerRegistryURL}/devops/${imageName}/${dockerComposeDir}/${database}:${version}
                    docker push ${containerRegistryURL}/devops/${imageName}/${dockerComposeDir}/${database}:${version}

                    cd ..

                    cd ${backend}
                    docker build --no-cache -t ${backend}:${version} .
                    docker tag ${backend}:${version} ${containerRegistryURL}/devops/${imageName}/${dockerComposeDir}/${backend}:${version}
                    docker push ${containerRegistryURL}/devops/${imageName}/${dockerComposeDir}/${backend}:${version}

                    cd ..

                    cd ${frontend}
                    docker build --no-cache -t ${frontend}:${version} .
                    docker tag ${frontend}:${version} ${containerRegistryURL}/devops/${imageName}/${dockerComposeDir}/${frontend}:${version}
                    docker push ${containerRegistryURL}/devops/${imageName}/${dockerComposeDir}/${frontend}:${version}
                    
                """
            }
        }

         stage("Deploy to Dev") {
            steps {
                script {

                    

                    sh""" docker ps -a """

                    sh"""

                        docker stop ${PROJECT_NAME}-frontend-1 || true
                        docker stop ${PROJECT_NAME}-backend-1 || true
                        docker stop ${PROJECT_NAME}-db-1 || true
                    """
                    
                    sh """
                        docker rm ${PROJECT_NAME}-frontend-1 || true
                        docker rm ${PROJECT_NAME}-backend-1 || true
                        docker rm ${PROJECT_NAME}-db-1 || true
                    """

                    sh """ echo "y" | docker system prune -a --volumes """
                    sh """ echo "y" | docker volume prune -a """

                    sh""" docker ps -a """

                    def currentBranch = env.BRANCH_NAME
                    echo "Current Branch: ${currentBranch}"

                    sh """ docker compose down -v """

                    sh """ docker login -u ${containerRegistryCredentials_USR} -p ${containerRegistryCredentials_PSW} ${containerRegistryURL} """


                    sh """ docker compose -f docker-compose.yml -p ${PROJECT_NAME} up --build -d """
                    

                }

                sh """
                    docker stop haproxy || true
                    docker rm haproxy || true
                    cp haproxy.cfg /var/opt/devops/ops/haproxy.cfg
                    docker create --name haproxy \
                        -v /var/opt/devops/ops/haproxy.cfg:/usr/local/etc/haproxy/ha-body.cfg:ro \
                        -v /var/opt/devops/ops/certs.d/:/usr/local/etc/haproxy/certs.d:ro \
                        -p 80:80 \
                        -p 443:443 \
                        -p 127.0.0.1:8400:8400 \
                        jack.hc-sc.gc.ca/base/haproxy:5.0.118-http
                    docker network connect ${PROJECT_NAME}_app_network haproxy
                    docker start haproxy
                """
            }
        }

        
    }

    post {
        // Command to run always, here we set the initial value for variable resultString to 'None'.
        always {
            script {
                resultString = "None"
            }
        }
        success {
            script {
                resultString = "Success"
            }
        }
        unstable {
            script {
                resultString = "Unstable"
            }
        }
        failure {
            script {
                resultString = "Failure"
            }
        }
        cleanup {
            // Commands to run during cleanup (after all post steps) go here.
            script {

                // clean the workspace after the build.
                cleanWs()
                
                // Retrieve the list of images with the same name.
                def allImages = sh(script: """
                    docker images --format '{{.Repository}}:{{.Tag}} {{.ID}}' | grep -E '(${database}|${backend}|${frontend}|${seleniumStandalone}|${seleniumTests})' | awk '{print \$1, \$2}'
                    """, returnStdout: true).trim()

                 echo """ ${allImages} """

                // convert the output to a list
                def imagesList = allImages.tokenize('\n')

                // Iterate through the images
                if (env.BRANCH_NAME == 'main'){
                    for (imageInfo in imagesList) {
                        def (fullTag, imageId) = imageInfo.split(' ')
                        def (imageName, imageVersion) = fullTag.split(':')

                        // Delete the image if its version does not match the current version
                        if (imageVersion != version) {
                            sh """ docker rmi -f ${imageId} """
                            echo """ Deleted image ${imageName} with ID ${imageId} """
                        }
                    }
                }
                
            }
        }
    }
}
