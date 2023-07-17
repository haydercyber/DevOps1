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
                        url('https://bitbucket.org/creativeadvtech/piktochart.git')
                        credentials('jenkins/MainDevops')
                    }
                    branch('master')
                    browser {
                        bitbucketWeb {
                            repoUrl('https://bitbucket.org/creativeadvtech/piktochart.git')
                        }
                    }
                }
            }
            scriptPath('Jenkinsfile-Deploy')
        }
    }
}
