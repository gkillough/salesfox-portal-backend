#!/usr/bin/env bash

SHORT_SHA=${CIRCLE_SHA1:0:7}

docker_image_name=${CIRCLE_BRANCH}-${SHORT_SHA}
repository="salesfox/portal-backend"

trivy image \
  --severity "HIGH,CRITICAL" \
  --vuln-type os \
  -f json \
  -o reports/trivy.json \
  $repository:$docker_image_name

exit 0
