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