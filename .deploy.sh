#!/bin/bash

export SERVICE=web
export PROJECT=piktochart
export IMAGETAG=piktochart-${GIT_COMMIT:0:7}

cd $SERVICE
yq e -i '(.image.tag) = strenv(IMAGETAG)' values-${ENV}.yaml

checkout() {
    git add --all && \
    git commit -m "${SERVICE} image tags updated to ${IMAGETAG}" &&  \
    git push -u origin HEAD:master
}

if (checkout; [ $? -eq 0 ]) ; then
    echo "**SUCCESS** ${SERVICE} will be deployed soon"
    argocd login --username $ARGOCD_USERNAME --password $ARGOCD_PASSWORD argocd.creativeadvtech.ml
    argocd app get --grpc-web ${PROJECT}-${ENV}-${SERVICE} --hard-refresh
    argocd app wait --grpc-web ${PROJECT}-${ENV}-${SERVICE}
else
    echo "**WARNING** nothing has changed, exit"
fi
