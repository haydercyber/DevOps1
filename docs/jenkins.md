# Create CI Using Jenkins 
To create Continuous Integration (CI) using Jenkins, you can utilize the Jenkins Job DSL plugin along with the Jenkins Build DSL. The Jenkins Job DSL plugin allows you to define and manage Jenkins jobs programmatically, while the Jenkins Build DSL provides a way to describe the build steps within a job using a Groovy-based DSL. Here's an example of how you can create a CI pipeline using these tools will exaplain 



### Install the required plugins:
- Jenkins Job DSL plugin: Install this plugin from the Jenkins plugin manager. 
- Jenkins Build DSL: Install this plugin from the Jenkins plugin manager. Configure Job DSL Seed Job:
#### Configure Job DSL Seed Job:
- Create a new freestyle Jenkins job. 
- In the job configuration, under "Build Environment," select "Process Job DSLs."
- Add a new "DSL Scripts" entry and provide the path to your Job DSL script.
- Save the configuration.
####  Create a Job DSL script 
- Create a new file with a .groovy extension (e.g., build.groovy) to define your Jenkins 
- In the script, use the Job DSL syntax to define your CI pipeline. Here's an example: 
```
multibranchPipelineJob('jenkins/seed/piktochart/build') {
    description('Build pipeline for warsha Project ')
    displayName('piktochart-build')
    branchSources {
        branchSource {
            source {
                bitbucket {
                    serverUrl('https://github.com')
                    repoOwner('haydercyber')
                    credentialsId('jenkins/MainDevops')
                    repository('piktochart')
                    traits {
                        bitbucketBranchDiscovery {
                            strategyId(1)
                        }
                        bitbucketBuildStatusNotifications {
                            disableNotificationForNotBuildJobs(true)
                        }
                        headWildcardFilter {
                            excludes('')
                            includes('master')
                        }
                    }
                }
            }
        }
    }
    properties {
        kubernetes {
            permittedClouds(['eks-clients-staging'])
        }
        authorizationMatrix {
            inheritanceStrategy {
                inheriting()
            }
        }
    }
    factory {
        workflowBranchProjectFactory {
            scriptPath('Jenkinsfile-Build')
        }
    }
    orphanedItemStrategy {
        discardOldItems {
            daysToKeep(15)
            numToKeep(30)
        }
    }
    triggers {
        bitBucketMultibranchTrigger {}
        bitbucketPush {
            buildOnCreatedBranch(true)
        }
    }
}
```
The code snippet you provided represents a Jenkins Job DSL script to create a multi-branch pipeline job in Jenkins. Let's go through the code and understand its functionality:
#### Explanation of the code 
* The multibranchPipelineJob function creates a multi-branch pipeline job with the name 'jenkins/seed/piktochart/build'.
* The description and displayName functions set the description and display name of the job, respectively.
* The branchSources block defines the branch sources for the job. In this case, it configures a Bitbucket repository as the source.
* Inside the branchSource, the Bitbucket repository details are specified, including the server URL, repository owner, credentials, and repository name.
* Traits are used to further configure the Bitbucket branch discovery, build status notifications, and head wildcard filtering.
* The properties block configures additional properties for the job, such as Kubernetes cloud permissions and authorization matrix.
* The factory block defines the factory for creating workflow branch projects. The scriptPath specifies the path to the Jenkinsfile that contains the pipeline script for the build.
* The orphanedItemStrategy block configures the strategy for handling orphaned items (branches that no longer exist). In this case, it discards old items based on a time-based and count-based criteria.

The triggers block configures triggers for the job, such as Bitbucket multibranch trigger and Bitbucket push trigger.
##### Jenkins-Build
The `Jenkinsfile-Build` is referenced in your previous Job DSL script as the script path for the workflow branch project factory. The Jenkinsfile-Build file is expected to exist in your repository and contain the pipeline script for the build stage of your multi-branch pipeline job.
The Jenkinsfile is a special file recognized by Jenkins as a pipeline script. It allows you to define the entire pipeline flow, including stages, steps, and conditions, in a single file committed to your version control system.
Here's an example of a simplified Jenkinsfile-Build that demonstrates a basic build stage:

```
pipeline {
    agent {
        kubernetes {
            cloud 'eks-clients-staging'
            inheritFrom 'jenkins-clients-staging'
            yamlFile 'pipeline/pipelinePod.yaml'
            workspaceVolume dynamicPVC(accessModes: 'ReadWriteOnce', requestsSize: '40Gi', storageClassName: 'ebs')
        }
    }
    environment {
        PATH = "/kaniko:/busybox:$PATH"
        ECR_REPO_URL = '235234525235.dkr.ecr.eu-central-1.amazonaws.com'
        ECR_REPO_NAME = 'piktochart'
        SCANNER_HOME = tool 'sonar'
        SCANNER_PROJECT_NAME = "haydercyber:piktochart"
    }
    options {
        ansiColor('xterm')
    }
    stages {
        stage('Make version') {
            steps {
                script {
                    env.shaVersion = sh(returnStdout: true, script: "git rev-parse --short=7 HEAD").trim()
                    currentBuild.description = "piktochart-${env.shaVersion}"
                }
            }
        }
        stage('Static code analysis') {
            tools {
                nodejs 'Nodejs'
            }
            steps {
                withSonarQubeEnv('sonarqube'){
                    sh '''
                        $SCANNER_HOME/bin/sonar-scanner -Dsonar.projectKey=$SCANNER_PROJECT_NAME
                    '''
                }
            }
        }
        stage('Build') {
            steps {
                container(name: 'builder', shell: '/busybox/sh') {
                    sh ''' 
                    /kaniko/executor --dockerfile Dockerfile --context . --destination=${ECR_REPO_URL}/${ECR_REPO_NAME}:piktochart-${GIT_COMMIT:0:7}
                    '''
                }
            }
        }
    stage('Trigger of dev Environment Deployment') {
            when {
                expression { env.BRANCH_NAME == 'master' }
                beforeOptions true
                beforeInput true
                beforeAgent true
            }
	    steps {
	        script {
		    build job: 'jenkins/seed/piktochart/deploy', 
                        parameters: [
                            string(name: 'ENVIRONMENT', value: "dev"),
                            string(name: 'IMAGETAG', value: env.shaVersion)
                        ]
		}
	    }
	}
    }
}

```
The `Jenkinsfile-Build` 
represents a Jenkins pipeline script. Let's go through the code and understand its functionality:
### Explanation of the code 
* The pipeline block defines the structure of the Jenkins pipeline.
* The environment block sets environment variables used throughout the pipeline, such   as PATH, ECR_REPO_URL, ECR_REPO_NAME, SCANNER_HOME, and SCANNER_PROJECT_NAME.
* The options block configures pipeline options, in this case enabling ANSI colors.
* The stages block defines the different stages of the pipeline.
* Inside each stage, the steps block contains the actions to be performed.
* The first stage, "Make version," retrieves the short Git commit SHA and sets it as the shaVersion environment variable. It also updates the build description with the version information.
* The second stage, "Static code analysis," uses the sonar-scanner to perform static code analysis on the project. It uses the SonarQube environment configured with the withSonarQubeEnv block.
* The third stage, "Build," runs a build step inside a container named "builder" using the /busybox/sh shell. It executes the /kaniko/executor command to build a Docker image based on the specified Dockerfile and destination repository.
* The final stage, "Trigger of dev Environment Deployment," is triggered only when the branch being built is master. It triggers a separate Jenkins job called 'jenkins/seed/piktochart/deploy' with two parameters: ENVIRONMENT set to "dev" and IMAGETAG set to env.shaVersion

### Strategy for tagging the container images
the image tag is created using the Git commit SHA. Here's a breakdown of the relevant code
```
stage('Make version') {
    steps {
        script {
            env.shaVersion = sh(returnStdout: true, script: "git rev-parse --short=7 HEAD").trim()
            currentBuild.description = "piktochart-${env.shaVersion}"
        }
    }
}
```
In the Make version stage, the script block is used to execute a shell script command:
```
env.shaVersion = sh(returnStdout: true, script: "git rev-parse --short=7 HEAD").trim()
```
This command retrieves the Git commit SHA using git rev-parse --short=7 HEAD. The --short=7 option ensures that only the first 7 characters of the commit SHA are returned. The returnStdout: true parameter allows capturing the command output, which is stored in the env.shaVersion environment variable.

Finally, the image tag is set as the description of the current build:

```
currentBuild.description = "piktochart-${env.shaVersion}"
```
In this case, the image tag is composed of the string "piktochart-" followed by the Git commit SHA. The image tag is then associated with the current build, potentially providing additional context or identification for the generated Docker image.

Please note that the image tag creation process can vary depending on your specific requirements and project setup. The code snippet above demonstrates one approach using the Git commit SHA as the basis for the image tag. You can customize this process based on your needs, such as using a timestamp, version number, or any other meaningful identifier to generate the image tag.




<img src="docs/gif/jenkins-build.gif" alt="Jenkins-Build" />

