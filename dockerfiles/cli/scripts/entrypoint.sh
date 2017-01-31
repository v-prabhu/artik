#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Tyler Jewell - Initial Implementation
#

CHE_PRODUCT_NAME="ARTIK"
CHE_MINI_PRODUCT_NAME="artik"
CHE_FORMAL_PRODUCT_NAME="Artik IDE"
CHE_CONTAINER_ROOT="/data"
CHE_ASSEMBLY_IN_REPO_MODULE_NAME="assembly/assembly-main"
CHE_ASSEMBLY_IN_REPO="${CHE_ASSEMBLY_IN_REPO_MODULE_NAME}/target/artik-ide-*/"
CHE_SCRIPTS_CONTAINER_SOURCE_DIR="/repo/dockerfiles/cli/scripts"
CHE_SERVER_CONTAINER_NAME="artik"
CHE_IMAGE_FULLNAME="codenvy/artik-cli"


pre_init() {
  ADDITIONAL_MANDATORY_PARAMETERS=""
  ADDITIONAL_OPTIONAL_DOCKER_PARAMETERS="
  -e ARTIK_HOST=<YOUR_HOST>            IP address or hostname where artik will serve its users
  -e ARTIK_PORT=<YOUR_PORT>            Port where che will bind itself to
  -e ARTIK_CONTAINER=<YOUR_NAME>       Name for the che container"
  ADDITIONAL_OPTIONAL_DOCKER_MOUNTS=""
  ADDITIONAL_COMMANDS=""
  ADDITIONAL_GLOBAL_OPTIONS=""
}

source /scripts/base/startup.sh
start "$@"