#!/bin/bash
# Copyright (c) 2016-2017 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html

post_init() {
  GLOBAL_HOST_IP=${GLOBAL_HOST_IP:=$(docker_run --net host ${BOOTSTRAP_IMAGE_CHEIP})}
  DEFAULT_ARTIK_HOST=$GLOBAL_HOST_IP
  ARTIK_HOST=${ARTIK_HOST:-${DEFAULT_ARTIK_HOST}}
}
