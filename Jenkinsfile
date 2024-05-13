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
        SONAR_TOKEN = credentials('SONARQUBE-ANALYSIS-TOKEN')
        SONARQUBE_IMAGE = 'budget-tracker-backend'
        SONARQUBE_IMAGE_FE = 'bugdet-tracker-fe'
        version = "1.1.${env.BUILD_ID}"
        SPRING_APP_VERSION = "${env.version}"

        // Set the version number using the Jenkins BUILD_ID environment variable.
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
        
         stage("Deploy with terraform") {
           when {
                branch 'terraform'
           }
           steps {
               script {
                   sh """
                        docker login -u ${containerRegistryCredentials_USR} -p ${containerRegistryCredentials_PSW} ${containerRegistryURL}
                        terraform init
                        terraform plan -out terra.pln -var="SPRING_APP_VERSION=${version}"
                        terraform apply -auto-approve terra.pln
                   """
               }
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
                    docker images --format '{{.Repository}}:{{.Tag}} {{.ID}}' | grep -E '(${database}|${backend}|${frontend}|haproxy)' | awk '{print \$1, \$2}'
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
