# CI/CD With Jenkins and ArgoCD 
Jenkins and Argo CD can be combined to create a powerful CI/CD pipeline for deploying applications to Kubernetes. Here's a high-level overview of how you can integrate Jenkins and Argo CD in your CI/CD workflow

i will create costome dsl for auto deployemt from jenkins and intgrate with ArgoCD

The code snippet you provided appears to be a Jenkins shared library script written in Groovy. This script defines a custom function called call that takes several parameters.

Let's break down the script:
```
#!/usr/bin/env groovy

def call(String urlToCheckout, String environment, String project, String imagetag) {
    checkout([$class: 'GitSCM',
            branches: [[name: '*/master']],
            extensions: [[$class: 'RelativeTargetDirectory', relativeTargetDir: 'helmchart']],
            userRemoteConfigs: [[credentialsId: 'jenkins/MainDevops', url: urlToCheckout]]])
    container(name: 'deployer', shell: '/bin/bash') {
        withCredentials([gitUsernamePassword(credentialsId: 'jenkins/MainDevops', gitToolName: 'git'),
                        usernamePassword(credentialsId: 'jenkins/argocd', passwordVariable: 'ARGOCD_PASSWORD', usernameVariable: 'ARGOCD_USERNAME')]) {
            withEnv(["ENV=${environment}", "PROJECT=${project}", "IMAGETAG=${imagetag}"]) {
                sh '''
                    ../.deploy.sh
                '''
            }
        }
    }
}
```
This script defines a function named call that performs several tasks:

* `Git Checkout`: It performs a Git checkout of a specified repository using the GitSCM plugin. The urlToCheckout parameter represents the URL of the repository to checkout, and the checkout is performed for the */master branch.

* `Container Execution`: It defines a container named 'deployer' and specifies that the shell used inside the container should be '/bin/bash'. The subsequent steps will be executed inside this container.

* `Credentials`: It retrieves credentials using the withCredentials block, which allows you to securely access and use sensitive credentials, such as Git and Argo CD credentials. The credentialsId parameter specifies the ID of the credentials stored in Jenkins for authentication.

* `Environment Variables`: It sets environment variables using the withEnv block. The values of environment, project, and imagetag are assigned to the corresponding environment variables (ENV, PROJECT, and IMAGETAG).

* `Shell Execution`: It executes a shell script using the sh step. The script being executed is '../.deploy.sh', which is a relative path to the '.deploy.sh' script.

This script provides a reusable function that can be called from Jenkins pipelines or other Jenkins jobs. It enables you to easily checkout a Git repository, perform tasks within a container, handle credentials, set environment variables, and execute shell scripts within a Jenkins pipeline or job.


lets Create costome dsl with name  `deploy.groovy`
The code snippet you provided is a Jenkins pipeline job definition written in Groovy. This pipeline job is responsible for deploying a given version/image tag of an application to a specified environment. Let's break down the different sections of the code
```
pipelineJob('jenkins/seed/piktochart/deploy') {
    description('Deploy a given version/image-tag to an environment.')
    displayName('piktochart-deploy')
    logRotator {
        numToKeep(50)
    }
    parameters {
        choiceParam('ENVIRONMENT', ['staging', 'production', 'dev'], 'Please choose the environment to deploy to')
        stringParam('IMAGETAG', ' ', "Please specify image tag to deploy")
    }
    definition {
        cpsScm {
            lightweight(true)
            scm {
                git {
                    remote {
                        name('origin')
                        url('https://github.com/haydercyber/piktochart.git')
                        credentials('jenkins/MainDevops')
                    }
                    branch('master')
                    browser {
                        bitbucketWeb {
                            repoUrl('https://github.com/haydercyber/piktochart.git')
                        }
                    }
                }
            }
            scriptPath('Jenkinsfile-Deploy')
        }
    }
}
```
This Jenkins pipeline job is defined using the pipelineJob function. Here's an overview of the different sections:

* `description` and displayName are optional fields that provide a description and display name for the job.
* `logRotator` section defines the log rotation settings for the job, specifying the number of build logs to keep.

* `parameters` section defines the input parameters for the job. In this case, it includes a choice parameter named 'ENVIRONMENT' with options 'staging', 'production', and 'dev', and a string parameter named 'IMAGETAG' for specifying the image tag to deploy.

* `definition` section defines the pipeline job's definition. It uses the cpsScm step to define a lightweight SCM (source code management) checkout. Inside cpsScm, it specifies the SCM system as Git and provides the repository URL, credentials for accessing the repository, and the branch to checkout ('master'). It also configures the Bitbucket browser for better integration with Bitbucket repositories.

* `scriptPath` specifies the path to the Jenkinsfile that will be executed for this pipeline job. The Jenkinsfile is named 'Jenkinsfile-Deploy' and should contain the steps and stages for deploying the application.

This pipeline job allows you to trigger a deployment for a specific version/image tag of the application by providing the environment and image tag as parameters. The pipeline will then checkout the code from the specified Git repository, execute the Jenkinsfile-Deploy script, and perform the deployment steps defined within it.

lets create `Jenkinsfile-Deploy`

The code snippet you provided is a Jenkins declarative pipeline written in Groovy. This pipeline defines a deployment process for an application using Kubernetes as the agent. Let's break down the different sections of the pipeline

```
pipeline {
    agent {
        kubernetes {
            cloud 'eks-clients-staging'
            yamlFile 'pipeline/deploy-pod-template.yaml'
        }
    }
    options {
        ansiColor('xterm')
    }
    stages {
        // Stage: Setting deployment
        stage('Setting deployment') {
            steps {
                script {
                    currentBuild.description = "Deploying image: ${params.IMAGETAG}\n Deploying to the ${params.ENVIRONMENT} environment"
                }
            }
        }
        
        // Stage: Deploy to dev
        stage('Deploy to dev') {
            when {
                expression { params.ENVIRONMENT == 'dev' }
                beforeInput true
                beforeAgent true
                beforeOptions true
            }
            steps {
                deployment('https://github.com/haydercyber/Piktochart.git', params.ENVIRONMENT, 'piktochart', params.IMAGETAG.trim())
            }
        }
    }
}

```
Here's an overview of the pipeline structure:

* `agent`: Specifies the agent for running the pipeline. In this case, it's a Kubernetes agent defined by kubernetes. It uses the eks-clients-staging cloud to provision the necessary resources. The yamlFile parameter points to a Kubernetes Pod template file (deploy-pod-template.yaml).

* `options`: Defines additional pipeline options. In this case, it sets ansiColor to enable ANSI color output in the console log.

* `stages`: Contains the stages of the pipeline.
    * `stage('Setting deployment')`: This stage sets the deployment information as the build description. It uses a script block to access the params object and dynamically set the build description based on the provided IMAGETAG and ENVIRONMENT parameters.

    * `stage('Deploy to dev')`: This stage deploys the application to the dev environment. It is executed conditionally based on the value of the ENVIRONMENT parameter. If ENVIRONMENT is equal to 'dev', the deployment step deployment() is executed, passing the necessary parameters: Git repository URL, environment (params.ENVIRONMENT), project ('piktochart'), and image tag (params.IMAGETAG.trim())

This pipeline demonstrates a basic deployment process to the dev environment. You can add more stages and customize the pipeline to match your specific deployment requirements for different environments or additional deployment targets.

you can see the full ci/cd 


![CICD](https://i.imgur.com/gij67x0.gif)