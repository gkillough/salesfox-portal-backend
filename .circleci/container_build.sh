#!/usr/bin/env bash

set -e

SHORT_SHA=${CIRCLE_SHA1:0:7}

docker_image_name=${CIRCLE_BRANCH}-${SHORT_SHA}
repository="salesfox/portal-backend"

docker login -u salesfox -p ${DOCKERHUB_PASSWORD}

pack build -v $repository:$docker_image_name \
  --builder heroku/buildpacks:18 \
  --buildpack heroku/java \
  --buildpack heroku/gradle \
  --buildpack heroku/procfile \
  --path . \
  --publish
