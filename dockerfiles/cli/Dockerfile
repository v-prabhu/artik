# Copyright (c) 2016 Codenvy, S.A.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# build:
#   docker build -t codenvy/artik-cli .
#
# use:
#    docker run -v $(pwd):/artik codenvy/artik-cli [command]

FROM eclipse/che-base:nightly

COPY scripts /scripts/
COPY version /version/

RUN mkdir /che
ENTRYPOINT ["/scripts/entrypoint.sh"]
