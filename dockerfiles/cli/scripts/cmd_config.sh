#!/bin/bash
# Copyright (c) 2016-2017 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

# Runs puppet image to generate ${CHE_FORMAL_PRODUCT_NAME} configuration
generate_configuration_with_puppet() {
  if is_docker_for_windows; then
    ARTIK_ENV_FILE=$(convert_posix_to_windows "${CHE_HOST_INSTANCE}/config/$CHE_MINI_PRODUCT_NAME.env")
  else
    ARTIK_ENV_FILE="${CHE_HOST_INSTANCE}/config/$CHE_MINI_PRODUCT_NAME.env"
  fi

  if debug_server; then
    CHE_ENVIRONMENT="development"
    WRITE_LOGS=""
  else
    CHE_ENVIRONMENT="production"
    WRITE_LOGS=">> \"${LOGS}\""
  fi


  if local_repo; then
    CHE_REPO="on"
    WRITE_PARAMETERS=" -e \"CHE_ASSEMBLY=${CHE_ASSEMBLY}\""
    # add local mounts only if they are present
    if [ -d "/repo/dockerfiles/init/manifests" ]; then
      WRITE_PARAMETERS+=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/manifests\":/etc/puppet/manifests:ro"
    fi
    if [ -d "/repo/dockerfiles/init/modules" ]; then
      WRITE_PARAMETERS+=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/modules\":/etc/puppet/modules:ro"
    fi
    # Handle override/addon
    if [ -d "/repo/dockerfiles/init/addon" ]; then
      WRITE_PARAMETERS+=" -v \"${CHE_HOST_DEVELOPMENT_REPO}/dockerfiles/init/addon/addon.pp\":/etc/puppet/manifests/addon.pp:ro"
    fi

  else
    CHE_REPO="off"
    WRITE_PARAMETERS=""
  fi

  for element in "${CLI_ENV_ARRAY[@]}" 
  do
    var1=$(echo $element | cut -f1 -d=)
    var2=$(echo $element | cut -f2 -d=)

    if [[ $var1 == CHE_* ]] ||
       [[ $var1 == ${CHE_PRODUCT_NAME}_* ]]; then
      WRITE_PARAMETERS+=" -e \"$var1=$var2\""
    fi
  done

  GENERATE_CONFIG_COMMAND="docker_run \
                  --env-file=\"${REFERENCE_CONTAINER_ENVIRONMENT_FILE}\" \
                  --env-file=/version/$CHE_VERSION/images \
                  -v \"${CHE_HOST_INSTANCE}\":/opt/${CHE_MINI_PRODUCT_NAME}:rw \
                  ${WRITE_PARAMETERS} \
                  -e \"ARTIK_ENV_FILE=${ARTIK_ENV_FILE}\" \
                  -e \"CHE_CONTAINER_ROOT=${CHE_CONTAINER_ROOT}\" \
                  -e \"CHE_ENVIRONMENT=${CHE_ENVIRONMENT}\" \
                  -e \"CHE_CONFIG=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_INSTANCE=${CHE_HOST_INSTANCE}\" \
                  -e \"CHE_REPO=${CHE_REPO}\" \
                  --entrypoint=/usr/bin/puppet \
                      $IMAGE_INIT \
                          apply --modulepath \
                                /etc/puppet/modules/ \
                                /etc/puppet/manifests/ --show_diff ${WRITE_LOGS}"

  log ${GENERATE_CONFIG_COMMAND}
  eval ${GENERATE_CONFIG_COMMAND}
}

