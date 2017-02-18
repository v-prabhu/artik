#!/bin/bash
# Copyright (c) 2016-2017 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html



pre_init() {
  # This must be incremented when BASE is incremented by an API developer
  CHE_CLI_API_VERSION=2

  CHE_PRODUCT_NAME="ARTIK"
  CHE_MINI_PRODUCT_NAME="artik"
  CHE_FORMAL_PRODUCT_NAME="Artik IDE"
  CHE_ASSEMBLY_IN_REPO_MODULE_NAME="assembly/assembly-main"
  CHE_ASSEMBLY_IN_REPO="${CHE_ASSEMBLY_IN_REPO_MODULE_NAME}/target/artik-ide-*/artik-ide-*"
  CHE_SERVER_CONTAINER_NAME="artik"
  CHE_IMAGE_FULLNAME="codenvy/artik-cli"
  DEFAULT_ARTIK_PORT=8080
  DEFAULT_CHE_PORT=8080

  ARTIK_PORT=${ARTIK_PORT:-${DEFAULT_ARTIK_PORT}}
  CHE_PORT=${ARTIK_PORT}
  CHE_MIN_RAM=1.5
  CHE_MIN_DISK=100
  CHE_COMPOSE_PROJECT_NAME=$CHE_MINI_PRODUCT_NAME


  ADDITIONAL_MANDATORY_PARAMETERS=""
  ADDITIONAL_OPTIONAL_DOCKER_PARAMETERS="
  -e ARTIK_HOST=<YOUR_HOST>            IP address or hostname where artik will serve its users
  -e ARTIK_PORT=<YOUR_PORT>            Port where che will bind itself to
  -e ARTIK_CONTAINER=<YOUR_NAME>       Name for the che container"
  ADDITIONAL_OPTIONAL_DOCKER_MOUNTS=""
  ADDITIONAL_COMMANDS=""
  ADDITIONAL_GLOBAL_OPTIONS=""
}
