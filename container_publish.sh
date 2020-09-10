#!/usr/bin/env bash

set -e

SHORT_SHA=${BITBUCKET_COMMIT:0:7}

docker login -u salesfox -p ${DOCKERHUB_PASSWORD}

DOCKER_IMAGE_NAME=-${BITBUCKET_BRANCH}-${SHORT_SHA}
repository="salesfox/portal-backend"

pack build $repository:$DOCKER_IMAGE_NAME \
  --builder heroku/buildpacks:18 \
  --buildpack heroku/java \
  --buildpack heroku/gradle
  --buildpack heroku/procfile \
  --path . \
  --publish
