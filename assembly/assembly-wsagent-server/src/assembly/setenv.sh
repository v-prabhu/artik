#
# Copyright (c) 2016-2017 Samsung Electronics Co., Ltd.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#
# Contributors:
#   Codenvy, S.A. - Initial implementation
#   Samsung Electronics Co., Ltd. - Initial implementation
#

#Global Conf dir
[ -z "${CHE_LOCAL_CONF_DIR}" ]  && CHE_LOCAL_CONF_DIR="${CATALINA_HOME}/conf/"

#Global JAVA options
[ -z "${JAVA_OPTS}" ]  && JAVA_OPTS="-Xms256m -Xmx1024m  -Djava.security.egd=file:/dev/./urandom"

#Global LOGS DIR
[ -z "${CHE_LOGS_DIR}" ]  && CHE_LOGS_DIR="$CATALINA_HOME/logs"

[ -z "${JPDA_ADDRESS}" ]  && JPDA_ADDRESS="4403"


#Going to check is directory with Artik API Docs exist. If it exist set special property to the location of this folder
#if not map it to the webapps/ROOT. We need this workaround for staring WS-Agent in any case.
#This property is used in server.xml of this bundle in Context section.
# details here https://github.com/codenvy/artik-ide/issues/151
if [ -d "/root/.apidocs/html" ]; then
  export ARTIK_DOCS_HOME="/root/.apidocs/html";
else 
  export ARTIK_DOCS_HOME="${CATALINA_HOME}/webapps/ROOT";
fi


#Tomcat options
[ -z "${CATALINA_OPTS}" ]  && CATALINA_OPTS="-Dcom.sun.management.jmxremote  \
                                             -Dcom.sun.management.jmxremote.ssl=false \
                                             -Dcom.sun.management.jmxremote.authenticate=false \
                                             -Dche.local.conf.dir=${CHE_LOCAL_CONF_DIR} \
                                             -Dartik.docs.home=${ARTIK_DOCS_HOME}"

#Class path
[ -z "${CLASSPATH}" ]  && CLASSPATH="${CATALINA_HOME}/conf/:${JAVA_HOME}/lib/tools.jar"


export JAVA_OPTS="$JAVA_OPTS  -Dche.logs.dir=${CHE_LOGS_DIR}"


#Class path
[ -z "${SERVER_PORT}" ]  && SERVER_PORT=8080
export SERVER_PORT
