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