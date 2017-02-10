# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# build:
#   docker build -t codenvy/artik-server .
#
# use:
#    docker run codenvy/artik-server [command]

FROM openjdk:8u111-jre-alpine
ARG USER_HOME

ENV LANG=C.UTF-8 \
    DOCKER_VERSION=1.6.0 \
    DOCKER_BUCKET=get.docker.com \
    CHE_IN_CONTAINER=true

RUN echo "http://dl-4.alpinelinux.org/alpine/edge/community" >> /etc/apk/repositories && \
    apk add --update curl openssl sudo bash && \
    curl -sSL "https://${DOCKER_BUCKET}/builds/Linux/x86_64/docker-${DOCKER_VERSION}" -o /usr/bin/docker && \
    chmod +x /usr/bin/docker && \
    echo "%root ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers && \
    rm -rf /tmp/* /var/cache/apk/*

EXPOSE 8000 8080
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
ADD artik-ide.tar.gz /home/user/
