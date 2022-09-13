#!/usr/bin/env bash
set -e

if [[ -z "${TRACERBOY_HOME}" ]]; then
  echo "TRACERBOY_HOME environment variable is not set!" && exit 255
fi

cd "$TRACERBOY_HOME" && \
  docker-compose \
    -f .docker/docker-compose.yml \
    --project-name tracerboy \
    --project-directory . $@
