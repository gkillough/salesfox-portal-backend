#!/usr/bin/env bash

set -e

SHORT_SHA=${BITBUCKET_COMMIT:0:7}

DOCKER_IMAGE_NAME=${BITBUCKET_BRANCH}-${SHORT_SHA}
repository="salesfox/portal-backend"

pack build -v $repository:$docker_image_name \
  --builder heroku/buildpacks:18 \
  --buildpack heroku/java \
  --buildpack heroku/gradle
  --buildpack heroku/procfile \
  --path . \
  --publish
