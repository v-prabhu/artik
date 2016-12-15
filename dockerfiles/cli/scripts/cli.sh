#!/bin/bash
# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
cli_pre_init() {
  GLOBAL_HOST_IP=${GLOBAL_HOST_IP:=$(docker_run --net host eclipse/che-ip:nightly)}
  DEFAULT_ARTIK_HOST=$GLOBAL_HOST_IP
  ARTIK_HOST=${ARTIK_HOST:-${DEFAULT_ARTIK_HOST}}
  ARTIK_PORT=8080
}



# Runs puppet image to generate ${CHE_FORMAL_PRODUCT_NAME} configuration
generate_configuration_with_puppet() {
  debug $FUNCNAME

  if is_docker_for_windows; then
    ARTIK_ENV_FILE=$(convert_posix_to_windows "${CHE_HOST_INSTANCE}/config/$CHE_MINI_PRODUCT_NAME.env")
  else
    ARTIK_ENV_FILE="${CHE_HOST_INSTANCE}/config/$CHE_MINI_PRODUCT_NAME.env"
  fi

  if [ "${CHE_DEVELOPMENT_MODE}" = "on" ]; then
    # Note - bug in docker requires relative path for env, not absolute
    GENERATE_CONFIG_COMMAND="docker_run -it \
                  --env-file=\"${REFERENCE_CONTAINER_ENVIRONMENT_FILE}\" \
                  --env-file=/version/$CHE_VERSION/images \
                  -v \"${CHE_HOST_INSTANCE}\":/opt/${CHE_MINI_PRODUCT_NAME}:rw \
                  -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/manifests\":/etc/puppet/manifests:ro \
                  -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/modules\":/etc/puppet/modules:ro \
                  -e \"ARTIK_ENV_FILE=${ARTIK_ENV_FILE}\" \
                  -e \"CHE_ENVIRONMENT=development\" \
                  -e \"CHE_CONFIG=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_INSTANCE=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_DEVELOPMENT_REPO=${CHE_HOST_DEVELOPMENT_REPO}\" \
                  -e \"CHE_ASSEMBLY=${CHE_ASSEMBLY}\" \
                  --entrypoint=/usr/bin/puppet \
                      $IMAGE_INIT \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/${CHE_MINI_PRODUCT_NAME}.pp --show_diff"
  else
    GENERATE_CONFIG_COMMAND="docker_run -it \
                  --env-file=\"${REFERENCE_CONTAINER_ENVIRONMENT_FILE}\" \
                  --env-file=/version/$CHE_VERSION/images \
                  -v \"${CHE_HOST_INSTANCE}\":/opt/${CHE_MINI_PRODUCT_NAME}:rw \
                  -e \"ARTIK_ENV_FILE=${ARTIK_ENV_FILE}\" \
                  -e \"CHE_ENVIRONMENT=production\" \
                  -e \"CHE_CONFIG=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_INSTANCE=${CHE_HOST_INSTANCE}\" \
                  --entrypoint=/usr/bin/puppet \
                      $IMAGE_INIT \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/${CHE_MINI_PRODUCT_NAME}.pp --show_diff >> \"${LOGS}\""
  fi

  log ${GENERATE_CONFIG_COMMAND}
  eval ${GENERATE_CONFIG_COMMAND}
}

